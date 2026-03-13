package com.ibe.ibe_blitz_backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRoomsInputDto {
    private UUID tenantId;
    private UUID propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer rooms;
    private Boolean accessible;
    private Integer page;
    private Integer size;
}