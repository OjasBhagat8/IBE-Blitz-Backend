package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.FilterConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FilterConfigRepository extends JpaRepository<FilterConfig , UUID> {
}
