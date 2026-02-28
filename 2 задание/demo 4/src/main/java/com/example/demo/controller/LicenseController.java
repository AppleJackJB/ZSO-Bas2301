package com.example.demo.controller;

import com.example.demo.dto.license.*;
import com.example.demo.entity.License;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    @PostMapping("/create")
    public ResponseEntity<?> createLicense(@RequestBody CreateLicenseRequest request,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            License license = licenseService.createLicense(request, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(license);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateLicense(@RequestBody ActivateLicenseRequest request,
                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Ticket ticket = licenseService.activateLicense(request, currentUser.getId());
            TicketResponse response = new TicketResponse();
            response.setTicket(ticket);
            response.setSignature("dummy-signature");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found"))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            if (e.getMessage().contains("another user"))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            if (e.getMessage().contains("limit"))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkLicense(@RequestBody CheckLicenseRequest request,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Ticket ticket = licenseService.checkLicense(request, currentUser.getId());
            TicketResponse response = new TicketResponse();
            response.setTicket(ticket);
            response.setSignature("dummy-signature");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/renew")
    public ResponseEntity<?> renewLicense(@RequestBody RenewLicenseRequest request,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Ticket ticket = licenseService.renewLicense(request, currentUser.getId());
            TicketResponse response = new TicketResponse();
            response.setTicket(ticket);
            response.setSignature("dummy-signature");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found"))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            if (e.getMessage().contains("not allowed"))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}