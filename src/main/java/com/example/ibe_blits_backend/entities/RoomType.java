package com.example.ibe_blits_backend.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "occupancy")
    private Integer occupancy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "amenities", columnDefinition = "jsonb")
    private List<String> amenities;

    @Column(name = "base_rate", precision = 10, scale = 2)
    private BigDecimal baseRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_spec_id")
    @JsonIgnore
    private RoomSpec roomSpec;

    @OneToMany(mappedBy = "roomType")
    private List<Prices> pricingList = new ArrayList<>();
}
