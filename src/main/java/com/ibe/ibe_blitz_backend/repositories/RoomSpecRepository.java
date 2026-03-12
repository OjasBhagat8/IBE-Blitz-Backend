package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.RoomSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomSpecRepository extends JpaRepository<RoomSpec, UUID> {
}

