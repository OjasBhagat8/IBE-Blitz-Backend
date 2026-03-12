package com.ibe.ibe_blitz_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tenant_id",nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "tenant_name", nullable = false)
    private String tenantName;

    @Column(name = "tenant_logo")
    private String tenantLogo;

    @Column(name = "tenant_banner")
    private String tenantBanner;

    @Column(name = "tenant_copyright")
    private String tenantCopyright;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Property> properties = new ArrayList<>();
}

