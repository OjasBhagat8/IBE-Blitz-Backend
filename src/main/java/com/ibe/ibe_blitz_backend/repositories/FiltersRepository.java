package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.Filters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FiltersRepository extends JpaRepository<Filters, UUID> {

}

