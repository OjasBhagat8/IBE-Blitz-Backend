package com.example.ibe_blits_backend.service;

import com.example.ibe_blits_backend.dto.DailyLeastPriceDto;
import com.example.ibe_blits_backend.entities.Prices;
import com.example.ibe_blits_backend.entities.Property;
import com.example.ibe_blits_backend.entities.RoomType;
import com.example.ibe_blits_backend.repositories.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalenderServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private CalenderService calenderService;

    @Test
    void getLeastPricesReturnsMinimumPriceForEachDayInWindow() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID cheaperRoomType = UUID.randomUUID();
        UUID expensiveRoomType = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 1, 1);

        when(priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDate(
                any(), any(), any()
        )).thenAnswer(invocation -> {
            Date argDate = invocation.getArgument(2);
            LocalDate date = argDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return List.of(
                    price(cheaperRoomType, propertyId, date, new BigDecimal("100.00")),
                    price(expensiveRoomType, propertyId, date, new BigDecimal("150.00"))
            );
        });

        List<DailyLeastPriceDto> result = calenderService.getLeastPrices(tenantId, propertyId, start);

        int expectedDays = (int) start.datesUntil(start.plusMonths(2).plusDays(1)).count();
        assertEquals(expectedDays, result.size());
        assertEquals(cheaperRoomType, result.get(0).getRoomTypeId());
        assertEquals(new BigDecimal("100.00"), result.get(0).getPrice());
    }

    @Test
    void getLeastPricesSkipsDaysWithNoPriceRows() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 2, 1);

        when(priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDate(
                any(), any(), any()
        )).thenReturn(List.of());

        List<DailyLeastPriceDto> result = calenderService.getLeastPrices(tenantId, propertyId, start);

        assertEquals(0, result.size());
    }

    private static Prices price(UUID roomTypeId, UUID propertyId, LocalDate date, BigDecimal roomPrice) {
        return Prices.builder()
                .roomType(RoomType.builder().roomTypeId(roomTypeId).build())
                .property(Property.builder().propertyId(propertyId).build())
                .date(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .roomPrice(roomPrice)
                .quantity(2)
                .build();
    }
}

