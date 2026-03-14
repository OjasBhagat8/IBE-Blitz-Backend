package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.entities.PromotionApplyTo;
import com.ibe.ibe_blitz_backend.entities.PromotionReward;
import com.ibe.ibe_blitz_backend.entities.PromotionRewardType;
import com.ibe.ibe_blitz_backend.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromotionDiscountServiceTest {

    private final PromotionDiscountService service = new PromotionDiscountService();

    @Test
    void applyRewardSupportsPercentageDiscounts() {
        PromotionReward stayTotal = reward(PromotionRewardType.PERCENTAGE_DISCOUNT, PromotionApplyTo.STAY_TOTAL);
        stayTotal.setPercentage(new BigDecimal("10"));

        PromotionReward perNight = reward(PromotionRewardType.PERCENTAGE_DISCOUNT, PromotionApplyTo.PER_NIGHT);
        perNight.setPercentage(new BigDecimal("25"));

        assertEquals(new BigDecimal("90.00"), service.applyReward(new BigDecimal("100.00"), stayTotal, 2));
        assertEquals(new BigDecimal("90.00"), service.applyReward(new BigDecimal("120.00"), perNight, 4));
    }

    @Test
    void applyRewardSupportsFlatDiscountsAndFloorsNegativeTotalsAtZero() {
        PromotionReward stayTotal = reward(PromotionRewardType.FLAT_DISCOUNT, PromotionApplyTo.STAY_TOTAL);
        stayTotal.setAmount(new BigDecimal("15.50"));

        PromotionReward perNight = reward(PromotionRewardType.FLAT_DISCOUNT, PromotionApplyTo.PER_NIGHT);
        perNight.setAmount(new BigDecimal("20.00"));

        PromotionReward excessive = reward(PromotionRewardType.FLAT_DISCOUNT, PromotionApplyTo.STAY_TOTAL);
        excessive.setAmount(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("84.50"), service.applyReward(new BigDecimal("100.00"), stayTotal, 3));
        assertEquals(new BigDecimal("40.00"), service.applyReward(new BigDecimal("100.00"), perNight, 3));
        assertEquals(new BigDecimal("0.00"), service.applyReward(new BigDecimal("100.00"), excessive, 1));
    }

    @Test
    void applyRewardRejectsMissingRewardMetadata() {
        assertThrows(BadRequestException.class, () -> service.applyReward(new BigDecimal("100.00"), null, 1));

        PromotionReward missingType = new PromotionReward();
        missingType.setApplyTo(PromotionApplyTo.STAY_TOTAL);
        assertThrows(BadRequestException.class, () -> service.applyReward(new BigDecimal("100.00"), missingType, 1));

        PromotionReward missingApplyTo = new PromotionReward();
        missingApplyTo.setRewardType(PromotionRewardType.FLAT_DISCOUNT);
        assertThrows(BadRequestException.class, () -> service.applyReward(new BigDecimal("100.00"), missingApplyTo, 1));
    }

    @Test
    void applyRewardRejectsMissingValuesAndInvalidPerNightStayLength() {
        PromotionReward missingPercentage = reward(PromotionRewardType.PERCENTAGE_DISCOUNT, PromotionApplyTo.STAY_TOTAL);
        PromotionReward missingAmount = reward(PromotionRewardType.FLAT_DISCOUNT, PromotionApplyTo.STAY_TOTAL);
        PromotionReward percentagePerNight = reward(PromotionRewardType.PERCENTAGE_DISCOUNT, PromotionApplyTo.PER_NIGHT);
        percentagePerNight.setPercentage(new BigDecimal("10"));
        PromotionReward flatPerNight = reward(PromotionRewardType.FLAT_DISCOUNT, PromotionApplyTo.PER_NIGHT);
        flatPerNight.setAmount(new BigDecimal("10"));

        assertThrows(BadRequestException.class, () -> service.applyReward(new BigDecimal("100.00"), missingPercentage, 1));
        assertThrows(BadRequestException.class, () -> service.applyReward(new BigDecimal("100.00"), missingAmount, 1));
        assertThrows(BadRequestException.class, () -> service.applyReward(new BigDecimal("100.00"), percentagePerNight, 0));
        assertThrows(BadRequestException.class, () -> service.applyReward(new BigDecimal("100.00"), flatPerNight, 0));
    }

    private PromotionReward reward(PromotionRewardType rewardType, PromotionApplyTo applyTo) {
        return PromotionReward.builder()
                .rewardType(rewardType)
                .applyTo(applyTo)
                .build();
    }
}
