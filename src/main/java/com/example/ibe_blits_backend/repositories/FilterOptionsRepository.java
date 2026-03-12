package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.FilterOptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FilterOptionsRepository extends JpaRepository<FilterOptions, UUID> {
    List<FilterOptions> findByFilter_FilterIdIn(Collection<UUID> filterIds);
}
