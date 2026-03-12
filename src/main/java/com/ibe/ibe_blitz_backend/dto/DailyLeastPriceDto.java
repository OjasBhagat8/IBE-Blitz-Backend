package com.ibe.ibe_blitz_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyLeastPriceDto {

    private LocalDate date;
    private UUID propertyId;
    private UUID roomTypeId;
    private BigDecimal price;
}

