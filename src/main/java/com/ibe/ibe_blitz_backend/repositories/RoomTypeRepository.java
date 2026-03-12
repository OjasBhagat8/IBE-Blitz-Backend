package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoomTypeRepository  extends JpaRepository<RoomType, UUID> {
    @Query("""
            select rt
            from RoomType rt
            left join fetch rt.roomSpec rs
            where rt.property.propertyId = :propertyId
              and rt.property.tenant.tenantId = :tenantId
            """)
    List<RoomType> findDetailedByPropertyAndTenant(@Param("propertyId") UUID propertyId, @Param("tenantId") UUID tenantId);
}

