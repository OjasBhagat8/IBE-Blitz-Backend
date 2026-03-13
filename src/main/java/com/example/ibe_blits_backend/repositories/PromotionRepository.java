package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.Promotion;
import com.example.ibe_blits_backend.entities.PromotionKind;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    @EntityGraph(attributePaths = {"condition", "reward", "roomTypes", "roomTypes.roomType"})
    List<Promotion> findByProperty_PropertyIdAndActiveTrueAndPromotionKind(UUID propertyId, PromotionKind promotionKind);
}
