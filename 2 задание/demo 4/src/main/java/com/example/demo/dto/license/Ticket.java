package com.example.demo.dto.license;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Ticket {
    private LocalDateTime serverDate;
    private long ttl;
    private LocalDateTime activationDate;
    private LocalDateTime expirationDate;
    private Long userId;
    private UUID deviceId;
    private boolean blocked;
}