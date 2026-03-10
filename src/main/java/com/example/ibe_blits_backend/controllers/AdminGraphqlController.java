package com.example.ibe_blits_backend.controllers;

import com.example.ibe_blits_backend.dto.PropertyConfigDto;
import com.example.ibe_blits_backend.dto.RoomPriceRecordDto;
import com.example.ibe_blits_backend.dto.UpdatePropertySettingsInputDto;
import com.example.ibe_blits_backend.dto.UpdateTenantInputDto;
import com.example.ibe_blits_backend.dto.UpsertRoomPriceInputDto;
import com.example.ibe_blits_backend.entities.Tenant;
import com.example.ibe_blits_backend.service.AdminPanelService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AdminGraphqlController {

    private final AdminPanelService adminPanelService;

    @QueryMapping
    public List<RoomPriceRecordDto> prices(@Argument UUID propertyId, @Argument String fromDate, @Argument String toDate) {
        return adminPanelService.prices(propertyId, fromDate, toDate);
    }

    @MutationMapping
    public Tenant updateTenant(@Argument UpdateTenantInputDto input) {
        return adminPanelService.updateTenant(input);
    }

    @MutationMapping
    public PropertyConfigDto updatePropertySettings(@Argument UpdatePropertySettingsInputDto input) {
        return adminPanelService.updatePropertySettings(input);
    }

    @MutationMapping
    public RoomPriceRecordDto upsertRoomPrice(@Argument UpsertRoomPriceInputDto input) {
        return adminPanelService.upsertRoomPrice(input);
    }
}
