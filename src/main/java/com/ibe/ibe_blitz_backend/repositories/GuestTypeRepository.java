package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.GuestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuestTypeRepository extends JpaRepository<GuestType, UUID> {
}

