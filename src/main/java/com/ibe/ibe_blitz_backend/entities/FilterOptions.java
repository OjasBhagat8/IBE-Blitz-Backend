package com.ibe.ibe_blitz_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "filter_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterOptions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_id",nullable = false, updatable = false)
    private UUID optionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id", nullable = false)
    @JsonIgnore
    private Filters filter;

    @Column(name = "value")
    private String value;
}

