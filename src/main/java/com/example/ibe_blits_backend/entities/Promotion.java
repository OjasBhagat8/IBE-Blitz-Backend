package com.example.ibe_blits_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_id", nullable = false, updatable = false)
    private UUID promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnore
    private Property property;

    @Column(name = "promotion_name", nullable = false)
    private String promotionName;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_kind", nullable = false)
    private PromotionKind promotionKind;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToOne(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = false)
    private PromotionCondition condition;

    @OneToOne(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = false)
    private PromotionReward reward;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PromotionRoomType> roomTypes = new ArrayList<>();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PromoCode> promoCodes = new ArrayList<>();

}
