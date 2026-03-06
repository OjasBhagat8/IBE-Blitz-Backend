package com.example.ibe_blits_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "room_spec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSpec extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_spec_id",nullable = false, updatable = false)
    private UUID roomSpecId;

    @Column(name = "bed_type")
    private String bedType;

    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area;

    @Column(name = "min_occ")
    private Integer minOcc;

    @Column(name = "max_occ")
    private Integer maxOcc;

    @Column(name = "quantity")
    private Integer quantity;

    @OneToMany(mappedBy = "roomSpec")
    private List<RoomType> roomTypes = new ArrayList<>();
}
