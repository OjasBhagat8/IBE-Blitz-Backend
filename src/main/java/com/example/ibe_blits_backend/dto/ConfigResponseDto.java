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
    private String tenantName;
    private String tenantLogo;
    private String tenantBanner;
    private String tenantCopyright;
    private List<PropertyConfigDto> properties;
}
