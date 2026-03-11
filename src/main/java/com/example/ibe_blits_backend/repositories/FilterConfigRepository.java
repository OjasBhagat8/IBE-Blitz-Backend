package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.FilterConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FilterConfigRepository extends JpaRepository<FilterConfig , UUID> {
    @Query("""
            select distinct fc
            from FilterConfig fc
            left join fetch fc.filters f
            where fc.roomType.roomTypeId = :roomTypeId
            """)
    Optional<FilterConfig> findDetailedByRoomType_RoomTypeId(UUID roomTypeId);
}
