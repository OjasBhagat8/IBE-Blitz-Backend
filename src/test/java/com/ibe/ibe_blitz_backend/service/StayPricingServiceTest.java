package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.entities.Prices;
import com.ibe.ibe_blitz_backend.exceptions.NotFoundException;
import com.ibe.ibe_blitz_backend.repositories.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StayPricingServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private StayPricingService service;

    @Test
    void calculateStayTotalSumsAllStayDates() {
        UUID propertyId = UUID.randomUUID();
        UUID roomTypeId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.of(2026, 3, 20);
        LocalDate checkOut = LocalDate.of(2026, 3, 22);

        when(priceRepository.findByPropertyIdAndRoomTypeIdAndDateBetweenOrderByDateAsc(eq(propertyId), eq(roomTypeId), any(Date.class), any(Date.class)))
                .thenReturn(List.of(
                        price(checkIn, "100.00"),
                        price(checkIn.plusDays(1), "125.50")
                ));

        assertEquals(new BigDecimal("225.50"), service.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut));
    }

    @Test
    void calculateStayTotalSupportsSqlDates() {
        UUID propertyId = UUID.randomUUID();
        UUID roomTypeId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.of(2026, 4, 1);
        LocalDate checkOut = LocalDate.of(2026, 4, 3);

        when(priceRepository.findByPropertyIdAndRoomTypeIdAndDateBetweenOrderByDateAsc(eq(propertyId), eq(roomTypeId), any(Date.class), any(Date.class)))
                .thenReturn(List.of(
                        Prices.builder().date(java.sql.Date.valueOf(checkIn)).roomPrice(new BigDecimal("80.00")).build(),
                        Prices.builder().date(java.sql.Date.valueOf(checkIn.plusDays(1))).roomPrice(new BigDecimal("90.00")).build()
                ));

        assertEquals(new BigDecimal("170.00"), service.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut));
    }

    @Test
    void calculateStayTotalThrowsWhenDateIsMissing() {
        UUID propertyId = UUID.randomUUID();
        UUID roomTypeId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.of(2026, 5, 1);
        LocalDate checkOut = LocalDate.of(2026, 5, 3);

        when(priceRepository.findByPropertyIdAndRoomTypeIdAndDateBetweenOrderByDateAsc(eq(propertyId), eq(roomTypeId), any(Date.class), any(Date.class)))
                .thenReturn(List.of(price(checkIn, "100.00")));

        assertThrows(NotFoundException.class, () -> service.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut));
    }

    @Test
    void calculateStayTotalThrowsWhenRoomPriceIsMissing() {
        UUID propertyId = UUID.randomUUID();
        UUID roomTypeId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.of(2026, 6, 1);
        LocalDate checkOut = LocalDate.of(2026, 6, 2);

        when(priceRepository.findByPropertyIdAndRoomTypeIdAndDateBetweenOrderByDateAsc(eq(propertyId), eq(roomTypeId), any(Date.class), any(Date.class)))
                .thenReturn(List.of(Prices.builder().date(java.sql.Date.valueOf(checkIn)).roomPrice(null).build()));

        assertThrows(NotFoundException.class, () -> service.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut));
    }

    private Prices price(LocalDate localDate, String amount) {
        return Prices.builder()
                .date(java.sql.Date.valueOf(localDate))
                .roomPrice(new BigDecimal(amount))
                .build();
    }
}
