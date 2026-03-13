package com.ibe.ibe_blitz_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilteredRoomResultDto {
    private UUID roomTypeId;
    private String roomTypeName;
    private String description;
    private Integer occupancy;
    private List<String> amenities;
    private List<String> images;
    private BigDecimal baseRate;
    private RoomSpecSummaryDto roomSpec;
}
