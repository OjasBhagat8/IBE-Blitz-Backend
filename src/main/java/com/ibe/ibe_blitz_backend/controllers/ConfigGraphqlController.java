package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.ConfigResponseDto;
import com.ibe.ibe_blitz_backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ConfigGraphqlController {

    private final ConfigService configService;

    @QueryMapping
    public ConfigResponseDto config(@Argument UUID tenantId) {
        return configService.getConfigByTenant(tenantId);
    }

    @QueryMapping
    public ConfigResponseDto configByTenantName(@Argument String tenantName) {
        return configService.getConfigByTenantName(tenantName);
    }
}

