package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.ConfigResponseDto;
import com.ibe.ibe_blitz_backend.entities.GuestType;
import com.ibe.ibe_blitz_backend.entities.Property;
import com.ibe.ibe_blitz_backend.entities.Tenant;
import com.ibe.ibe_blitz_backend.repositories.PropertyRepository;
import com.ibe.ibe_blitz_backend.repositories.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private ConfigService configService;

    @Test
    void getConfigByTenantMapsTenantPropertiesAndGuestTypes() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID guestTypeId = UUID.randomUUID();

        Tenant tenant = Tenant.builder()
                .tenantId(tenantId)
                .tenantName("Radison")
                .tenantLogo("logo")
                .tenantBanner("banner")
                .tenantCopyright("copyright")
                .build();

        GuestType guestType = GuestType.builder()
                .guestTypeId(guestTypeId)
                .guestTypeName("Adults")
                .minAge(18)
                .maxAge(99)
                .build();

        Property property = Property.builder()
                .propertyId(propertyId)
                .propertyName("Radison Mumbai")
                .guestAllowed(2)
                .guestFlag(true)
                .roomCount(20)
                .lengthOfStay(3)
                .roomFlag(false)
                .accessibleFlag(true)
                .guestTypes(List.of(guestType))
                .build();

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(propertyRepository.findByTenant_TenantId(tenantId)).thenReturn(List.of(property));

        ConfigResponseDto response = configService.getConfigByTenant(tenantId);

        assertEquals("Radison", response.getTenantName());
        assertEquals(1, response.getProperties().size());
        assertEquals("Radison Mumbai", response.getProperties().get(0).getPropertyName());
        assertEquals(1, response.getProperties().get(0).getGuestTypes().size());
        assertEquals("Adults", response.getProperties().get(0).getGuestTypes().get(0).getGuestTypeName());
    }

    @Test
    void getConfigByTenantNameFallsBackToContainingMatch() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().tenantId(tenantId).tenantName("Hilton").build();

        when(tenantRepository.findByTenantNameIgnoreCase("hil")).thenReturn(Optional.empty());
        when(tenantRepository.findFirstByTenantNameContainingIgnoreCase("hil")).thenReturn(Optional.of(tenant));
        when(propertyRepository.findByTenant_TenantId(tenantId)).thenReturn(List.of());

        ConfigResponseDto response = configService.getConfigByTenantName("hil");

        assertEquals("Hilton", response.getTenantName());
    }

    @Test
    void getConfigByTenantNameThrowsWhenNotFound() {
        when(tenantRepository.findByTenantNameIgnoreCase("unknown")).thenReturn(Optional.empty());
        when(tenantRepository.findFirstByTenantNameContainingIgnoreCase("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> configService.getConfigByTenantName("unknown"));
    }
}


