package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.Prices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    List<Prices> findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDateBetween(
            UUID propertyId,
            UUID tenantId,
            Date from,
            Date to
    );

    Optional<Prices> findFirstByRoomType_RoomTypeIdAndDate(UUID roomTypeId, Date date);

    @Query("select p from Prices p join fetch p.roomType rt where p.property.propertyId = :propertyId and p.date between :from and :to order by p.date asc")
    List<Prices> findByProperty_PropertyIdAndDateBetweenOrderByDateAsc(UUID propertyId, Date from, Date to);
}
