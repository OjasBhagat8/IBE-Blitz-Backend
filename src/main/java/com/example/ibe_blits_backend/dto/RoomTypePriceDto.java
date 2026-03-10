package com.example.ibe_blits_backend.dto;

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
