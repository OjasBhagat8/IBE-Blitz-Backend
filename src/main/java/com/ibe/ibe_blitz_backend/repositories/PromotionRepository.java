package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.Promotion;
import com.ibe.ibe_blitz_backend.entities.PromotionKind;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    @EntityGraph(attributePaths = {"condition", "reward", "roomTypes", "roomTypes.roomType"})
    List<Promotion> findByProperty_PropertyIdAndActiveTrueAndPromotionKind(UUID propertyId, PromotionKind promotionKind);
}
