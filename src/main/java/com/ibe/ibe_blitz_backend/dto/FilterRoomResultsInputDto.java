package com.ibe.ibe_blitz_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class FilterRoomResultsInputDto {
    private UUID tenantId;
    private UUID propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer rooms;
    private Boolean accessible;
    private Integer page;
    private Integer size;
    private List<SelectedFilterInputDto> filters;
}
