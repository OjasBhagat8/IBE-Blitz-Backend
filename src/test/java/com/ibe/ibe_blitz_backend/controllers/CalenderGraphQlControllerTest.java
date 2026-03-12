package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.DailyLeastPriceDto;
import com.ibe.ibe_blitz_backend.service.CalenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalenderGraphQlControllerTest {

    @Mock
    private CalenderService calenderService;

    @InjectMocks
    private CalenderGraphQlController controller;

    @Test
    void calendarPricesParsesStartDateAndDelegatesToService() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 1, 10);
        DailyLeastPriceDto dto = new DailyLeastPriceDto(startDate, propertyId, UUID.randomUUID(), null);

        when(calenderService.getLeastPrices(tenantId, propertyId, startDate)).thenReturn(List.of(dto));

        List<DailyLeastPriceDto> result = controller.calendarPrices(tenantId, propertyId, "2026-01-10");

        assertEquals(1, result.size());
        assertEquals(startDate, result.get(0).getDate());
    }
}


