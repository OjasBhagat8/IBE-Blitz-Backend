package com.ibe.ibe_blitz_backend.dto;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomFilterDto {
    private UUID filterId;
    private String filterName;
    private List<FilterOptionDto> options;
}

