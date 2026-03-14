package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.GuestSelectionInputDto;
import com.ibe.ibe_blitz_backend.dto.RoomDealsResponseDto;
import com.ibe.ibe_blitz_backend.entities.Property;
import com.ibe.ibe_blitz_backend.entities.Promotion;
import com.ibe.ibe_blitz_backend.entities.PromotionApplyTo;
import com.ibe.ibe_blitz_backend.entities.PromotionCondition;
import com.ibe.ibe_blitz_backend.entities.PromotionKind;
import com.ibe.ibe_blitz_backend.entities.PromotionReward;
import com.ibe.ibe_blitz_backend.entities.PromotionRewardType;
import com.ibe.ibe_blitz_backend.entities.RoomType;
import com.ibe.ibe_blitz_backend.exceptions.BadRequestException;
import com.ibe.ibe_blitz_backend.exceptions.NotFoundException;
import com.ibe.ibe_blitz_backend.repositories.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomDealsServiceTest {

    @Mock
    private RoomTypeValidationService roomTypeValidationService;
    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private StayPricingService stayPricingService;
    @Mock
    private PromotionConditionEvaluator promotionConditionEvaluator;
    @Mock
    private PromotionDiscountService promotionDiscountService;

    @InjectMocks
    private RoomDealsService service;

    @Test
    void getRoomDealsRejectsInvalidInput() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        assertThrows(BadRequestException.class, () -> service.getRoomDeals(null, propertyId, today, today.plusDays(1), guests(2)));
        assertThrows(BadRequestException.class, () -> service.getRoomDeals(roomTypeId, propertyId, today, today, guests(2)));
        assertThrows(BadRequestException.class, () -> service.getRoomDeals(roomTypeId, propertyId, today.minusDays(2), today.plusDays(1), guests(2)));
        assertThrows(BadRequestException.class, () -> service.getRoomDeals(roomTypeId, propertyId, today, today.plusDays(1), List.of()));
        assertThrows(BadRequestException.class, () -> service.getRoomDeals(roomTypeId, propertyId, today, today.plusDays(1), List.of(new GuestSelectionInputDto("Adults", -1))));
        assertThrows(BadRequestException.class, () -> service.getRoomDeals(roomTypeId, propertyId, today, today.plusDays(1), List.of(new GuestSelectionInputDto("Adults", 0))));
    }

    @Test
    void getRoomDealsRejectsGuestCountsAbovePropertyLimit() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);

        when(roomTypeValidationService.getRoomType(roomTypeId, propertyId))
                .thenReturn(RoomType.builder()
                        .property(Property.builder().guestAllowed(1).build())
                        .build());

        assertThrows(BadRequestException.class, () -> service.getRoomDeals(roomTypeId, propertyId, checkIn, checkOut, guests(2)));
    }

    @Test
    void getRoomDealsSkipsPromotionsThatDoNotQualifyAndSortsMatches() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);
        RoomType roomType = RoomType.builder()
                .property(Property.builder().guestAllowed(4).build())
                .build();
        Promotion skippedByRoomType = promotion("Skip Room Type");
        Promotion skippedByWindow = promotion("Skip Window");
        Promotion skippedByCondition = promotion("Skip Condition");
        Promotion cheaper = promotion("Cheaper Deal");
        Promotion pricier = promotion("Pricier Deal");

        when(roomTypeValidationService.getRoomType(roomTypeId, propertyId)).thenReturn(roomType);
        when(stayPricingService.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut)).thenReturn(new BigDecimal("200.00"));
        when(promotionRepository.findByProperty_PropertyIdAndActiveTrueAndPromotionKind(propertyId, PromotionKind.AUTOMATIC))
                .thenReturn(List.of(skippedByRoomType, skippedByWindow, skippedByCondition, pricier, cheaper));
        when(roomTypeValidationService.appliesToRoomType(skippedByRoomType, roomTypeId)).thenReturn(false);
        when(roomTypeValidationService.appliesToRoomType(skippedByWindow, roomTypeId)).thenReturn(true);
        when(roomTypeValidationService.appliesToRoomType(skippedByCondition, roomTypeId)).thenReturn(true);
        when(roomTypeValidationService.appliesToRoomType(pricier, roomTypeId)).thenReturn(true);
        when(roomTypeValidationService.appliesToRoomType(cheaper, roomTypeId)).thenReturn(true);
        when(roomTypeValidationService.isWithinPromotionWindow(skippedByWindow, checkIn, checkOut)).thenReturn(false);
        when(roomTypeValidationService.isWithinPromotionWindow(skippedByCondition, checkIn, checkOut)).thenReturn(true);
        when(roomTypeValidationService.isWithinPromotionWindow(pricier, checkIn, checkOut)).thenReturn(true);
        when(roomTypeValidationService.isWithinPromotionWindow(cheaper, checkIn, checkOut)).thenReturn(true);
        when(promotionConditionEvaluator.matches(eq(skippedByCondition.getCondition()), anySet(), anyLong(), anyInt(), anySet())).thenReturn(false);
        when(promotionConditionEvaluator.matches(eq(pricier.getCondition()), anySet(), anyLong(), anyInt(), anySet())).thenReturn(true);
        when(promotionConditionEvaluator.matches(eq(cheaper.getCondition()), anySet(), anyLong(), anyInt(), anySet())).thenReturn(true);
        when(promotionDiscountService.applyReward(new BigDecimal("200.00"), pricier.getReward(), 2)).thenReturn(new BigDecimal("180.00"));
        when(promotionDiscountService.applyReward(new BigDecimal("200.00"), cheaper.getReward(), 2)).thenReturn(new BigDecimal("150.00"));

        RoomDealsResponseDto response = service.getRoomDeals(roomTypeId, propertyId, checkIn, checkOut, guests(2));

        assertEquals(new BigDecimal("200.00"), response.getStandardRate().getTotalPrice());
        assertEquals(2, response.getDeals().size());
        assertEquals("Cheaper Deal", response.getDeals().get(0).getTitle());
        assertEquals("Pricier Deal", response.getDeals().get(1).getTitle());
    }

    @Test
    void getRoomDealsAllowsMissingGuestLimitMetadata() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(1);

        when(roomTypeValidationService.getRoomType(roomTypeId, propertyId)).thenReturn(RoomType.builder().property(null).build());
        when(stayPricingService.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut)).thenReturn(new BigDecimal("100.00"));
        when(promotionRepository.findByProperty_PropertyIdAndActiveTrueAndPromotionKind(propertyId, PromotionKind.AUTOMATIC)).thenReturn(List.of());

        RoomDealsResponseDto response = service.getRoomDeals(roomTypeId, propertyId, checkIn, checkOut, guests(1));

        assertEquals(List.of(), response.getDeals());
    }

    @Test
    void getRoomDealsThrowsWhenPromotionRewardIsMissing() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(1);
        Promotion invalidPromotion = Promotion.builder()
                .promotionId(UUID.randomUUID())
                .promotionName("Broken")
                .description("Broken")
                .promotionKind(PromotionKind.AUTOMATIC)
                .condition(new PromotionCondition())
                .reward(null)
                .build();

        when(roomTypeValidationService.getRoomType(roomTypeId, propertyId))
                .thenReturn(RoomType.builder().property(Property.builder().guestAllowed(4).build()).build());
        when(stayPricingService.calculateStayTotal(propertyId, roomTypeId, checkIn, checkOut)).thenReturn(new BigDecimal("100.00"));
        when(promotionRepository.findByProperty_PropertyIdAndActiveTrueAndPromotionKind(propertyId, PromotionKind.AUTOMATIC))
                .thenReturn(List.of(invalidPromotion));
        when(roomTypeValidationService.appliesToRoomType(invalidPromotion, roomTypeId)).thenReturn(true);
        when(roomTypeValidationService.isWithinPromotionWindow(invalidPromotion, checkIn, checkOut)).thenReturn(true);
        when(promotionConditionEvaluator.matches(eq(invalidPromotion.getCondition()), anySet(), anyLong(), anyInt(), anySet())).thenReturn(true);

        assertThrows(NotFoundException.class, () -> service.getRoomDeals(roomTypeId, propertyId, checkIn, checkOut, guests(2)));
        verify(promotionDiscountService, never()).applyReward(any(), any(), anyLong());
    }

    private Promotion promotion(String name) {
        PromotionReward reward = PromotionReward.builder()
                .rewardType(PromotionRewardType.FLAT_DISCOUNT)
                .applyTo(PromotionApplyTo.STAY_TOTAL)
                .amount(new BigDecimal("20.00"))
                .build();
        PromotionCondition condition = new PromotionCondition();
        Promotion promotion = Promotion.builder()
                .promotionId(UUID.randomUUID())
                .promotionName(name)
                .description(name)
                .promotionKind(PromotionKind.AUTOMATIC)
                .condition(condition)
                .reward(reward)
                .build();
        reward.setPromotion(promotion);
        condition.setPromotion(promotion);
        return promotion;
    }

    private List<GuestSelectionInputDto> guests(int count) {
        return List.of(new GuestSelectionInputDto("Adults", count));
    }
}
