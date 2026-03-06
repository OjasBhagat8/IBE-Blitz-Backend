package com.example.ibe_blits_backend.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigResponseDto {
    private UUID tenantId;
    private List<PropertyConfigDto> properties;
}
