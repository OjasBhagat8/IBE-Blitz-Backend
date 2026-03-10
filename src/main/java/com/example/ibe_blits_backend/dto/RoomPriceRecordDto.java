package com.example.ibe_blits_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class RoomPriceRecordDto {
    private UUID priceId;
    private UUID roomTypeId;
    private String roomTypeName;
    private UUID propertyId;
    private String date;
    private BigDecimal roomPrice;
    private Integer quantity;
}
