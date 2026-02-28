package com.example.demo.repository;

import com.example.demo.entity.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {
}