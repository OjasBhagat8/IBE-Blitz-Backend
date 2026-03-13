package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.DynamicFilterOptionDto;
import com.ibe.ibe_blitz_backend.dto.DynamicRoomFilterDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResultDto;
import com.ibe.ibe_blitz_backend.dto.RoomSpecSummaryDto;
import com.ibe.ibe_blitz_backend.dto.SelectedFilterInputDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DynamicFilterService {

    public List<DynamicRoomFilterDto> buildFilters(List<RoomSearchResultDto> rooms) {
        List<DynamicRoomFilterDto> filters = new ArrayList<>();

        addCheckboxFilter(filters, "amenities", rooms, RoomSearchResultDto::getAmenities);
        addCheckboxFilter(filters, "bedType", rooms, room -> {
            if (room.getRoomSpec() == null || room.getRoomSpec().getBedType() == null) {
                return null;
            }
            return List.of(room.getRoomSpec().getBedType());
        });
        addRangeFilter(filters, "area", rooms, room -> room.getRoomSpec() == null ? null : room.getRoomSpec().getArea());
        addRangeFilter(filters, "occupancy", rooms, room -> room.getOccupancy() == null ? null : BigDecimal.valueOf(room.getOccupancy()));
        addRangeFilter(filters, "baseRate", rooms, RoomSearchResultDto::getBaseRate);

        return filters;
    }

    public List<RoomSearchResultDto> applyFilters(List<RoomSearchResultDto> rooms, List<SelectedFilterInputDto> filters) {
        if (filters == null || filters.isEmpty()) {
            return rooms;
        }

        return rooms.stream()
                .filter(room -> matchesAll(room, filters))
                .toList();
    }

    private boolean matchesAll(RoomSearchResultDto room, List<SelectedFilterInputDto> filters) {
        for (SelectedFilterInputDto filter : filters) {
            if (filter == null || filter.getFilterName() == null || filter.getFilterName().isBlank()) {
                continue;
            }

            String key = normalize(filter.getFilterName());
            boolean matches = switch (key) {
                case "amenities", "amenity" -> matchesCheckbox(room.getAmenities(), filter.getOptions());
                case "bedtype" -> matchesBedType(room.getRoomSpec(), filter.getOptions());
                case "area" -> matchesRange(room.getRoomSpec() == null ? null : room.getRoomSpec().getArea(), filter);
                case "occupancy" -> matchesRange(room.getOccupancy() == null ? null : BigDecimal.valueOf(room.getOccupancy()), filter);
                case "baserate", "price" -> matchesRange(room.getBaseRate(), filter);
                default -> true;
            };

            if (!matches) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesBedType(RoomSpecSummaryDto roomSpec, List<String> options) {
        if (roomSpec == null || roomSpec.getBedType() == null) {
            return false;
        }
        return matchesCheckbox(List.of(roomSpec.getBedType()), options);
    }

    private boolean matchesCheckbox(List<String> values, List<String> options) {
        if (values == null || values.isEmpty() || options == null || options.isEmpty()) {
            return options == null || options.isEmpty();
        }

        List<String> normalizedOptions = options.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(option -> !option.isBlank())
                .toList();

        if (normalizedOptions.isEmpty()) {
            return true;
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .anyMatch(normalizedOptions::contains);
    }

    private boolean matchesRange(BigDecimal actualValue, SelectedFilterInputDto filter) {
        if (actualValue == null) {
            return false;
        }
        if (filter.getMinValue() != null && actualValue.compareTo(filter.getMinValue()) < 0) {
            return false;
        }
        if (filter.getMaxValue() != null && actualValue.compareTo(filter.getMaxValue()) > 0) {
            return false;
        }
        return true;
    }

    private void addCheckboxFilter(
            List<DynamicRoomFilterDto> filters,
            String filterKey,
            List<RoomSearchResultDto> rooms,
            Function<RoomSearchResultDto, List<String>> extractor
    ) {
        Map<String, Long> counts = rooms.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        if (counts.isEmpty()) {
            return;
        }

        List<DynamicFilterOptionDto> options = counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> DynamicFilterOptionDto.builder()
                        .value(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .toList();

        filters.add(DynamicRoomFilterDto.builder()
                .filterKey(filterKey)
                .filterType("CHECKBOX")
                .options(options)
                .build());
    }

    private void addRangeFilter(
            List<DynamicRoomFilterDto> filters,
            String filterKey,
            List<RoomSearchResultDto> rooms,
            Function<RoomSearchResultDto, BigDecimal> extractor
    ) {
        List<BigDecimal> values = rooms.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return;
        }

        BigDecimal minValue = values.stream().min(Comparator.naturalOrder()).orElse(null);
        BigDecimal maxValue = values.stream().max(Comparator.naturalOrder()).orElse(null);

        filters.add(DynamicRoomFilterDto.builder()
                .filterKey(filterKey)
                .filterType("RANGE")
                .options(List.of())
                .minValue(minValue)
                .maxValue(maxValue)
                .build());
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-]+", "");
    }
}
