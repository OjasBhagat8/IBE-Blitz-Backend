package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomTypeRepository  extends JpaRepository<RoomType, UUID> {
}
