package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.DynamicRoomFilterDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResultDto;
import com.ibe.ibe_blitz_backend.dto.RoomSpecSummaryDto;
import com.ibe.ibe_blitz_backend.dto.SelectedFilterInputDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DynamicFilterServiceTest {

    private final DynamicFilterService service = new DynamicFilterService();

    @Test
    void buildFiltersCreatesCheckboxAndRangeFilters() {
        List<RoomSearchResultDto> rooms = List.of(
                room("King Bed", new BigDecimal("320"), 2, List.of("Wifi", "Breakfast"), new BigDecimal("60")),
                room("Twin Bed", new BigDecimal("420"), 4, List.of("Wifi"), new BigDecimal("80")),
                room(null, new BigDecimal("280"), 1, List.of("Pool"), null)
        );

        List<DynamicRoomFilterDto> filters = service.buildFilters(rooms);

        assertEquals(5, filters.size());
        assertEquals("amenities", filters.get(0).getFilterKey());
        assertEquals(3, filters.get(0).getOptions().size());
        assertEquals("bedType", filters.get(1).getFilterKey());
        assertEquals(2, filters.get(1).getOptions().size());
        assertEquals(new BigDecimal("60"), filters.get(2).getMinValue());
        assertEquals(new BigDecimal("80"), filters.get(2).getMaxValue());
        assertEquals(new BigDecimal("1"), filters.get(3).getMinValue());
        assertEquals(new BigDecimal("4"), filters.get(3).getMaxValue());
        assertEquals(new BigDecimal("280"), filters.get(4).getMinValue());
        assertEquals(new BigDecimal("420"), filters.get(4).getMaxValue());
    }

    @Test
    void applyFiltersReturnsOriginalRoomsWhenFiltersAreMissing() {
        List<RoomSearchResultDto> rooms = List.of(room("King Bed", new BigDecimal("320"), 2, List.of("Wifi"), new BigDecimal("60")));

        assertSame(rooms, service.applyFilters(rooms, null));
        assertSame(rooms, service.applyFilters(rooms, List.of()));
    }

    @Test
    void applyFiltersSupportsCheckboxRangeAndUnknownFilters() {
        RoomSearchResultDto matchingRoom = room("King Bed", new BigDecimal("320"), 2, List.of("Wifi", "Breakfast"), new BigDecimal("60"));
        RoomSearchResultDto secondRoom = room("Twin Bed", new BigDecimal("420"), 4, List.of("Pool"), new BigDecimal("80"));
        List<RoomSearchResultDto> rooms = List.of(matchingRoom, secondRoom);

        List<SelectedFilterInputDto> filters = List.of(
                SelectedFilterInputDto.builder().filterName("Amenities").options(List.of("wifi")).build(),
                SelectedFilterInputDto.builder().filterName("bed_type").options(List.of(" king bed ")).build(),
                SelectedFilterInputDto.builder().filterName("area").minValue(new BigDecimal("50")).maxValue(new BigDecimal("70")).build(),
                SelectedFilterInputDto.builder().filterName("occupancy").minValue(new BigDecimal("2")).maxValue(new BigDecimal("2")).build(),
                SelectedFilterInputDto.builder().filterName("price").maxValue(new BigDecimal("350")).build(),
                SelectedFilterInputDto.builder().filterName("unknown").options(List.of("ignored")).build()
        );

        assertIterableEquals(List.of(matchingRoom), service.applyFilters(rooms, filters));
    }

    @Test
    void applyFiltersHandlesBlankNamesEmptyOptionsAndOutOfRangeValues() {
        RoomSearchResultDto roomWithoutSpec = RoomSearchResultDto.builder()
                .amenities(List.of())
                .baseRate(new BigDecimal("200"))
                .occupancy(2)
                .build();
        RoomSearchResultDto roomWithSpec = room("Queen Bed", new BigDecimal("300"), 3, List.of("Spa"), new BigDecimal("55"));
        List<RoomSearchResultDto> rooms = List.of(roomWithoutSpec, roomWithSpec);

        List<SelectedFilterInputDto> blankFilter = List.of(
                SelectedFilterInputDto.builder().filterName(" ").options(List.of("ignored")).build(),
                SelectedFilterInputDto.builder().filterName("amenity").options(List.of()).build()
        );
        List<SelectedFilterInputDto> filtersThatFail = List.of(
                SelectedFilterInputDto.builder().filterName("bedType").options(List.of("Queen Bed")).build(),
                SelectedFilterInputDto.builder().filterName("baseRate").minValue(new BigDecimal("500")).build()
        );

        assertIterableEquals(rooms, service.applyFilters(rooms, blankFilter));
        assertEquals(List.of(), service.applyFilters(List.of(roomWithoutSpec), filtersThatFail));
    }

    private RoomSearchResultDto room(
            String bedType,
            BigDecimal baseRate,
            Integer occupancy,
            List<String> amenities,
            BigDecimal area
    ) {
        return RoomSearchResultDto.builder()
                .roomSpec(area == null && bedType == null ? null : RoomSpecSummaryDto.builder()
                        .bedType(bedType)
                        .area(area)
                        .build())
                .baseRate(baseRate)
                .occupancy(occupancy)
                .amenities(amenities)
                .build();
    }
}
