package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.TenantSummaryDto;
import com.ibe.ibe_blitz_backend.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TenantGraphqlController {
    private final TenantRepository tenantRepository;

    @QueryMapping
    public List<TenantSummaryDto> tenants() {
        return tenantRepository.findAll().stream()
                .map(t -> TenantSummaryDto.builder()
                        .tenantId(t.getTenantId())
                        .tenantName(t.getTenantName())
                        .build())
                .toList();
    }
}

