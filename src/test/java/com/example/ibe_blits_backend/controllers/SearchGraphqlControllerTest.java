package com.example.ibe_blits_backend.controllers;

import com.example.ibe_blits_backend.dto.RoomSearchResultDto;
import com.example.ibe_blits_backend.dto.SearchRoomsInputDto;
import com.example.ibe_blits_backend.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchGraphqlControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchGraphqlController controller;

    @Test
    void searchRoomsDelegatesToService() {
        SearchRoomsInputDto input = new SearchRoomsInputDto();
        RoomSearchResultDto dto = RoomSearchResultDto.builder()
                .roomTypeId(UUID.randomUUID())
                .roomTypeName("Suite")
                .description("Premium suite")
                .occupancy(3)
                .amenities(List.of("Wifi", "Mini Bar"))
                .images(List.of("image1.jpg", "image2.jpg"))
                .baseRate(new BigDecimal("250.00"))
                .totalPrice(new BigDecimal("500.00"))
                .availableCount(3)
                .build();

        when(searchService.searchRooms(input)).thenReturn(List.of(dto));

        List<RoomSearchResultDto> result = controller.searchRooms(input);

        assertEquals(1, result.size());
        assertEquals("Suite", result.get(0).getRoomTypeName());
    }
}

