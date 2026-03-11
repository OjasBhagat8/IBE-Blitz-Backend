package com.example.ibe_blits_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "property")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_id",nullable = false, updatable = false)
    private UUID propertyId;

    @Column(name ="property_name", nullable = false)
    private String propertyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnore
    private Tenant tenant;

    @Column(name = "guest_allowed")
    private Integer guestAllowed;

    @Column(name = "guest_flag")
    private Boolean guestFlag;

    @Column(name = "room_count")
    private Integer roomCount;

    @Column(name = "length_of_stay")
    private Integer lengthOfStay;

    @Column(name = "room_flag")
    private Boolean roomFlag;

    @Column(name = "accessible_flag")
    private Boolean accessibleFlag;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<GuestType> guestTypes = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<RoomType> roomTypes = new ArrayList<>();
}
