package com.example.ibe_blits_backend.service;

import com.example.ibe_blits_backend.dto.ConfigResponseDto;
import com.example.ibe_blits_backend.dto.GuestTypeDto;
import com.example.ibe_blits_backend.dto.PropertyConfigDto;
import com.example.ibe_blits_backend.entities.GuestType;
import com.example.ibe_blits_backend.entities.Property;
import com.example.ibe_blits_backend.entities.Tenant;
import com.example.ibe_blits_backend.repositories.PropertyRepository;
import com.example.ibe_blits_backend.repositories.TenantRepository;
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
        List<Property> properties = propertyRepository.findByTenant_TenantId(tenantId);

        List<PropertyConfigDto> propertyDtos = properties.stream()
                .map(this::mapPropertyToDto)
                .toList();
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() ->new IllegalArgumentException("Tenant not found" + tenantId));

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
