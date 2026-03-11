package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.FilterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FilterConfigRepository extends JpaRepository<FilterConfig , UUID> {
    @Query("""
            select distinct fc
            from FilterConfig fc
            left join fetch fc.filters f
            where fc.property.propertyId = :propertyId
            """)
    Optional<FilterConfig> findDetailedByProperty_PropertyId(@Param("propertyId") UUID propertyId);
}
