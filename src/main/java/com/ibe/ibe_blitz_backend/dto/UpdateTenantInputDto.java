package com.ibe.ibe_blitz_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateTenantInputDto {
    private UUID tenantId;
    private String tenantName;
    private String tenantLogo;
    private String tenantBanner;
    private String tenantCopyright;
}

