package com.example.demo.service;

import com.example.demo.dto.license.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseRepository licenseRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final UserRepository userRepository;

    private String generateCode() {
        return "LIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Ticket buildTicket(License license, Device device) {
        return Ticket.builder()
                .serverDate(LocalDateTime.now())
                .ttl(3600L)
                .activationDate(license.getFirstActivationDate())
                .expirationDate(license.getEndingDate())
                .userId(license.getUser() != null ? license.getUser().getId() : null)
                .deviceId(device.getId())
                .blocked(license.getBlocked())
                .build();
    }

    @Transactional
    public License createLicense(CreateLicenseRequest request, Long adminId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        LicenseType type = licenseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new RuntimeException("License type not found"));
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        License license = new License();
        license.setCode(generateCode());
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setDeviceCount(request.getDeviceCount() != null ? request.getDeviceCount() : 1);
        license.setDescription(request.getDescription());
        license.setBlocked(false);

        License savedLicense = licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(savedLicense);
        history.setUser(userRepository.getReferenceById(adminId));
        history.setStatus("CREATED");
        history.setDescription("License created by admin");
        licenseHistoryRepository.save(history);

        return savedLicense;
    }

    @Transactional
    public Ticket activateLicense(ActivateLicenseRequest request, Long userId) {
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new RuntimeException("License not found"));

        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new RuntimeException("License owned by another user");
        }

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseGet(() -> {
                    Device newDevice = new Device();
                    newDevice.setMacAddress(request.getDeviceMac());
                    newDevice.setName(request.getDeviceName());
                    newDevice.setUser(userRepository.getReferenceById(userId));
                    return deviceRepository.save(newDevice);
                });

        boolean alreadyActivated = deviceLicenseRepository
                .findByLicenseIdAndDeviceId(license.getId(), device.getId())
                .isPresent();

        if (!alreadyActivated) {
            long currentDeviceCount = deviceLicenseRepository.countByLicenseId(license.getId());
            if (currentDeviceCount >= license.getDeviceCount()) {
                throw new RuntimeException("Device limit reached");
            }
        }

        boolean firstActivation = (license.getUser() == null);
        if (firstActivation) {
            license.setUser(userRepository.getReferenceById(userId));
            license.setFirstActivationDate(LocalDateTime.now());
            LocalDateTime ending = LocalDateTime.now().plusDays(license.getType().getDefaultDurationInDays());
            license.setEndingDate(ending);
            licenseRepository.save(license);
        }

        if (!alreadyActivated) {
            DeviceLicense dl = new DeviceLicense();
            dl.setLicense(license);
            dl.setDevice(device);
            deviceLicenseRepository.save(dl);
        }

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(userRepository.getReferenceById(userId));
        history.setStatus("ACTIVATED");
        history.setDescription("Activated on device " + device.getMacAddress());
        licenseHistoryRepository.save(history);

        return buildTicket(license, device);
    }

    @Transactional(readOnly = true)
    public Ticket checkLicense(CheckLicenseRequest request, Long userId) {
        Device device = deviceRepository.findByMacAddress(request.getDeviceMac())
                .orElseThrow(() -> new RuntimeException("Device not found"));

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                        device.getId(), userId, request.getProductId())
                .orElseThrow(() -> new RuntimeException("Active license not found"));

        return buildTicket(license, device);
    }

    @Transactional
    public Ticket renewLicense(RenewLicenseRequest request, Long userId) {
        License license = licenseRepository.findByCode(request.getActivationKey())
                .orElseThrow(() -> new RuntimeException("License not found"));

        if (license.getEndingDate() == null ||
                license.getEndingDate().isAfter(LocalDateTime.now().plusDays(7))) {
            throw new RuntimeException("Renewal not allowed");
        }

        LocalDateTime newEnding = license.getEndingDate()
                .plusDays(license.getType().getDefaultDurationInDays());
        license.setEndingDate(newEnding);
        licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(userRepository.getReferenceById(userId));
        history.setStatus("RENEWED");
        licenseHistoryRepository.save(history);

        DeviceLicense deviceLicense = deviceLicenseRepository.findFirstByLicenseId(license.getId())
                .orElseThrow(() -> new RuntimeException("No device associated with this license"));
        Device device = deviceLicense.getDevice();

        return buildTicket(license, device);
    }
}