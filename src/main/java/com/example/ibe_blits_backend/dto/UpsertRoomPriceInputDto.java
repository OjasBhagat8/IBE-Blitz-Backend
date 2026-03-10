package com.example.ibe_blits_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class UpsertRoomPriceInputDto {
    private UUID roomTypeId;
    private String date;
    private BigDecimal roomPrice;
    private Integer quantity;
}
