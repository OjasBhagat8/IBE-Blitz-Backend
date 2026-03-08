package com.example.ibe_blits_backend.service;

import com.example.ibe_blits_backend.dto.PropertyConfigDto;
import com.example.ibe_blits_backend.dto.RoomPriceRecordDto;
import com.example.ibe_blits_backend.dto.UpdatePropertySettingsInputDto;
import com.example.ibe_blits_backend.dto.UpdateTenantInputDto;
import com.example.ibe_blits_backend.dto.UpsertRoomPriceInputDto;
import com.example.ibe_blits_backend.entities.Prices;
import com.example.ibe_blits_backend.entities.Property;
import com.example.ibe_blits_backend.entities.RoomType;
import com.example.ibe_blits_backend.entities.Tenant;
import com.example.ibe_blits_backend.repositories.PriceRepository;
import com.example.ibe_blits_backend.repositories.PropertyRepository;
import com.example.ibe_blits_backend.repositories.RoomTypeRepository;
import com.example.ibe_blits_backend.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPanelService {

    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PriceRepository priceRepository;
    private final ConfigService configService;

    @Transactional
    public Tenant updateTenant(UpdateTenantInputDto input) {
        Tenant tenant = tenantRepository.findById(input.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + input.getTenantId()));

        if (input.getTenantName() != null) tenant.setTenantName(input.getTenantName());
        if (input.getTenantLogo() != null) tenant.setTenantLogo(input.getTenantLogo());
        if (input.getTenantBanner() != null) tenant.setTenantBanner(input.getTenantBanner());
        if (input.getTenantCopyright() != null) tenant.setTenantCopyright(input.getTenantCopyright());

        return tenantRepository.save(tenant);
    }

    @Transactional
    public PropertyConfigDto updatePropertySettings(UpdatePropertySettingsInputDto input) {
        Property property = propertyRepository.findById(input.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + input.getPropertyId()));

        if (input.getGuestAllowed() != null) property.setGuestAllowed(input.getGuestAllowed());
        if (input.getGuestFlag() != null) property.setGuestFlag(input.getGuestFlag());
        if (input.getRoomCount() != null) property.setRoomCount(input.getRoomCount());
        if (input.getLengthOfStay() != null) property.setLengthOfStay(input.getLengthOfStay());
        if (input.getRoomFlag() != null) property.setRoomFlag(input.getRoomFlag());
        if (input.getAccessibleFlag() != null) property.setAccessibleFlag(input.getAccessibleFlag());

        Property savedProperty = propertyRepository.save(property);
        UUID tenantId = savedProperty.getTenant().getTenantId();
        UUID propertyId = savedProperty.getPropertyId();
        return configService.getConfigByTenant(tenantId)
                .getProperties()
                .stream()
                .filter(p -> p.getPropertyId().equals(propertyId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Updated property missing from config response"));
    }

    @Transactional
    public RoomPriceRecordDto upsertRoomPrice(UpsertRoomPriceInputDto input) {
        LocalDate localDate = LocalDate.parse(input.getDate());
        Date sqlDate = Date.valueOf(localDate);

        RoomType roomType = roomTypeRepository.findById(input.getRoomTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Room type not found: " + input.getRoomTypeId()));

        Prices price = priceRepository.findFirstByRoomType_RoomTypeIdAndDate(input.getRoomTypeId(), sqlDate)
                .orElseGet(() -> Prices.builder()
                        .roomType(roomType)
                        .property(roomType.getProperty())
                        .date(sqlDate)
                        .build());

        if (input.getRoomPrice() != null) price.setRoomPrice(input.getRoomPrice());
        if (input.getQuantity() != null) price.setQuantity(input.getQuantity());

        Prices saved = priceRepository.save(price);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RoomPriceRecordDto> prices(UUID propertyId, String fromDate, String toDate) {
        Date from = Date.valueOf(LocalDate.parse(fromDate));
        Date to = Date.valueOf(LocalDate.parse(toDate));
        return priceRepository.findByProperty_PropertyIdAndDateBetweenOrderByDateAsc(propertyId, from, to)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private RoomPriceRecordDto toDto(Prices price) {
        return RoomPriceRecordDto.builder()
                .priceId(price.getPriceId())
                .roomTypeId(price.getRoomType().getRoomTypeId())
                .roomTypeName(price.getRoomType().getRoomTypeName())
                .propertyId(price.getProperty().getPropertyId())
                .date(new Date(price.getDate().getTime()).toLocalDate().toString())
                .roomPrice(price.getRoomPrice())
                .quantity(price.getQuantity())
                .build();
    }
}
