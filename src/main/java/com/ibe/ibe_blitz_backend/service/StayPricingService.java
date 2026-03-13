package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.entities.Prices;
import com.ibe.ibe_blitz_backend.exceptions.NotFoundException;
import com.ibe.ibe_blitz_backend.repositories.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StayPricingService {
    private final PriceRepository priceRepository;

    public BigDecimal calculateStayTotal(UUID propertyId, UUID roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        Date from = toDate(checkIn);
        Date toInclusive = toDate(checkOut.minusDays(1));
        List<Prices> priceRows = priceRepository.findByPropertyIdAndRoomTypeIdAndDateBetweenOrderByDateAsc(
                propertyId,
                roomTypeId,
                from,
                toInclusive
        );
        Map<LocalDate, Prices> pricesByDate = new HashMap<>();
        for (Prices priceRow : priceRows) {
            pricesByDate.put(toLocalDate(priceRow.getDate()), priceRow);
        }
        BigDecimal total = BigDecimal.ZERO;
        LocalDate cursor = checkIn;
        while (cursor.isBefore(checkOut)) {
            Prices dailyPrice = pricesByDate.get(cursor);
            if (dailyPrice == null) {
                throw new NotFoundException("Missing price for one or more stay dates");
            }
            if (dailyPrice.getRoomPrice() == null) {
                throw new NotFoundException("Missing room price for one or more stay dates");
            }
            total = total.add(dailyPrice.getRoomPrice());
            cursor = cursor.plusDays(1);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
