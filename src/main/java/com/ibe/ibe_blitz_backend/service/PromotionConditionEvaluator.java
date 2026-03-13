package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.entities.PromotionCondition;
import com.ibe.ibe_blitz_backend.entities.PromotionConditionOperator;
import com.ibe.ibe_blitz_backend.entities.PromotionConditionType;
import com.ibe.ibe_blitz_backend.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PromotionConditionEvaluator {
    public boolean matches(
            PromotionCondition condition,
            Set<DayOfWeek> stayDays,
            long stayNights,
            int guestCount,
            Set<String> selectedGuestTypes
    ) {
        if (condition == null) {
            return true;
        }
        return evaluateCondition(
                condition,
                stayDays,
                stayNights,
                guestCount,
                selectedGuestTypes
        );
    }

    private boolean evaluateCondition(
            PromotionCondition condition,
            Set<DayOfWeek> stayDays,
            long stayNights,
            int guestCount,
            Set<String> selectedGuestTypes
    ) {
        PromotionConditionType conditionType = requireConditionType(condition);
        PromotionConditionOperator operator = requireOperator(condition);
        return switch (conditionType) {
            case TRIP_INCLUDES_DAY -> evaluateStayDaysCondition(condition, stayDays, operator, false);
            case TRIP_INCLUDES_ALL_DAYS -> evaluateStayDaysCondition(condition, stayDays, operator, true);
            case MIN_STAY_NIGHTS -> evaluateNumericCondition(
                    condition,
                    BigDecimal.valueOf(stayNights),
                    operator
            );
            case MIN_GUEST_COUNT -> evaluateNumericCondition(
                    condition,
                    BigDecimal.valueOf(guestCount),
                    operator
            );
            case GUEST_TYPE_SELECTED -> evaluateGuestTypeCondition(condition, selectedGuestTypes, operator);
        };
    }

    private boolean evaluateStayDaysCondition(
            PromotionCondition condition,
            Set<DayOfWeek> stayDays,
            PromotionConditionOperator operator,
            boolean requireAll
    ) {
        List<DayOfWeek> configuredDays = readConfiguredDays(condition);
        return switch (operator) {
            case IN -> {
                if (requireAll) {
                    throw new BadRequestException("TRIP_INCLUDES_ALL_DAYS does not support operator IN");
                }
                yield configuredDays.stream().anyMatch(stayDays::contains);
            }
            case ALL -> {
                if (!requireAll) {
                    throw new BadRequestException("TRIP_INCLUDES_DAY does not support operator ALL");
                }
                yield configuredDays.stream().allMatch(stayDays::contains);
            }
            default -> throw new BadRequestException("Unsupported operator " + operator + " for " + condition.getConditionType());
        };
    }

    private boolean evaluateNumericCondition(
            PromotionCondition condition,
            BigDecimal actualValue,
            PromotionConditionOperator operator
    ) {
        BigDecimal expectedValue = requireValueNumber(condition);
        int comparison = actualValue.compareTo(expectedValue);
        return switch (operator) {
            case EQUALS -> comparison == 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
            default -> throw new BadRequestException("Unsupported operator " + operator + " for " + condition.getConditionType());
        };
    }

    private boolean evaluateGuestTypeCondition(
            PromotionCondition condition,
            Set<String> selectedGuestTypes,
            PromotionConditionOperator operator
    ) {
        Set<String> normalizedSelectedTypes = selectedGuestTypes == null
                ? Set.of()
                : selectedGuestTypes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalizeValue)
                .collect(Collectors.toSet());
        List<String> configuredGuestTypes = readStringList(condition.getValueJson()).stream()
                .map(this::normalizeValue)
                .toList();
        if (configuredGuestTypes.isEmpty()) {
            throw new BadRequestException("Guest type list is required for " + condition.getConditionType());
        }
        return switch (operator) {
            case EQUALS, IN -> configuredGuestTypes.stream().anyMatch(normalizedSelectedTypes::contains);
            case ALL -> configuredGuestTypes.stream().allMatch(normalizedSelectedTypes::contains);
            default -> throw new BadRequestException("Unsupported operator " + operator + " for " + condition.getConditionType());
        };
    }

    private PromotionConditionType requireConditionType(PromotionCondition condition) {
        if (condition.getConditionType() == null) {
            throw new BadRequestException("Promotion condition type is required");
        }
        return condition.getConditionType();
    }

    private PromotionConditionOperator requireOperator(PromotionCondition condition) {
        if (condition.getConditionOperator() == null) {
            throw new BadRequestException("Promotion condition operator is required for " + condition.getConditionType());
        }
        return condition.getConditionOperator();
    }

    private BigDecimal requireValueNumber(PromotionCondition condition) {
        if (condition.getValueNumber() == null) {
            throw new BadRequestException("Numeric value is required for " + condition.getConditionType());
        }
        return condition.getValueNumber();
    }

    private List<DayOfWeek> readConfiguredDays(PromotionCondition condition) {
        List<String> rawValues = readStringList(condition.getValueJson());
        if (rawValues.isEmpty()) {
            throw new BadRequestException("Day list is required for " + condition.getConditionType());
        }
        List<DayOfWeek> days = rawValues.stream()
                .map(this::toDayOfWeek)
                .toList();
        if (days.stream().anyMatch(day -> day == null)) {
            throw new BadRequestException("Invalid day value configured for " + condition.getConditionType());
        }
        return days;
    }

    private List<String> readStringList(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        String normalized = rawValue.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");
        if (normalized.isBlank()) {
            return List.of();
        }
        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private DayOfWeek toDayOfWeek(String value) {
        try {
            return DayOfWeek.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalizeValue(String value) {
        return value.trim().toUpperCase();
    }
}
