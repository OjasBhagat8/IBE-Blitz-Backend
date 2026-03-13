package com.example.ibe_blits_backend.service;

import com.example.ibe_blits_backend.entities.PromotionApplyTo;
import com.example.ibe_blits_backend.entities.PromotionReward;
import com.example.ibe_blits_backend.entities.PromotionRewardType;
import com.example.ibe_blits_backend.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PromotionDiscountService {

    public BigDecimal applyReward(BigDecimal standardTotal, PromotionReward reward, long stayNights) {
        if (reward == null || reward.getRewardType() == null) {
            throw new BadRequestException("Promotion reward type is required");
        }
        if (reward.getApplyTo() == null) {
            throw new BadRequestException("Promotion reward applyTo is required");
        }

        BigDecimal discountedTotal = switch (reward.getRewardType()) {
            case PERCENTAGE_DISCOUNT -> applyPercentageDiscount(standardTotal, reward, stayNights);
            case FLAT_DISCOUNT -> applyFlatDiscount(standardTotal, reward, stayNights);
        };

        if (discountedTotal.signum() < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return discountedTotal.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyPercentageDiscount(BigDecimal standardTotal, PromotionReward reward, long stayNights) {
        BigDecimal percentage = requirePercentage(reward);
        return switch (reward.getApplyTo()) {
            case STAY_TOTAL -> standardTotal.subtract(calculatePercentageDiscount(standardTotal, percentage));
            case PER_NIGHT -> {
                if (stayNights <= 0) {
                    throw new BadRequestException("stayNights must be greater than zero for PER_NIGHT rewards");
                }
                BigDecimal perNightBase = standardTotal.divide(BigDecimal.valueOf(stayNights), 2, RoundingMode.HALF_UP);
                BigDecimal perNightDiscount = calculatePercentageDiscount(perNightBase, percentage);
                yield standardTotal.subtract(perNightDiscount.multiply(BigDecimal.valueOf(stayNights)));
            }
        };
    }

    private BigDecimal applyFlatDiscount(BigDecimal standardTotal, PromotionReward reward, long stayNights) {
        BigDecimal amount = requireAmount(reward);
        return switch (reward.getApplyTo()) {
            case STAY_TOTAL -> standardTotal.subtract(amount);
            case PER_NIGHT -> {
                if (stayNights <= 0) {
                    throw new BadRequestException("stayNights must be greater than zero for PER_NIGHT rewards");
                }
                yield standardTotal.subtract(amount.multiply(BigDecimal.valueOf(stayNights)));
            }
        };
    }

    private BigDecimal calculatePercentageDiscount(BigDecimal baseAmount, BigDecimal percentage) {
        return baseAmount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal requirePercentage(PromotionReward reward) {
        if (reward.getPercentage() == null) {
            throw new BadRequestException("Percentage value is required for PERCENTAGE_DISCOUNT");
        }
        return reward.getPercentage();
    }

    private BigDecimal requireAmount(PromotionReward reward) {
        if (reward.getAmount() == null) {
            throw new BadRequestException("Amount value is required for FLAT_DISCOUNT");
        }
        return reward.getAmount();
    }
}
