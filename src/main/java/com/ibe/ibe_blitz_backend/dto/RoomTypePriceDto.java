package com.ibe.ibe_blitz_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypePriceDto {

    private UUID roomTypeId;
    private BigDecimal price;
}

