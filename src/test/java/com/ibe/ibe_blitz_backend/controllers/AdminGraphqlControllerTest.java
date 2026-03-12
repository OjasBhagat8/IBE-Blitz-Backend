package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.PropertyConfigDto;
import com.ibe.ibe_blitz_backend.dto.RoomPriceRecordDto;
import com.ibe.ibe_blitz_backend.dto.UpdatePropertySettingsInputDto;
import com.ibe.ibe_blitz_backend.dto.UpdateTenantInputDto;
import com.ibe.ibe_blitz_backend.dto.UpsertRoomPriceInputDto;
import com.ibe.ibe_blitz_backend.entities.Tenant;
import com.ibe.ibe_blitz_backend.service.AdminPanelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminGraphqlControllerTest {

    @Mock
    private AdminPanelService adminPanelService;

    @InjectMocks
    private AdminGraphqlController controller;

    @Test
    void pricesDelegatesToService() {
        UUID propertyId = UUID.randomUUID();
        RoomPriceRecordDto dto = RoomPriceRecordDto.builder().roomTypeName("Suite").build();
        when(adminPanelService.prices(propertyId, "2026-01-01", "2026-01-31")).thenReturn(List.of(dto));

        List<RoomPriceRecordDto> result = controller.prices(propertyId, "2026-01-01", "2026-01-31");

        assertEquals(1, result.size());
    }

    @Test
    void updateTenantDelegatesToService() {
        UpdateTenantInputDto input = new UpdateTenantInputDto();
        Tenant tenant = Tenant.builder().tenantName("Hilton").build();
        when(adminPanelService.updateTenant(input)).thenReturn(tenant);

        Tenant result = controller.updateTenant(input);

        assertEquals("Hilton", result.getTenantName());
    }

    @Test
    void updatePropertySettingsDelegatesToService() {
        UpdatePropertySettingsInputDto input = new UpdatePropertySettingsInputDto();
        PropertyConfigDto dto = PropertyConfigDto.builder().propertyName("P1").build();
        when(adminPanelService.updatePropertySettings(input)).thenReturn(dto);

        PropertyConfigDto result = controller.updatePropertySettings(input);

        assertEquals("P1", result.getPropertyName());
    }

    @Test
    void upsertRoomPriceDelegatesToService() {
        UpsertRoomPriceInputDto input = new UpsertRoomPriceInputDto();
        RoomPriceRecordDto dto = RoomPriceRecordDto.builder().date("2026-01-01").build();
        when(adminPanelService.upsertRoomPrice(input)).thenReturn(dto);

        RoomPriceRecordDto result = controller.upsertRoomPrice(input);

        assertEquals("2026-01-01", result.getDate());
    }
}


