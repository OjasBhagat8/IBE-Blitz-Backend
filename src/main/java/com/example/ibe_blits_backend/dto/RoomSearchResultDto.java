package com.example.ibe_blits_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSearchResultDto {
    private UUID roomTypeId;
    private String roomTypeName;
    private BigDecimal totalPrice;
    private Integer availableCount;
}

