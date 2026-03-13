package com.example.ibe_blits_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDealResponseDto {
    private UUID promotionId;
    private UUID promoCodeId;
    private String title;
    private String description;
    private BigDecimal totalPrice;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private String promotionType;
}
