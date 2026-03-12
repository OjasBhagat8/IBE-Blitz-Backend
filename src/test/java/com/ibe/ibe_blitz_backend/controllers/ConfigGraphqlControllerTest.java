package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.ConfigResponseDto;
import com.ibe.ibe_blitz_backend.service.ConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigGraphqlControllerTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigGraphqlController controller;

    @Test
    void configDelegatesToService() {
        UUID tenantId = UUID.randomUUID();
        ConfigResponseDto dto = ConfigResponseDto.builder().tenantId(tenantId).tenantName("Hilton").build();
        when(configService.getConfigByTenant(tenantId)).thenReturn(dto);

        ConfigResponseDto result = controller.config(tenantId);

        assertEquals("Hilton", result.getTenantName());
    }

    @Test
    void configByTenantNameDelegatesToService() {
        ConfigResponseDto dto = ConfigResponseDto.builder().tenantName("Radison").build();
        when(configService.getConfigByTenantName("rad")).thenReturn(dto);

        ConfigResponseDto result = controller.configByTenantName("rad");

        assertEquals("Radison", result.getTenantName());
    }
}


