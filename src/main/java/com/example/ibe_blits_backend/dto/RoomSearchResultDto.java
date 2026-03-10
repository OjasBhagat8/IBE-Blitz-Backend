package com.example.ibe_blits_backend.dto;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSearchResultDto {
    private UUID roomTypeId;
    private String roomTypeName;
    private String description;
    private Integer occupancy;
    private List<String> amenities;
    private BigDecimal baseRate;
    private RoomSpecSummaryDto roomSpec;
    private BigDecimal totalPrice;
    private Integer availableCount;
}

