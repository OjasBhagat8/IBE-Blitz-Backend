package com.ibe.ibe_blitz_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "promotion_condition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCondition extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_condition_id", nullable = false, updatable = false)
    private UUID promotionConditionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false, unique = true)
    @JsonIgnore
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private PromotionConditionType conditionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_operator", nullable = false)
    private PromotionConditionOperator conditionOperator;

    @Column(name = "value_number", precision = 10, scale = 2)
    private BigDecimal valueNumber;

    @Column(name = "value_json", length = 2000)
    private String valueJson;
}
