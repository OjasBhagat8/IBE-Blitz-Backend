package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.TenantSummaryDto;
import com.ibe.ibe_blitz_backend.entities.Tenant;
import com.ibe.ibe_blitz_backend.repositories.TenantRepository;
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
class TenantGraphqlControllerTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantGraphqlController controller;

    @Test
    void tenantsMapsRepositoryResult() {
        Tenant t1 = Tenant.builder().tenantId(UUID.randomUUID()).tenantName("Radison").build();
        Tenant t2 = Tenant.builder().tenantId(UUID.randomUUID()).tenantName("Hilton").build();
        when(tenantRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TenantSummaryDto> result = controller.tenants();

        assertEquals(2, result.size());
        assertEquals("Radison", result.get(0).getTenantName());
        assertEquals("Hilton", result.get(1).getTenantName());
    }
}


