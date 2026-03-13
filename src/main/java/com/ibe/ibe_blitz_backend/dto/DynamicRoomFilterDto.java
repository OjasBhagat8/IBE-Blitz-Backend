package com.ibe.ibe_blitz_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicRoomFilterDto {
    private String filterKey;
    private String filterType;
    private List<DynamicFilterOptionDto> options;
    private BigDecimal minValue;
    private BigDecimal maxValue;
}
