package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "license")
@Data
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private LicenseType type;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;  // владелец лицензии

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;   // пользователь, активировавший лицензию (может быть null)

    @Column(name = "first_activation_date")
    private LocalDateTime firstActivationDate;

    @Column(name = "ending_date")
    private LocalDateTime endingDate;

    private Boolean blocked = false;

    @Column(name = "device_count")
    private Integer deviceCount = 1;

    private String description;
}