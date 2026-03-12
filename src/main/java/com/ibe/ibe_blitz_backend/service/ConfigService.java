package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.ConfigResponseDto;
import com.ibe.ibe_blitz_backend.dto.GuestTypeDto;
import com.ibe.ibe_blitz_backend.dto.PropertyConfigDto;
import com.ibe.ibe_blitz_backend.entities.GuestType;
import com.ibe.ibe_blitz_backend.entities.Property;
import com.ibe.ibe_blitz_backend.entities.Tenant;
import com.ibe.ibe_blitz_backend.repositories.PropertyRepository;
import com.ibe.ibe_blitz_backend.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfigService {
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    public ConfigResponseDto getConfigByTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found " + tenantId));
        return buildConfigResponse(tenant);
    }

    public ConfigResponseDto getConfigByTenantName(String tenantName) {
        Tenant tenant = tenantRepository.findByTenantNameIgnoreCase(tenantName)
                .or(() -> tenantRepository.findFirstByTenantNameContainingIgnoreCase(tenantName))
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found " + tenantName));
        return buildConfigResponse(tenant);
    }

    private ConfigResponseDto buildConfigResponse(Tenant tenant) {
        UUID tenantId = tenant.getTenantId();
        List<Property> properties = propertyRepository.findByTenant_TenantId(tenantId);

        List<PropertyConfigDto> propertyDtos = properties.stream()
                .map(this::mapPropertyToDto)
                .toList();

        return ConfigResponseDto.builder()
                .tenantId(tenantId)
                .tenantName(tenant.getTenantName())
                .tenantLogo(tenant.getTenantLogo())
                .tenantBanner(tenant.getTenantBanner())
                .tenantCopyright(tenant.getTenantCopyright())
                .properties(propertyDtos)
                .build();
    }

    private PropertyConfigDto mapPropertyToDto(Property p) {
        List<GuestTypeDto> guestTypeDtos = p.getGuestTypes().stream()
                .map(this::mapGuestTypeToDto)
                .toList();

        return PropertyConfigDto.builder()
                .propertyId(p.getPropertyId())
                .propertyName(p.getPropertyName())
                .guestAllowed(p.getGuestAllowed())
                .guestFlag(p.getGuestFlag())
                .roomCount(p.getRoomCount())
                .lengthOfStay(p.getLengthOfStay())
                .roomFlag(p.getRoomFlag())
                .accessibleFlag(p.getAccessibleFlag())
                .guestTypes(guestTypeDtos)
                .build();
    }

    private GuestTypeDto mapGuestTypeToDto(GuestType g) {
        return GuestTypeDto.builder()
                .guestTypeId(g.getGuestTypeId())
                .guestTypeName(g.getGuestTypeName())
                .minAge(g.getMinAge())
                .maxAge((g.getMaxAge()))
                .build();
    }
}

