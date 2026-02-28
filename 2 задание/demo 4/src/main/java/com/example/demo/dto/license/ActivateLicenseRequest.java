package com.example.demo.dto.license;

import lombok.Data;

@Data
public class ActivateLicenseRequest {
    private String activationKey;
    private String deviceMac;
    private String deviceName;
}