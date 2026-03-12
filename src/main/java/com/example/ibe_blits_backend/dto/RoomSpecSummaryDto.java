package com.example.ibe_blits_backend.dto;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSpecSummaryDto {
    private UUID roomSpecId;
    private String bedType;
    private BigDecimal area;
    private Integer minOcc;
    private Integer maxOcc;
}
