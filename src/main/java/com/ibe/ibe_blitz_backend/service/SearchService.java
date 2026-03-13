package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.RoomSearchResponseDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResultDto;
import com.ibe.ibe_blitz_backend.dto.RoomSortBy;
import com.ibe.ibe_blitz_backend.dto.RoomSpecSummaryDto;
import com.ibe.ibe_blitz_backend.dto.SearchRoomsInputDto;
import com.ibe.ibe_blitz_backend.dto.SelectedFilterInputDto;
import com.ibe.ibe_blitz_backend.dto.SortDirection;
import com.ibe.ibe_blitz_backend.dto.DynamicRoomFilterDto;
import com.ibe.ibe_blitz_backend.entities.Prices;
import com.ibe.ibe_blitz_backend.entities.Property;
import com.ibe.ibe_blitz_backend.entities.RoomSpec;
import com.ibe.ibe_blitz_backend.repositories.PriceRepository;
import com.ibe.ibe_blitz_backend.repositories.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final PriceRepository priceRepository;
    private final PropertyRepository propertyRepository;
    private final DynamicFilterService dynamicFilterService;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 3;
    private static final int MAX_SIZE = 3;
    private static final RoomSortBy DEFAULT_SORT_BY = RoomSortBy.TOTAL_PRICE;
    private static final SortDirection DEFAULT_SORT_DIRECTION = SortDirection.ASC;

    public RoomSearchResponseDto searchRooms(SearchRoomsInputDto input) {
        validate(input);
        Property property = propertyRepository.findByPropertyIdAndTenant_TenantId(input.getPropertyId(), input.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("property not found for tenant"));

        long stayNights = input.getCheckIn().datesUntil(input.getCheckOut()).count();
        if (property.getLengthOfStay() != null && stayNights > property.getLengthOfStay()) {
            throw new IllegalArgumentException("stay exceeds property lengthOfStay");
        }
        if (property.getRoomCount() != null && input.getRooms() > property.getRoomCount()) {
            throw new IllegalArgumentException("rooms exceeds property roomCount");
        }
        if (Boolean.TRUE.equals(input.getAccessible()) && !Boolean.TRUE.equals(property.getAccessibleFlag())) {
            return emptyPage(input);
        }

        Date from = Date.from(input.getCheckIn().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toInclusive = Date.from(input.getCheckOut().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Prices> priceRows = priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDateBetween(
                input.getPropertyId(),
                input.getTenantId(),
                from,
                toInclusive
        );

        if (priceRows.isEmpty() || stayNights <= 0) {
            return emptyPage(input);
        }

        List<RoomSearchResultDto> availableRooms = buildAvailableRooms(input, stayNights, priceRows);
        List<DynamicRoomFilterDto> filters = dynamicFilterService.buildFilters(availableRooms);

        List<RoomSearchResultDto> rooms = dynamicFilterService.applyFilters(availableRooms, defaultFilters(input));

        RoomSortBy sortBy = input.getSortBy() == null ? DEFAULT_SORT_BY : input.getSortBy();
        SortDirection sortDirection = input.getSortDirection() == null ? DEFAULT_SORT_DIRECTION : input.getSortDirection();
        rooms = sortRooms(rooms, sortBy, sortDirection);

        return paginate(rooms, filters, input);
    }

    public List<RoomSearchResultDto> sortRooms(List<RoomSearchResultDto> results, RoomSortBy sortBy, SortDirection sortDirection) {
        Comparator<RoomSearchResultDto> comparator = comparatorFor(sortBy);
        if (sortDirection == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return results.stream()
                .sorted(comparator.thenComparing(RoomSearchResultDto::getRoomTypeName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private void validate(SearchRoomsInputDto input) {
        if (input.getTenantId() == null || input.getPropertyId() == null) {
            throw new IllegalArgumentException("tenantId and propertyId are required");
        }
        if (input.getCheckIn() == null || input.getCheckOut() == null || !input.getCheckOut().isAfter(input.getCheckIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        if (input.getRooms() == null || input.getRooms() <= 0) {
            throw new IllegalArgumentException("rooms must be greater than zero");
        }
        if (input.getPage() != null && input.getPage() < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (input.getSize() != null && input.getSize() <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
    }

    private RoomSpecSummaryDto toRoomSpec(RoomSpec roomSpec) {
        if (roomSpec == null) {
            return null;
        }
        return RoomSpecSummaryDto.builder()
                .roomSpecId(roomSpec.getRoomSpecId())
                .bedType(roomSpec.getBedType())
                .area(roomSpec.getArea())
                .minOcc(roomSpec.getMinOcc())
                .maxOcc(roomSpec.getMaxOcc())
                .build();
    }

    private List<RoomSearchResultDto> buildAvailableRooms(SearchRoomsInputDto input, long stayNights, List<Prices> priceRows) {
        Map<UUID, List<Prices>> groupedByRoomType = priceRows.stream()
                .filter(p -> p.getRoomType() != null && p.getRoomType().getRoomTypeId() != null)
                .collect(Collectors.groupingBy(p -> p.getRoomType().getRoomTypeId()));

        List<RoomSearchResultDto> results = new ArrayList<>();

        for (Map.Entry<UUID, List<Prices>> entry : groupedByRoomType.entrySet()) {
            List<Prices> rows = entry.getValue();
            if (rows.size() < stayNights) {
                continue;
            }

            Map<LocalDate, Prices> daily = rows.stream().collect(Collectors.toMap(
                    p -> p.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    p -> p,
                    (a, b) -> a
            ));

            LocalDate cursor = input.getCheckIn();
            BigDecimal total = BigDecimal.ZERO;
            int minAvailable = Integer.MAX_VALUE;
            boolean completeCoverage = true;

            while (cursor.isBefore(input.getCheckOut())) {
                Prices day = daily.get(cursor);
                if (day == null || day.getRoomPrice() == null || day.getQuantity() == null) {
                    completeCoverage = false;
                    break;
                }
                total = total.add(day.getRoomPrice());
                minAvailable = Math.min(minAvailable, day.getQuantity());
                cursor = cursor.plusDays(1);
            }

            if (!completeCoverage || minAvailable < input.getRooms()) {
                continue;
            }

            Prices any = rows.get(0);
            results.add(RoomSearchResultDto.builder()
                    .roomTypeId(entry.getKey())
                    .roomTypeName(any.getRoomType().getRoomTypeName())
                    .description(any.getRoomType().getDescription())
                    .occupancy(any.getRoomType().getOccupancy())
                    .amenities(any.getRoomType().getAmenities())
                    .images(any.getRoomType().getImages())
                    .baseRate(any.getRoomType().getBaseRate())
                    .roomSpec(toRoomSpec(any.getRoomType().getRoomSpec()))
                    .totalPrice(total.multiply(BigDecimal.valueOf(input.getRooms())))
                    .availableCount(minAvailable)
                    .build());
        }

        return results;
    }

    private RoomSearchResponseDto paginate(List<RoomSearchResultDto> results, List<DynamicRoomFilterDto> filters, SearchRoomsInputDto input) {
        int page = input.getPage() == null ? DEFAULT_PAGE : input.getPage();
        int size = input.getSize() == null ? DEFAULT_SIZE : Math.min(input.getSize(), MAX_SIZE);
        int totalItems = results.size();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        int fromIndex = Math.min(page * size, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);

        return RoomSearchResponseDto.builder()
                .items(results.subList(fromIndex, toIndex))
                .filters(filters)
                .page(page)
                .size(size)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(page + 1 < totalPages)
                .hasPrevious(page > 0 && totalPages > 0)
                .build();
    }

    private RoomSearchResponseDto emptyPage(SearchRoomsInputDto input) {
        int page = input.getPage() == null ? DEFAULT_PAGE : input.getPage();
        int size = input.getSize() == null ? DEFAULT_SIZE : Math.min(input.getSize(), MAX_SIZE);
        return RoomSearchResponseDto.builder()
                .items(List.of())
                .filters(List.of())
                .page(page)
                .size(size)
                .totalItems(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    private Comparator<RoomSearchResultDto> comparatorFor(RoomSortBy sortBy) {
        return switch (sortBy) {
            case OCCUPANCY -> Comparator.comparing(RoomSearchResultDto::getOccupancy, Comparator.nullsLast(Integer::compareTo));
            case AREA -> Comparator.comparing(
                    room -> room.getRoomSpec() == null ? null : room.getRoomSpec().getArea(),
                    Comparator.nullsLast(BigDecimal::compareTo)
            );
            case AVAILABLE_COUNT -> Comparator.comparing(RoomSearchResultDto::getAvailableCount, Comparator.nullsLast(Integer::compareTo));
            case TOTAL_PRICE -> Comparator.comparing(RoomSearchResultDto::getTotalPrice, Comparator.nullsLast(BigDecimal::compareTo));
        };
    }

    private List<SelectedFilterInputDto> defaultFilters(SearchRoomsInputDto input) {
        if (input == null || input.getFilters() == null) {
            return List.of();
        }
        return input.getFilters().stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
