package com.example.ibe_blits_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomPricingResponseDto {
    private StandardRateResponseDto standardRate;
    private List<PromotionDealResponseDto> deals;
}
