package com.ibe.ibe_blitz_backend.repositories;

import com.ibe.ibe_blitz_backend.entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomTypeRepository  extends JpaRepository<RoomType, UUID> {
}

