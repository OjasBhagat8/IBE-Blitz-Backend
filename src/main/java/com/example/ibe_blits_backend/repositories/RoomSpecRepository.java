package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.RoomSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomSpecRepository extends JpaRepository<RoomSpec, UUID> {
}
