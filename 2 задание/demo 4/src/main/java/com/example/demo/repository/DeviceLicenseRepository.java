package com.example.demo.repository;

import com.example.demo.entity.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
    long countByLicenseId(UUID licenseId);

    Optional<DeviceLicense> findByLicenseIdAndDeviceId(UUID licenseId, UUID deviceId);

    // Найти первую (любую) связь для лицензии (чтобы получить устройство для тикета)
    @Query("SELECT dl FROM DeviceLicense dl WHERE dl.license.id = :licenseId ORDER BY dl.activationDate ASC")
    Optional<DeviceLicense> findFirstByLicenseId(@Param("licenseId") UUID licenseId);
}