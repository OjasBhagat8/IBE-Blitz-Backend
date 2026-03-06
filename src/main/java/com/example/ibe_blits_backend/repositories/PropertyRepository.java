package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.Property;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    @EntityGraph(attributePaths = { "guestTypes" })
    List<Property> findByTenant_TenantId(UUID tenantId);
}
