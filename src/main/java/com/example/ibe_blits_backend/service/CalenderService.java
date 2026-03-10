package com.example.ibe_blits_backend.service;

import com.example.ibe_blits_backend.dto.DailyLeastPriceDto;
import com.example.ibe_blits_backend.entities.Prices;
import com.example.ibe_blits_backend.repositories.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalenderService {

    private final PriceRepository priceRepository;

    public List<DailyLeastPriceDto> getLeastPrices(UUID tenantId, UUID propertyId, LocalDate startDate) {
        LocalDate from = startDate != null ? startDate : LocalDate.now();
        LocalDate to = from.plusMonths(2);

        List<DailyLeastPriceDto> result = new ArrayList<>();
        LocalDate current = from;

        while (!current.isAfter(to)) {
            List<Prices> prices = priceRepository.findByProperty_PropertyIdAndProperty_Tenant_TenantIdAndDate(
                    propertyId,
                    tenantId,
                    java.util.Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant())
            );

            if (!prices.isEmpty()) {
                Prices leastPrice = prices.stream()
                        .filter(price -> price.getRoomPrice() != null)
                        .min(Comparator.comparing(Prices::getRoomPrice))
                        .orElse(null);

                if (leastPrice != null) {
                    result.add(new DailyLeastPriceDto(
                            current,
                            propertyId,
                            leastPrice.getRoomType().getRoomTypeId(),
                            leastPrice.getRoomPrice()
                    ));
                }
            }

            current = current.plusDays(1);
        }

        return result;
    }
}