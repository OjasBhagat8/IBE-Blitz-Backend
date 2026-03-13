package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.entities.Promotion;
import com.ibe.ibe_blitz_backend.entities.RoomType;
import com.ibe.ibe_blitz_backend.exceptions.NotFoundException;
import com.ibe.ibe_blitz_backend.repositories.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomTypeValidationService {
    private final RoomTypeRepository roomTypeRepository;

    public RoomType getRoomType(UUID roomTypeId, UUID propertyId) {
        return roomTypeRepository.findByRoomTypeIdAndProperty_PropertyId(roomTypeId, propertyId)
                .orElseThrow(() -> new NotFoundException("Room type does not belong to the selected property"));
    }

    public boolean appliesToRoomType(Promotion promotion, UUID roomTypeId) {
        if (promotion.getRoomTypes() == null || promotion.getRoomTypes().isEmpty()) {
            return true;
        }
        return promotion.getRoomTypes().stream()
                .anyMatch(mapping -> mapping.getRoomType() != null
                        && roomTypeId.equals(mapping.getRoomType().getRoomTypeId()));
    }

    public boolean isWithinPromotionWindow(Promotion promotion, LocalDate checkIn, LocalDate checkOut) {
        LocalDate stayEnd = checkOut.minusDays(1);
        if (promotion.getStartDate() != null && checkIn.isBefore(promotion.getStartDate())) {
            return false;
        }
        return promotion.getEndDate() == null || !stayEnd.isAfter(promotion.getEndDate());
    }
}
