package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.Prices;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PricesRepository extends JpaRepository<Prices, UUID> {
}
