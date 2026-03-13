package com.example.ibe_blits_backend.service;

import com.example.ibe_blits_backend.dto.GuestSelectionInputDto;
import com.example.ibe_blits_backend.dto.PromotionDealResponseDto;
import com.example.ibe_blits_backend.dto.RoomPricingResponseDto;
import com.example.ibe_blits_backend.dto.StandardRateResponseDto;
import com.example.ibe_blits_backend.entities.Promotion;
import com.example.ibe_blits_backend.entities.PromotionKind;
import com.example.ibe_blits_backend.entities.RoomType;
import com.example.ibe_blits_backend.exceptions.BadRequestException;
import com.example.ibe_blits_backend.exceptions.NotFoundException;
import com.example.ibe_blits_backend.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomPricingService {

    private static final String STANDARD_RATE_TITLE = "Standard Rate";
    private static final String STANDARD_RATE_DESCRIPTION = "Have a comfortable stay.";

    private final RoomTypeValidationService roomTypeValidationService;
    private final PromotionRepository promotionRepository;
    private final StayPricingService stayPricingService;
    private final PromotionConditionEvaluator promotionConditionEvaluator;
    private final PromotionDiscountService promotionDiscountService;

    public RoomPricingResponseDto getPricingOptions(
            UUID roomTypeId,
            UUID propertyId,
            LocalDate checkIn,
            LocalDate checkOut,
            List<GuestSelectionInputDto> guestSelections
    ) {
        validate(roomTypeId, propertyId, checkIn, checkOut, guestSelections);

        RoomType roomType = roomTypeValidationService.getRoomType(roomTypeId, propertyId);

        long stayNights = checkIn.datesUntil(checkOut).count();
        int guestCount = calculateGuestCount(guestSelections);
        validateGuestLimit(roomType, guestCount);
        BigDecimal standardTotal = stayPricingService.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut);
        Set<DayOfWeek> stayDays = collectStayDays(checkIn, checkOut);
        Set<String> selectedGuestTypes = toNormalizedGuestTypeSet(guestSelections);

        List<PromotionDealResponseDto> deals = new ArrayList<>();
        List<Promotion> promotions = promotionRepository
                .findByProperty_PropertyIdAndActiveTrueAndPromotionKind(propertyId, PromotionKind.AUTOMATIC);

        for (Promotion promotion : promotions) {
            if (!roomTypeValidationService.appliesToRoomType(promotion, roomTypeId)) {
                continue;
            }
            if (!roomTypeValidationService.isWithinPromotionWindow(promotion, checkIn, checkOut)) {
                continue;
            }
            boolean matchesCondition = promotionConditionEvaluator.matches(
                    promotion.getCondition(),
                    stayDays,
                    stayNights,
                    guestCount,
                    selectedGuestTypes
            );
            if (!matchesCondition) {
                continue;
            }
            deals.add(toDealDto(promotion, standardTotal, stayNights));
        }
        deals.sort(Comparator.comparing(PromotionDealResponseDto::getTotalPrice));

        return RoomPricingResponseDto.builder()
                .standardRate(StandardRateResponseDto.builder()
                        .title(STANDARD_RATE_TITLE)
                        .description(STANDARD_RATE_DESCRIPTION)
                        .totalPrice(standardTotal)
                        .build())
                .deals(deals)
                .build();
    }

    private void validate(
            UUID roomTypeId,
            UUID propertyId,
            LocalDate checkIn,
            LocalDate checkOut,
            List<GuestSelectionInputDto> guestSelections
    ) {
        if (roomTypeId == null || propertyId == null) {
            throw new BadRequestException("roomTypeId and propertyId are required");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BadRequestException("checkOut must be after checkIn");
        }
        LocalDate today = LocalDate.now();
        if (checkIn.isBefore(today)) {
            throw new BadRequestException("checkIn date cannot be in the past. Please select today or a future date.");
        }
        if (guestSelections == null || guestSelections.isEmpty()) {
            throw new BadRequestException("At least one guest selection is required");
        }
        if (guestSelections.stream().anyMatch(selection -> selection == null || selection.getCount() == null || selection.getCount() < 0)) {
            throw new BadRequestException("Guest selection counts must be zero or greater");
        }
        if (calculateGuestCount(guestSelections) < 1) {
            throw new BadRequestException("At least one guest is required");
        }
    }

    private PromotionDealResponseDto toDealDto(Promotion promotion, BigDecimal standardTotal, long stayNights) {
        if (promotion.getReward() == null) {
            throw new NotFoundException("Promotion reward is missing for promotion " + promotion.getPromotionId());
        }

        BigDecimal discountedTotal = promotionDiscountService.applyReward(standardTotal, promotion.getReward(), stayNights);
        BigDecimal discountAmount = standardTotal.subtract(discountedTotal).setScale(2, RoundingMode.HALF_UP);

        return PromotionDealResponseDto.builder()
                .promotionId(promotion.getPromotionId())
                .title(promotion.getPromotionName())
                .description(promotion.getDescription())
                .totalPrice(discountedTotal)
                .originalPrice(standardTotal)
                .discountAmount(discountAmount)
                .promotionType(promotion.getPromotionKind().name())
                .build();
    }

    private void validateGuestLimit(RoomType roomType, int guestCount) {
        if (roomType == null || roomType.getProperty() == null) {
            return;
        }
        Integer guestAllowed = roomType.getProperty().getGuestAllowed();
        if (guestAllowed == null || guestAllowed <= 0) {
            return;
        }
        if (guestCount > guestAllowed) {
            throw new BadRequestException("Selected guests exceed allowed occupancy. Maximum " + guestAllowed + " guest(s) per room.");
        }
    }

    private int calculateGuestCount(List<GuestSelectionInputDto> guestSelections) {
        if (guestSelections == null) {
            return 0;
        }
        int guestCount = 0;
        for (GuestSelectionInputDto selection : guestSelections) {
            if (selection == null || selection.getCount() == null) {
                continue;
            }
            guestCount += selection.getCount();
        }
        return guestCount;
    }

    private Set<String> toNormalizedGuestTypeSet(List<GuestSelectionInputDto> guestSelections) {
        if (guestSelections == null) {
            return Set.of();
        }
        Set<String> normalizedGuestTypes = new HashSet<>();
        for (GuestSelectionInputDto selection : guestSelections) {
            if (selection == null || selection.getCount() == null || selection.getCount() <= 0) {
                continue;
            }
            String guestTypeName = selection.getGuestTypeName();
            if (guestTypeName == null || guestTypeName.isBlank()) {
                continue;
            }
            normalizedGuestTypes.add(guestTypeName.trim().toUpperCase());
        }
        return normalizedGuestTypes;
    }

    private Set<DayOfWeek> collectStayDays(LocalDate checkIn, LocalDate checkOut) {
        Set<DayOfWeek> stayDays = new HashSet<>();
        LocalDate cursor = checkIn;
        while (cursor.isBefore(checkOut)) {
            stayDays.add(cursor.getDayOfWeek());
            cursor = cursor.plusDays(1);
        }
        return stayDays;
    }
}
