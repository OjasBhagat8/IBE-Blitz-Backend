package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.FilterRoomResultsInputDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResponseDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResultDto;
import com.ibe.ibe_blitz_backend.dto.SearchRoomsInputDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterService {
    private final SearchService searchService;
    private final DynamicFilterService dynamicFilterService;

    @Transactional(readOnly = true)
    public RoomSearchResponseDto filterRoomResults(FilterRoomResultsInputDto input) {
        validateFilterInput(input);
        SearchRoomsInputDto searchInput = toSearchInput(input);
        List<RoomSearchResultDto> availableRooms = searchService.findAvailableRooms(searchInput);
        List<RoomSearchResultDto> filteredRooms = dynamicFilterService.applyFilters(availableRooms, input.getFilters());
        return searchService.buildSearchResponse(filteredRooms, searchInput);
    }

    private void validateFilterInput(FilterRoomResultsInputDto input) {
        if (input == null || input.getTenantId() == null || input.getPropertyId() == null) {
            throw new IllegalArgumentException("tenantId and propertyId are required");
        }
        if (input.getCheckIn() == null || input.getCheckOut() == null || !input.getCheckOut().isAfter(input.getCheckIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        if (input.getRooms() == null || input.getRooms() <= 0) {
            throw new IllegalArgumentException("rooms must be greater than zero");
        }
        if (input.getFilters() == null) {
            throw new IllegalArgumentException("filters are required");
        }
    }

    private SearchRoomsInputDto toSearchInput(FilterRoomResultsInputDto input) {
        return SearchRoomsInputDto.builder()
                .tenantId(input.getTenantId())
                .propertyId(input.getPropertyId())
                .checkIn(input.getCheckIn())
                .checkOut(input.getCheckOut())
                .rooms(input.getRooms())
                .accessible(input.getAccessible())
                .page(input.getPage())
                .size(input.getSize())
                .build();
    }
}
