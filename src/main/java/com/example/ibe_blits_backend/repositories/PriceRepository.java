package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.Prices;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PriceRepository extends JpaRepository<Prices, UUID> {

    List<Prices> findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDate(
            UUID propertyId,
            UUID tenantId,
            Date date
    );
}