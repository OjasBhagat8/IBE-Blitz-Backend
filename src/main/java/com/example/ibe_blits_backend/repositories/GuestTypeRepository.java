package com.example.ibe_blits_backend.repositories;

import com.example.ibe_blits_backend.entities.GuestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuestTypeRepository extends JpaRepository<GuestType, UUID> {
}
