package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.Property;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    @EntityGraph(attributePaths = { "guestTypes" })
    List<Property> findByTenant_TenantId(UUID tenantId);

    java.util.Optional<Property> findByPropertyIdAndTenant_TenantId(UUID propertyId, UUID tenantId);
}

