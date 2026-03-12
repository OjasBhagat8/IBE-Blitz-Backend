package com.ibe.ibe_blitz_backend.service;

import com.ibe.ibe_blitz_backend.dto.ConfigResponseDto;
import com.ibe.ibe_blitz_backend.dto.PropertyConfigDto;
import com.ibe.ibe_blitz_backend.dto.RoomPriceRecordDto;
import com.ibe.ibe_blitz_backend.dto.UpdatePropertySettingsInputDto;
import com.ibe.ibe_blitz_backend.dto.UpdateTenantInputDto;
import com.ibe.ibe_blitz_backend.dto.UpsertRoomPriceInputDto;
import com.ibe.ibe_blitz_backend.entities.Prices;
import com.ibe.ibe_blitz_backend.entities.Property;
import com.ibe.ibe_blitz_backend.entities.RoomType;
import com.ibe.ibe_blitz_backend.entities.Tenant;
import com.ibe.ibe_blitz_backend.repositories.PriceRepository;
import com.ibe.ibe_blitz_backend.repositories.PropertyRepository;
import com.ibe.ibe_blitz_backend.repositories.RoomTypeRepository;
import com.ibe.ibe_blitz_backend.repositories.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPanelServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private PriceRepository priceRepository;
    @Mock
    private ConfigService configService;

    @InjectMocks
    private AdminPanelService adminPanelService;

    @Test
    void updateTenantUpdatesOnlyProvidedFields() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .tenantId(tenantId)
                .tenantName("Old")
                .tenantLogo("old-logo")
                .tenantBanner("old-banner")
                .tenantCopyright("old-copy")
                .build();

        UpdateTenantInputDto input = new UpdateTenantInputDto();
        input.setTenantId(tenantId);
        input.setTenantName("New");
        input.setTenantLogo("new-logo");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tenant result = adminPanelService.updateTenant(input);

        assertEquals("New", result.getTenantName());
        assertEquals("new-logo", result.getTenantLogo());
        assertEquals("old-banner", result.getTenantBanner());
    }

    @Test
    void updateTenantThrowsWhenMissing() {
        UUID tenantId = UUID.randomUUID();
        UpdateTenantInputDto input = new UpdateTenantInputDto();
        input.setTenantId(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminPanelService.updateTenant(input));
    }

    @Test
    void updatePropertySettingsReturnsUpdatedPropertyConfig() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().tenantId(tenantId).build();
        Property property = Property.builder()
                .propertyId(propertyId)
                .tenant(tenant)
                .guestAllowed(2)
                .build();

        UpdatePropertySettingsInputDto input = new UpdatePropertySettingsInputDto();
        input.setPropertyId(propertyId);
        input.setGuestAllowed(4);
        input.setAccessibleFlag(true);

        PropertyConfigDto expectedDto = PropertyConfigDto.builder()
                .propertyId(propertyId)
                .propertyName("Hilton Delhi")
                .guestAllowed(4)
                .accessibleFlag(true)
                .build();

        ConfigResponseDto configResponse = ConfigResponseDto.builder()
                .tenantId(tenantId)
                .properties(List.of(expectedDto))
                .build();

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(configService.getConfigByTenant(tenantId)).thenReturn(configResponse);

        PropertyConfigDto result = adminPanelService.updatePropertySettings(input);

        assertEquals(propertyId, result.getPropertyId());
        assertEquals(4, result.getGuestAllowed());
        assertEquals(true, result.getAccessibleFlag());
    }

    @Test
    void upsertRoomPriceUpdatesExistingRecord() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 15);
        RoomType roomType = RoomType.builder()
                .roomTypeId(roomTypeId)
                .roomTypeName("Suite")
                .property(Property.builder().propertyId(propertyId).build())
                .build();

        Prices existing = Prices.builder()
                .PriceId(UUID.randomUUID())
                .roomType(roomType)
                .property(roomType.getProperty())
                .date(Date.valueOf(date))
                .roomPrice(new BigDecimal("100.00"))
                .quantity(2)
                .build();

        UpsertRoomPriceInputDto input = new UpsertRoomPriceInputDto();
        input.setRoomTypeId(roomTypeId);
        input.setDate(date.toString());
        input.setRoomPrice(new BigDecimal("150.00"));
        input.setQuantity(3);

        when(roomTypeRepository.findById(roomTypeId)).thenReturn(Optional.of(roomType));
        when(priceRepository.findFirstByRoomType_RoomTypeIdAndDate(roomTypeId, Date.valueOf(date))).thenReturn(Optional.of(existing));
        when(priceRepository.save(any(Prices.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomPriceRecordDto result = adminPanelService.upsertRoomPrice(input);

        assertEquals(roomTypeId, result.getRoomTypeId());
        assertEquals("150.00", result.getRoomPrice().toPlainString());
        assertEquals(3, result.getQuantity());
    }

    @Test
    void upsertRoomPriceCreatesNewRecordWhenMissing() {
        UUID roomTypeId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 6, 1);
        RoomType roomType = RoomType.builder()
                .roomTypeId(roomTypeId)
                .roomTypeName("Deluxe")
                .property(Property.builder().propertyId(propertyId).build())
                .build();

        UpsertRoomPriceInputDto input = new UpsertRoomPriceInputDto();
        input.setRoomTypeId(roomTypeId);
        input.setDate(date.toString());
        input.setRoomPrice(new BigDecimal("220.00"));
        input.setQuantity(6);

        when(roomTypeRepository.findById(roomTypeId)).thenReturn(Optional.of(roomType));
        when(priceRepository.findFirstByRoomType_RoomTypeIdAndDate(roomTypeId, Date.valueOf(date))).thenReturn(Optional.empty());
        when(priceRepository.save(any(Prices.class))).thenAnswer(invocation -> {
            Prices p = invocation.getArgument(0);
            p.setPriceId(UUID.randomUUID());
            return p;
        });

        RoomPriceRecordDto result = adminPanelService.upsertRoomPrice(input);

        assertNotNull(result.getPriceId());
        assertEquals("220.00", result.getRoomPrice().toPlainString());
    }

    @Test
    void pricesReturnsMappedRecords() {
        UUID propertyId = UUID.randomUUID();
        UUID roomTypeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 7, 10);

        RoomType roomType = RoomType.builder()
                .roomTypeId(roomTypeId)
                .roomTypeName("Studio")
                .build();
        Property property = Property.builder().propertyId(propertyId).build();

        Prices row = Prices.builder()
                .PriceId(UUID.randomUUID())
                .roomType(roomType)
                .property(property)
                .date(Date.valueOf(date))
                .roomPrice(new BigDecimal("180.00"))
                .quantity(5)
                .build();

        when(priceRepository.findByProperty_PropertyIdAndDateBetweenOrderByDateAsc(
                propertyId,
                Date.valueOf("2026-07-01"),
                Date.valueOf("2026-07-31")
        )).thenReturn(List.of(row));

        List<RoomPriceRecordDto> result = adminPanelService.prices(propertyId, "2026-07-01", "2026-07-31");

        assertEquals(1, result.size());
        assertEquals("Studio", result.get(0).getRoomTypeName());
        assertEquals("2026-07-10", result.get(0).getDate());
    }
}


