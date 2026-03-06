package com.example.ibe_blits_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "filter_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "filter_config_id",nullable = false, updatable = false)
    private UUID FilterConfigId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnore
    private Property property;

    @OneToMany(mappedBy = "filterConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Filters> filters = new ArrayList<>();

}
