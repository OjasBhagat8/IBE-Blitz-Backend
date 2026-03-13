package com.example.ibe_blits_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "promo_code",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_promo_code_code", columnNames = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promo_code_id", nullable = false, updatable = false)
    private UUID promoCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    @JsonIgnore
    private Promotion promotion;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
