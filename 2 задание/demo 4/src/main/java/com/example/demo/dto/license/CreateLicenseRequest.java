package com.example.demo.dto.license;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateLicenseRequest {
    private UUID productId;
    private UUID typeId;
    private Long ownerId;
    private Integer deviceCount;
    private String description;
}