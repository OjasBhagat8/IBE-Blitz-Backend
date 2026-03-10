package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByTenantNameIgnoreCase(String tenantName);
    Optional<Tenant> findFirstByTenantNameContainingIgnoreCase(String tenantName);
}
