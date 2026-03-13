package com.example.ibe_blits_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "promotion_reward")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionReward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_reward_id", nullable = false, updatable = false)
    private UUID promotionRewardId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false, unique = true)
    @JsonIgnore
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private PromotionRewardType rewardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_to", nullable = false)
    private PromotionApplyTo applyTo;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 10, scale = 2)
    private BigDecimal percentage;
}
