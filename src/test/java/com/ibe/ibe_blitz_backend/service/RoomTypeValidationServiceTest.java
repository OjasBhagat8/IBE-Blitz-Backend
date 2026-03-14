package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.entities.Promotion;
import com.ibe.ibe_blitz_backend.entities.PromotionRoomType;
import com.ibe.ibe_blitz_backend.entities.RoomType;
import com.ibe.ibe_blitz_backend.exceptions.NotFoundException;
import com.ibe.ibe_blitz_backend.repositories.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomTypeValidationServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private RoomTypeValidationService service;

    @Test
    void getRoomTypeReturnsMatchingRoomType() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        RoomType roomType = RoomType.builder().roomTypeId(roomTypeId).build();

        when(roomTypeRepository.findByRoomTypeIdAndProperty_PropertyId(roomTypeId, propertyId))
                .thenReturn(Optional.of(roomType));

        assertEquals(roomType, service.getRoomType(roomTypeId, propertyId));
    }

    @Test
    void getRoomTypeThrowsWhenRoomTypeDoesNotBelongToProperty() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        when(roomTypeRepository.findByRoomTypeIdAndProperty_PropertyId(roomTypeId, propertyId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getRoomType(roomTypeId, propertyId));
    }

    @Test
    void appliesToRoomTypeReturnsTrueWhenMappingsAreMissingOrMatching() {
        UUID roomTypeId = UUID.randomUUID();
        Promotion emptyMappings = Promotion.builder().roomTypes(List.of()).build();
        Promotion matching = Promotion.builder()
                .roomTypes(List.of(PromotionRoomType.builder().roomType(RoomType.builder().roomTypeId(roomTypeId).build()).build()))
                .build();

        assertTrue(service.appliesToRoomType(emptyMappings, roomTypeId));
        assertTrue(service.appliesToRoomType(matching, roomTypeId));
    }

    @Test
    void appliesToRoomTypeReturnsFalseWhenMappingsDoNotMatch() {
        UUID roomTypeId = UUID.randomUUID();
        Promotion promotion = Promotion.builder()
                .roomTypes(List.of(
                        PromotionRoomType.builder().roomType(null).build(),
                        PromotionRoomType.builder().roomType(RoomType.builder().roomTypeId(UUID.randomUUID()).build()).build()
                ))
                .build();

        assertFalse(service.appliesToRoomType(promotion, roomTypeId));
    }

    @Test
    void isWithinPromotionWindowRespectsStartAndEndDates() {
        LocalDate checkIn = LocalDate.of(2026, 7, 10);
        LocalDate checkOut = LocalDate.of(2026, 7, 12);

        Promotion withinWindow = Promotion.builder()
                .startDate(LocalDate.of(2026, 7, 9))
                .endDate(LocalDate.of(2026, 7, 11))
                .build();
        Promotion beforeWindow = Promotion.builder()
                .startDate(LocalDate.of(2026, 7, 11))
                .endDate(LocalDate.of(2026, 7, 20))
                .build();
        Promotion afterWindow = Promotion.builder()
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 10))
                .build();
        Promotion openEnded = Promotion.builder()
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(null)
                .build();

        assertTrue(service.isWithinPromotionWindow(withinWindow, checkIn, checkOut));
        assertFalse(service.isWithinPromotionWindow(beforeWindow, checkIn, checkOut));
        assertFalse(service.isWithinPromotionWindow(afterWindow, checkIn, checkOut));
        assertTrue(service.isWithinPromotionWindow(openEnded, checkIn, checkOut));
    }
}
