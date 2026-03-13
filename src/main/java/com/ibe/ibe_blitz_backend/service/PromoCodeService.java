package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.GuestSelectionInputDto;
import com.ibe.ibe_blitz_backend.dto.PromoCodeApplyRequestDto;
import com.ibe.ibe_blitz_backend.dto.PromotionDealResponseDto;
import com.ibe.ibe_blitz_backend.entities.PromoCode;
import com.ibe.ibe_blitz_backend.entities.Promotion;
import com.ibe.ibe_blitz_backend.entities.PromotionKind;
import com.ibe.ibe_blitz_backend.exceptions.BadRequestException;
import com.ibe.ibe_blitz_backend.exceptions.NotFoundException;
import com.ibe.ibe_blitz_backend.repositories.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoCodeService {
    private final RoomTypeValidationService roomTypeValidationService;
    private final StayPricingService stayPricingService;
    private final PromotionConditionEvaluator promotionConditionEvaluator;
    private final PromotionDiscountService promotionDiscountService;
    private final PromoCodeRepository promoCodeRepository;

    public PromotionDealResponseDto applyPromoCode(PromoCodeApplyRequestDto input) {
        validate(input);
        roomTypeValidationService.getRoomType(input.getRoomTypeId(), input.getPropertyId());
        PromoCode promoCode = promoCodeRepository.findByCode(input.getPromoCode())
                .orElseThrow(() -> new NotFoundException("Promo code not found"));
        if (Boolean.FALSE.equals(promoCode.getActive())) {
            throw new BadRequestException("Promo code is inactive");
        }
        if (promoCode.getExpiryDate() != null && promoCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Promo code is expired");
        }
        Promotion promotion = promoCode.getPromotion();
        if (promotion == null || promotion.getPromotionKind() != PromotionKind.PROMO_CODE) {
            throw new NotFoundException("Promotion not found for promo code");
        }
        if (!input.getPropertyId().equals(promotion.getProperty().getPropertyId())) {
            throw new BadRequestException("Promo code does not belong to the selected property");
        }
        long stayNights = input.getCheckIn().datesUntil(input.getCheckOut()).count();
        int guestCount = calculateGuestCount(input.getGuestSelections());
        BigDecimal standardTotal = stayPricingService.calculateStayTotal(
                input.getPropertyId(),
                input.getRoomTypeId(),
                input.getCheckIn(),
                input.getCheckOut()
        );
        Set<DayOfWeek> stayDays = input.getCheckIn().datesUntil(input.getCheckOut())
                .map(LocalDate::getDayOfWeek)
                .collect(Collectors.toSet());
        Set<String> selectedGuestTypes = toNormalizedGuestTypeSet(input.getGuestSelections());
        if (!roomTypeValidationService.appliesToRoomType(promotion, input.getRoomTypeId())) {
            throw new BadRequestException("Promo code does not apply to the selected room type");
        }
        if (!roomTypeValidationService.isWithinPromotionWindow(promotion, input.getCheckIn(), input.getCheckOut())) {
            throw new BadRequestException("Promo code is not valid for the selected stay dates");
        }
        if (!promotionConditionEvaluator.matches(
                promotion.getCondition(),
                stayDays,
                stayNights,
                guestCount,
                selectedGuestTypes
        )) {
            throw new BadRequestException("Promo code conditions are not satisfied");
        }
        if (promotion.getReward() == null) {
            throw new NotFoundException("Promotion reward is missing for promo code");
        }
        BigDecimal discountedTotal = promotionDiscountService.applyReward(standardTotal, promotion.getReward(), stayNights);
        BigDecimal discountAmount = standardTotal.subtract(discountedTotal).setScale(2, RoundingMode.HALF_UP);
        return PromotionDealResponseDto.builder()
                .promotionId(promotion.getPromotionId())
                .promoCodeId(promoCode.getPromoCodeId())
                .title(promotion.getPromotionName())
                .description(promotion.getDescription())
                .totalPrice(discountedTotal)
                .originalPrice(standardTotal)
                .discountAmount(discountAmount)
                .promotionType(promotion.getPromotionKind().name())
                .build();
    }

    private void validate(PromoCodeApplyRequestDto input) {
        if (input.getRoomTypeId() == null || input.getPropertyId() == null) {
            throw new BadRequestException("roomTypeId and propertyId are required");
        }
        if (input.getCheckIn() == null || input.getCheckOut() == null || !input.getCheckOut().isAfter(input.getCheckIn())) {
            throw new BadRequestException("checkOut must be after checkIn");
        }
        if (input.getGuestSelections() == null || input.getGuestSelections().isEmpty()) {
            throw new BadRequestException("At least one guest selection is required");
        }
        if (input.getGuestSelections().stream().anyMatch(selection -> selection == null || selection.getCount() == null || selection.getCount() < 0)) {
            throw new BadRequestException("Guest selection counts must be zero or greater");
        }
        if (calculateGuestCount(input.getGuestSelections()) < 1) {
            throw new BadRequestException("At least one guest is required");
        }
        if (input.getPromoCode() == null || input.getPromoCode().isBlank()) {
            throw new BadRequestException("promoCode is required");
        }
    }

    private int calculateGuestCount(List<GuestSelectionInputDto> guestSelections) {
        if (guestSelections == null) {
            return 0;
        }
        return guestSelections.stream()
                .filter(selection -> selection != null && selection.getCount() != null)
                .mapToInt(GuestSelectionInputDto::getCount)
                .sum();
    }

    private Set<String> toNormalizedGuestTypeSet(List<GuestSelectionInputDto> guestSelections) {
        if (guestSelections == null) {
            return Set.of();
        }
        return guestSelections.stream()
                .filter(selection -> selection != null
                        && selection.getCount() != null
                        && selection.getCount() > 0
                        && selection.getGuestTypeName() != null
                        && !selection.getGuestTypeName().isBlank())
                .map(GuestSelectionInputDto::getGuestTypeName)
                .map(value -> value.trim().toUpperCase())
                .collect(Collectors.toSet());
    }
}
