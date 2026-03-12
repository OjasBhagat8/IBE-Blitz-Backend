package com.ibe.ibe_blitz_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prices extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "price_id",nullable = false, updatable = false)
    private UUID PriceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    @JsonIgnore
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnore
    private Property property;

    @Column(name = "room_price", precision = 10, scale = 2)
    private BigDecimal roomPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "date")
    private Date date;
}

