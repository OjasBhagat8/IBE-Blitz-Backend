package com.ibe.ibe_blitz_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "filter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Filters  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "filter_id",nullable = false, updatable = false)
    private UUID filterId;

    @Column(name = "filter_name")
    private String filterName;

    @OneToMany(mappedBy = "filter")
    private List<FilterOptions> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_config_id")
    @JsonIgnore
    private FilterConfig filterConfig ;
}

