package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.FilterOptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FilterOptionsRepository extends JpaRepository<FilterOptions, UUID> {
}
