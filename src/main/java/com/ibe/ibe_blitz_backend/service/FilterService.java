package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.FilterOptionDto;
import com.ibe.ibe_blitz_backend.dto.FilterRoomResultsInputDto;
import com.ibe.ibe_blitz_backend.dto.RoomFilterDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResultDto;
import com.ibe.ibe_blitz_backend.dto.RoomSpecSummaryDto;
import com.ibe.ibe_blitz_backend.dto.SearchRoomsInputDto;
import com.ibe.ibe_blitz_backend.dto.SelectedFilterInputDto;
import com.ibe.ibe_blitz_backend.entities.FilterConfig;
import com.ibe.ibe_blitz_backend.entities.FilterOptions;
import com.ibe.ibe_blitz_backend.entities.Filters;
import com.ibe.ibe_blitz_backend.repositories.FilterConfigRepository;
import com.ibe.ibe_blitz_backend.repositories.FilterOptionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilterService {
    private final FilterConfigRepository filterConfigRepository;
    private final FilterOptionsRepository filterOptionsRepository;
    private final SearchService searchService;

    @Transactional(readOnly = true)
    public List<RoomFilterDto> getPropertyFilters(UUID propertyId) {
        if (propertyId == null) {
            throw new IllegalArgumentException("propertyId is required");
        }

        FilterConfig config = filterConfigRepository.findDetailedByProperty_PropertyId(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("filters not found for property"));

        List<UUID> filterIds = config.getFilters().stream()
                .map(Filters::getFilterId)
                .toList();

        Map<UUID, List<FilterOptions>> optionsByFilterId = filterOptionsRepository.findByFilter_FilterIdIn(filterIds).stream()
                .collect(Collectors.groupingBy(option -> option.getFilter().getFilterId()));

        return config.getFilters().stream()
                .sorted(Comparator.comparing(Filters::getFilterName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(filter -> RoomFilterDto.builder()
                        .filterId(filter.getFilterId())
                        .filterName(filter.getFilterName())
                        .options(optionsByFilterId.getOrDefault(filter.getFilterId(), List.of()).stream()
                                .sorted(Comparator.comparing(FilterOptions::getValue, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(option -> FilterOptionDto.builder()
                                        .optionId(option.getOptionId())
                                        .value(option.getValue())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomSearchResultDto> filterRoomResults(FilterRoomResultsInputDto input) {
        validateFilterInput(input);

        SearchRoomsInputDto searchInput = SearchRoomsInputDto.builder()
                .tenantId(input.getTenantId())
                .propertyId(input.getPropertyId())
                .checkIn(input.getCheckIn())
                .checkOut(input.getCheckOut())
                .rooms(input.getRooms())
                .accessible(input.getAccessible())
                .build();

        return searchService.searchRooms(searchInput).stream()
                .filter(room -> matchesAllSelectedFilters(room, input.getFilters()))
                .toList();
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

    private boolean matchesAllSelectedFilters(RoomSearchResultDto room, List<SelectedFilterInputDto> filters) {
        for (SelectedFilterInputDto filter : filters) {
            if (filter == null || filter.getFilterName() == null || filter.getOptions() == null || filter.getOptions().isEmpty()) {
                continue;
            }

            String normalizedName = normalize(filter.getFilterName());
            List<String> options = filter.getOptions().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();

            if (options.isEmpty()) {
                continue;
            }

            boolean matched = switch (normalizedName) {
                case "bedtype" -> matchesBedType(room.getRoomSpec(), options);
                case "area" -> matchesArea(room.getRoomSpec(), options);
                case "amenities", "amenity" -> matchesAmenities(room, options);
                default -> true;
            };

            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesBedType(RoomSpecSummaryDto roomSpec, List<String> options) {
        if (roomSpec == null || roomSpec.getBedType() == null) {
            return false;
        }
        String actual = normalize(roomSpec.getBedType());
        return options.stream().map(this::normalize).anyMatch(actual::equals);
    }

    private boolean matchesArea(RoomSpecSummaryDto roomSpec, List<String> options) {
        if (roomSpec == null || roomSpec.getArea() == null) {
            return false;
        }
        Set<BigDecimal> allowed = new HashSet<>();
        for (String option : options) {
            try {
                allowed.add(new BigDecimal(option.trim()).stripTrailingZeros());
            } catch (NumberFormatException ignored) {
                // Ignore invalid frontend option values instead of failing the whole request.
            }
        }
        return !allowed.isEmpty() && allowed.contains(roomSpec.getArea().stripTrailingZeros());
    }

    private boolean matchesAmenities(RoomSearchResultDto room, List<String> options) {
        if (room.getAmenities() == null || room.getAmenities().isEmpty()) {
            return false;
        }
        Set<String> available = room.getAmenities().stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .collect(Collectors.toSet());
        return options.stream().map(this::normalize).anyMatch(available::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-]+", "");
    }
}
