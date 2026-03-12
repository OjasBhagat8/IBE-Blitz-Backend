package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.DailyLeastPriceDto;
import com.ibe.ibe_blitz_backend.service.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CalenderGraphQlController {

    private final CalenderService calenderService;

    @QueryMapping
    public List<DailyLeastPriceDto> calendarPrices(
            @Argument UUID tenantId,
            @Argument UUID propertyId,
            @Argument String startDate
    ) {
        LocalDate fromDate = startDate != null && !startDate.isBlank()
                ? LocalDate.parse(startDate)
                : LocalDate.now();

        return calenderService.getLeastPrices(tenantId, propertyId, fromDate);
    }
}

