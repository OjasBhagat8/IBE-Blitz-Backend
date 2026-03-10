package com.example.ibe_blits_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "room_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_type_id",nullable = false, updatable = false)
    private UUID roomTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnore
    private Property property;

    @Column(name = "room_type_name")
    private String roomTypeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_spec_id")
    @JsonIgnore
    private RoomSpec roomSpec;

    @OneToMany(mappedBy = "roomType")
    private List<Prices> pricingList = new ArrayList<>();
}
