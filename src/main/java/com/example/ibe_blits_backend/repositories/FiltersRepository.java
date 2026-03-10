package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.Filters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FiltersRepository extends JpaRepository<Filters, UUID> {

}
