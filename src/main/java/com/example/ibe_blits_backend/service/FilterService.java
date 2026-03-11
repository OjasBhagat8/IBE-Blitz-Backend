package com.example.ibe_blits_backend.service;
import com.example.ibe_blits_backend.dto.FilterOptionDto;
import com.example.ibe_blits_backend.dto.RoomFilterDto;
import com.example.ibe_blits_backend.entities.FilterConfig;
import com.example.ibe_blits_backend.entities.FilterOptions;
import com.example.ibe_blits_backend.entities.Filters;
import com.example.ibe_blits_backend.repositories.FilterConfigRepository;
import com.example.ibe_blits_backend.repositories.FilterOptionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilterService {
    private final FilterConfigRepository filterConfigRepository;
    private final FilterOptionsRepository filterOptionsRepository;

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
}
