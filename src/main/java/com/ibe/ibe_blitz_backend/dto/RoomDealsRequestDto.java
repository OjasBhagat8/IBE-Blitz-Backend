package com.ibe.ibe_blitz_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDealsRequestDto {
    private UUID roomTypeId;
    private UUID propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private List<GuestSelectionInputDto> guestSelections;
}
