package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.PromoCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromoCodeRepository extends JpaRepository<PromoCode, UUID> {
    @EntityGraph(attributePaths = {
            "promotion",
            "promotion.condition",
            "promotion.reward",
            "promotion.roomTypes",
            "promotion.roomTypes.roomType"
    })
    Optional<PromoCode> findByCode(String code);
}
