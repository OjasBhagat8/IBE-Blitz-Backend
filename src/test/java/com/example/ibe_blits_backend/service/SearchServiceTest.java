package com.example.ibe_blits_backend.service;

import com.example.ibe_blits_backend.dto.RoomSearchResultDto;
import com.example.ibe_blits_backend.dto.SearchRoomsInputDto;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchRoomsThrowsWhenTenantMissing() {
        SearchRoomsInputDto input = new SearchRoomsInputDto(
                null,
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                1,
                false
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> searchService.searchRooms(input));
        assertEquals("tenantId and propertyId are required", ex.getMessage());
    }

    @Test
    void searchRoomsReturnsEmptyWhenNoRows() {
        SearchRoomsInputDto input = new SearchRoomsInputDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                1,
                false
        );
        when(priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<RoomSearchResultDto> results = searchService.searchRooms(input);

        assertEquals(0, results.size());
    }

    @Test
    void searchRoomsReturnsSortedAndFiltersIncompleteOrInsufficientInventory() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID cheapRoomTypeId = UUID.randomUUID();
        UUID expensiveRoomTypeId = UUID.randomUUID();
        UUID incompleteRoomTypeId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.of(2026, 4, 1);
        LocalDate checkOut = LocalDate.of(2026, 4, 3); // 2 nights

        SearchRoomsInputDto input = new SearchRoomsInputDto(
                tenantId,
                propertyId,
                checkIn,
                checkOut,
                2,
                true
        );

        RoomType cheapType = roomType(cheapRoomTypeId, "Deluxe");
        RoomType expensiveType = roomType(expensiveRoomTypeId, "Suite");
        RoomType incompleteType = roomType(incompleteRoomTypeId, "Single");

        List<Prices> priceRows = List.of(
                priceRow(cheapType, propertyId, checkIn, "100.00", 3),
                priceRow(cheapType, propertyId, checkIn.plusDays(1), "110.00", 2),
                priceRow(expensiveType, propertyId, checkIn, "200.00", 4),
                priceRow(expensiveType, propertyId, checkIn.plusDays(1), "220.00", 4),
                priceRow(incompleteType, propertyId, checkIn, "80.00", 5)
        );

        when(priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(priceRows);

        List<RoomSearchResultDto> results = searchService.searchRooms(input);

        assertEquals(2, results.size());
        assertEquals("Deluxe", results.get(0).getRoomTypeName());
        assertEquals(new BigDecimal("420.00"), results.get(0).getTotalPrice());
        assertEquals(2, results.get(0).getAvailableCount());
        assertEquals("Suite", results.get(1).getRoomTypeName());
        assertEquals(new BigDecimal("840.00"), results.get(1).getTotalPrice());
    }

    private static Prices priceRow(RoomType roomType, UUID propertyId, LocalDate date, String amount, int quantity) {
        return Prices.builder()
                .roomType(roomType)
                .property(Property.builder().propertyId(propertyId).build())
                .date(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .roomPrice(new BigDecimal(amount))
                .quantity(quantity)
                .build();
    }

    private static RoomType roomType(UUID id, String name) {
        return RoomType.builder()
                .roomTypeId(id)
                .roomTypeName(name)
                .build();
    }
}

