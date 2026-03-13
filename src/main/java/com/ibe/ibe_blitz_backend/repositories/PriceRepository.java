package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.Prices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceRepository extends JpaRepository<Prices, UUID> {

    List<Prices> findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDate(
            UUID propertyId,
            UUID tenantId,
            Date date
    );

    @Query("""
            select p
            from Prices p
            join fetch p.roomType rt
            left join fetch rt.roomSpec rs
            where p.property.propertyId = :propertyId
              and p.property.tenant.tenantId = :tenantId
              and p.date between :from and :to
            """)
    List<Prices> findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDateBetween(
            UUID propertyId,
            UUID tenantId,
            Date from,
            Date to
    );

    Optional<Prices> findFirstByRoomType_RoomTypeIdAndDate(UUID roomTypeId, Date date);

    @Query("select p from Prices p join fetch p.roomType rt where p.property.propertyId = :propertyId and p.date between :from and :to order by p.date asc")
    List<Prices> findByProperty_PropertyIdAndDateBetweenOrderByDateAsc(UUID propertyId, Date from, Date to);

    @Query("""
            select p from Prices p
            join fetch p.roomType rt
            where p.property.propertyId = :propertyId
              and rt.roomTypeId = :roomTypeId
              and p.date between :from and :to
            order by p.date asc
            """)
    List<Prices> findByPropertyIdAndRoomTypeIdAndDateBetweenOrderByDateAsc(
            @Param("propertyId") UUID propertyId,
            @Param("roomTypeId") UUID roomTypeId,
            @Param("from") Date from,
            @Param("to") Date to
    );
}

