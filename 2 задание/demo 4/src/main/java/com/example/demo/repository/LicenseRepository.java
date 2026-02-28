package com.example.demo.repository;

import com.example.demo.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    Optional<License> findByCode(String code);

    // Поиск активной лицензии для конкретного устройства, пользователя и продукта
    @Query("SELECT l FROM License l " +
            "JOIN DeviceLicense dl ON dl.license = l " +
            "WHERE dl.device.id = :deviceId " +
            "AND l.user.id = :userId " +
            "AND l.product.id = :productId " +
            "AND l.blocked = false " +
            "AND (l.endingDate IS NULL OR l.endingDate >= CURRENT_DATE)")
    Optional<License> findActiveByDeviceUserAndProduct(
            @Param("deviceId") UUID deviceId,
            @Param("userId") Long userId,
            @Param("productId") UUID productId);
}