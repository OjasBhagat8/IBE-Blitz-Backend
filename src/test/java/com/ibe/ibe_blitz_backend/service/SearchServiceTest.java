package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.RoomSearchResponseDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResultDto;
import com.ibe.ibe_blitz_backend.dto.SearchRoomsInputDto;
import com.ibe.ibe_blitz_backend.entities.Prices;
import com.ibe.ibe_blitz_backend.entities.Property;
import com.ibe.ibe_blitz_backend.entities.RoomSpec;
import com.ibe.ibe_blitz_backend.entities.RoomType;
import com.ibe.ibe_blitz_backend.repositories.PriceRepository;
import com.ibe.ibe_blitz_backend.repositories.PropertyRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private PriceRepository priceRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private DynamicFilterService dynamicFilterService;

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
                false,
                null,
                null
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
                false,
                null,
                null
        );
        when(propertyRepository.findByPropertyIdAndTenant_TenantId(any(), any()))
                .thenReturn(Optional.of(searchableProperty(input.getPropertyId(), 5, 3, false)));
        when(priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(List.of());

        RoomSearchResponseDto results = searchService.searchRooms(input);

        assertEquals(0, results.getItems().size());
        assertEquals(0, results.getPage());
        assertEquals(3, results.getSize());
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
                true,
                null,
                null
        );
        when(propertyRepository.findByPropertyIdAndTenant_TenantId(any(), any()))
                .thenReturn(Optional.of(searchableProperty(propertyId, 5, 5, true)));

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

        RoomSearchResponseDto results = searchService.searchRooms(input);

        assertEquals(2, results.getItems().size());
        assertEquals("Deluxe", results.getItems().get(0).getRoomTypeName());
        assertEquals("Room description", results.getItems().get(0).getDescription());
        assertEquals(2, results.getItems().get(0).getOccupancy());
        assertEquals(List.of("Wifi", "Breakfast"), results.getItems().get(0).getAmenities());
        assertEquals(List.of("image1.jpg", "image2.jpg"), results.getItems().get(0).getImages());
        assertEquals(new BigDecimal("100.00"), results.getItems().get(0).getBaseRate());
        assertEquals("King Bed", results.getItems().get(0).getRoomSpec().getBedType());
        assertEquals(new BigDecimal("320.00"), results.getItems().get(0).getRoomSpec().getArea());
        assertEquals(new BigDecimal("420.00"), results.getItems().get(0).getTotalPrice());
        assertEquals(2, results.getItems().get(0).getAvailableCount());
        assertEquals("Suite", results.getItems().get(1).getRoomTypeName());
        assertEquals(new BigDecimal("840.00"), results.getItems().get(1).getTotalPrice());
    }

    @Test
    void searchRoomsReturnsEmptyWhenAccessibleRequestedForNonAccessibleProperty() {
        SearchRoomsInputDto input = new SearchRoomsInputDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                1,
                true,
                null,
                null
        );
        when(propertyRepository.findByPropertyIdAndTenant_TenantId(any(), any()))
                .thenReturn(Optional.of(searchableProperty(input.getPropertyId(), 5, 3, false)));

        RoomSearchResponseDto results = searchService.searchRooms(input);

        assertEquals(0, results.getItems().size());
    }

    @Test
    void searchRoomsThrowsWhenStayExceedsPropertyLimit() {
        SearchRoomsInputDto input = new SearchRoomsInputDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15),
                1,
                false,
                null,
                null
        );
        when(propertyRepository.findByPropertyIdAndTenant_TenantId(any(), any()))
                .thenReturn(Optional.of(searchableProperty(input.getPropertyId(), 5, 2, false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> searchService.searchRooms(input));

        assertEquals("stay exceeds property lengthOfStay", ex.getMessage());
    }

    @Test
    void searchRoomsThrowsWhenRequestedRoomsExceedPropertyLimit() {
        SearchRoomsInputDto input = new SearchRoomsInputDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                4,
                false,
                null,
                null
        );
        when(propertyRepository.findByPropertyIdAndTenant_TenantId(any(), any()))
                .thenReturn(Optional.of(searchableProperty(input.getPropertyId(), 3, 5, false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> searchService.searchRooms(input));

        assertEquals("rooms exceeds property roomCount", ex.getMessage());
    }

    @Test
    void searchRoomsCapsRequestedPageSizeAtThree() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.of(2026, 4, 1);
        LocalDate checkOut = LocalDate.of(2026, 4, 3);
        SearchRoomsInputDto input = new SearchRoomsInputDto(
                tenantId,
                propertyId,
                checkIn,
                checkOut,
                1,
                false,
                0,
                10
        );

        when(propertyRepository.findByPropertyIdAndTenant_TenantId(any(), any()))
                .thenReturn(Optional.of(searchableProperty(propertyId, 5, 5, false)));

        RoomType roomTypeOne = roomType(UUID.randomUUID(), "Budget");
        RoomType roomTypeTwo = roomType(UUID.randomUUID(), "Deluxe");
        RoomType roomTypeThree = roomType(UUID.randomUUID(), "Suite");
        RoomType roomTypeFour = roomType(UUID.randomUUID(), "Premium");

        List<Prices> priceRows = List.of(
                priceRow(roomTypeOne, propertyId, checkIn, "100.00", 2),
                priceRow(roomTypeOne, propertyId, checkIn.plusDays(1), "100.00", 2),
                priceRow(roomTypeTwo, propertyId, checkIn, "120.00", 2),
                priceRow(roomTypeTwo, propertyId, checkIn.plusDays(1), "120.00", 2),
                priceRow(roomTypeThree, propertyId, checkIn, "140.00", 2),
                priceRow(roomTypeThree, propertyId, checkIn.plusDays(1), "140.00", 2),
                priceRow(roomTypeFour, propertyId, checkIn, "160.00", 2),
                priceRow(roomTypeFour, propertyId, checkIn.plusDays(1), "160.00", 2)
        );

        when(priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(priceRows);

        RoomSearchResponseDto results = searchService.searchRooms(input);

        assertEquals(3, results.getSize());
        assertEquals(4, results.getTotalItems());
        assertEquals(2, results.getTotalPages());
        assertEquals(3, results.getItems().size());
        assertEquals(true, results.getHasNext());
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
                .description("Room description")
                .occupancy(2)
                .amenities(List.of("Wifi", "Breakfast"))
                .images(List.of("image1.jpg", "image2.jpg"))
                .baseRate(new BigDecimal("100.00"))
                .roomSpec(RoomSpec.builder()
                        .roomSpecId(UUID.randomUUID())
                        .bedType("King Bed")
                        .area(new BigDecimal("320.00"))
                        .minOcc(1)
                        .maxOcc(2)
                        .build())
                .build();
    }

    private static Property searchableProperty(UUID propertyId, int roomCount, int lengthOfStay, boolean accessibleFlag) {
        return Property.builder()
                .propertyId(propertyId)
                .roomCount(roomCount)
                .lengthOfStay(lengthOfStay)
                .accessibleFlag(accessibleFlag)
                .build();
    }
}


