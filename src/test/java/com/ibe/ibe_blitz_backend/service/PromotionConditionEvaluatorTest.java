package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.entities.PromotionCondition;
import com.ibe.ibe_blitz_backend.entities.PromotionConditionOperator;
import com.ibe.ibe_blitz_backend.entities.PromotionConditionType;
import com.ibe.ibe_blitz_backend.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromotionConditionEvaluatorTest {

    private final PromotionConditionEvaluator evaluator = new PromotionConditionEvaluator();

    @Test
    void matchesReturnsTrueWhenConditionIsNull() {
        assertTrue(evaluator.matches(null, Set.of(), 0, 0, Set.of()));
    }

    @Test
    void matchesTripIncludesDayWithInOperator() {
        PromotionCondition condition = condition(
                PromotionConditionType.TRIP_INCLUDES_DAY,
                PromotionConditionOperator.IN,
                null,
                "[\"SATURDAY\",\"SUNDAY\"]"
        );

        assertTrue(evaluator.matches(condition, Set.of(DayOfWeek.SATURDAY), 2, 2, Set.of()));
        assertFalse(evaluator.matches(condition, Set.of(DayOfWeek.MONDAY), 2, 2, Set.of()));
    }

    @Test
    void matchesTripIncludesAllDaysWithAllOperator() {
        PromotionCondition condition = condition(
                PromotionConditionType.TRIP_INCLUDES_ALL_DAYS,
                PromotionConditionOperator.ALL,
                null,
                "[\"FRIDAY\",\"SATURDAY\"]"
        );

        assertTrue(evaluator.matches(condition, Set.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), 2, 2, Set.of()));
        assertFalse(evaluator.matches(condition, Set.of(DayOfWeek.FRIDAY), 2, 2, Set.of()));
    }

    @Test
    void matchesThrowsForUnsupportedStayDayOperatorsAndInvalidDays() {
        PromotionCondition invalidIn = condition(
                PromotionConditionType.TRIP_INCLUDES_ALL_DAYS,
                PromotionConditionOperator.IN,
                null,
                "[\"FRIDAY\"]"
        );
        PromotionCondition invalidAll = condition(
                PromotionConditionType.TRIP_INCLUDES_DAY,
                PromotionConditionOperator.ALL,
                null,
                "[\"FRIDAY\"]"
        );
        PromotionCondition invalidOperator = condition(
                PromotionConditionType.TRIP_INCLUDES_DAY,
                PromotionConditionOperator.EQUALS,
                null,
                "[\"FRIDAY\"]"
        );
        PromotionCondition missingDays = condition(
                PromotionConditionType.TRIP_INCLUDES_DAY,
                PromotionConditionOperator.IN,
                null,
                "[]"
        );
        PromotionCondition invalidDay = condition(
                PromotionConditionType.TRIP_INCLUDES_DAY,
                PromotionConditionOperator.IN,
                null,
                "[\"FUNDAY\"]"
        );

        assertThrows(BadRequestException.class, () -> evaluator.matches(invalidIn, Set.of(DayOfWeek.FRIDAY), 1, 1, Set.of()));
        assertThrows(BadRequestException.class, () -> evaluator.matches(invalidAll, Set.of(DayOfWeek.FRIDAY), 1, 1, Set.of()));
        assertThrows(BadRequestException.class, () -> evaluator.matches(invalidOperator, Set.of(DayOfWeek.FRIDAY), 1, 1, Set.of()));
        assertThrows(BadRequestException.class, () -> evaluator.matches(missingDays, Set.of(DayOfWeek.FRIDAY), 1, 1, Set.of()));
        assertThrows(BadRequestException.class, () -> evaluator.matches(invalidDay, Set.of(DayOfWeek.FRIDAY), 1, 1, Set.of()));
    }

    @Test
    void matchesNumericConditionsAcrossSupportedOperators() {
        PromotionCondition equals = condition(
                PromotionConditionType.MIN_STAY_NIGHTS,
                PromotionConditionOperator.EQUALS,
                BigDecimal.valueOf(3),
                null
        );
        PromotionCondition greaterOrEqual = condition(
                PromotionConditionType.MIN_STAY_NIGHTS,
                PromotionConditionOperator.GREATER_THAN_OR_EQUAL,
                BigDecimal.valueOf(3),
                null
        );
        PromotionCondition lessOrEqual = condition(
                PromotionConditionType.MIN_GUEST_COUNT,
                PromotionConditionOperator.LESS_THAN_OR_EQUAL,
                BigDecimal.valueOf(2),
                null
        );

        assertTrue(evaluator.matches(equals, Set.of(), 3, 0, Set.of()));
        assertFalse(evaluator.matches(equals, Set.of(), 2, 0, Set.of()));
        assertTrue(evaluator.matches(greaterOrEqual, Set.of(), 4, 0, Set.of()));
        assertFalse(evaluator.matches(greaterOrEqual, Set.of(), 2, 0, Set.of()));
        assertTrue(evaluator.matches(lessOrEqual, Set.of(), 0, 2, Set.of()));
        assertFalse(evaluator.matches(lessOrEqual, Set.of(), 0, 3, Set.of()));
    }

    @Test
    void matchesThrowsForInvalidNumericConfiguration() {
        PromotionCondition missingValue = condition(
                PromotionConditionType.MIN_STAY_NIGHTS,
                PromotionConditionOperator.EQUALS,
                null,
                null
        );
        PromotionCondition unsupportedOperator = condition(
                PromotionConditionType.MIN_GUEST_COUNT,
                PromotionConditionOperator.IN,
                BigDecimal.ONE,
                null
        );

        assertThrows(BadRequestException.class, () -> evaluator.matches(missingValue, Set.of(), 1, 1, Set.of()));
        assertThrows(BadRequestException.class, () -> evaluator.matches(unsupportedOperator, Set.of(), 1, 1, Set.of()));
    }

    @Test
    void matchesGuestTypeConditionsAcrossSupportedOperators() {
        PromotionCondition equals = condition(
                PromotionConditionType.GUEST_TYPE_SELECTED,
                PromotionConditionOperator.EQUALS,
                null,
                "[\"Senior Citizen\",\"Adults\"]"
        );
        PromotionCondition all = condition(
                PromotionConditionType.GUEST_TYPE_SELECTED,
                PromotionConditionOperator.ALL,
                null,
                "[\"Senior Citizen\",\"Adults\"]"
        );

        assertTrue(evaluator.matches(equals, Set.of(), 2, 2, Set.of(" senior citizen ")));
        assertFalse(evaluator.matches(equals, Set.of(), 2, 2, Set.of("children")));
        assertTrue(evaluator.matches(all, Set.of(), 2, 2, Set.of("adults", "senior citizen")));
        assertFalse(evaluator.matches(all, Set.of(), 2, 2, Set.of("adults")));
    }

    @Test
    void matchesThrowsForInvalidGuestTypeConfiguration() {
        PromotionCondition missingGuestTypes = condition(
                PromotionConditionType.GUEST_TYPE_SELECTED,
                PromotionConditionOperator.IN,
                null,
                "[]"
        );
        PromotionCondition unsupportedOperator = condition(
                PromotionConditionType.GUEST_TYPE_SELECTED,
                PromotionConditionOperator.GREATER_THAN_OR_EQUAL,
                null,
                "[\"Adults\"]"
        );

        assertThrows(BadRequestException.class, () -> evaluator.matches(missingGuestTypes, Set.of(), 1, 1, null));
        assertThrows(BadRequestException.class, () -> evaluator.matches(unsupportedOperator, Set.of(), 1, 1, Set.of("adults")));
    }

    @Test
    void matchesThrowsWhenConditionTypeOrOperatorIsMissing() {
        PromotionCondition missingType = new PromotionCondition();
        missingType.setConditionOperator(PromotionConditionOperator.IN);

        PromotionCondition missingOperator = new PromotionCondition();
        missingOperator.setConditionType(PromotionConditionType.TRIP_INCLUDES_DAY);

        assertThrows(BadRequestException.class, () -> evaluator.matches(missingType, Set.of(), 1, 1, Set.of()));
        assertThrows(BadRequestException.class, () -> evaluator.matches(missingOperator, Set.of(), 1, 1, Set.of()));
        assertDoesNotThrow(() -> evaluator.matches(condition(
                PromotionConditionType.GUEST_TYPE_SELECTED,
                PromotionConditionOperator.IN,
                null,
                "[\"Adults\"]"
        ), Set.of(), 1, 1, null));
    }

    private PromotionCondition condition(
            PromotionConditionType type,
            PromotionConditionOperator operator,
            BigDecimal valueNumber,
            String valueJson
    ) {
        return PromotionCondition.builder()
                .conditionType(type)
                .conditionOperator(operator)
                .valueNumber(valueNumber)
                .valueJson(valueJson)
                .build();
    }
}
