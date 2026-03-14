package com.ibe.ibe_blitz_backend.config;

import com.ibe.ibe_blitz_backend.entities.FilterConfig;
import com.ibe.ibe_blitz_backend.entities.Property;
import com.ibe.ibe_blitz_backend.entities.RoomSpec;
import com.ibe.ibe_blitz_backend.entities.RoomType;
import com.ibe.ibe_blitz_backend.entities.Tenant;
import com.ibe.ibe_blitz_backend.repositories.FilterConfigRepository;
import com.ibe.ibe_blitz_backend.repositories.FilterOptionsRepository;
import com.ibe.ibe_blitz_backend.repositories.FiltersRepository;
import com.ibe.ibe_blitz_backend.repositories.GuestTypeRepository;
import com.ibe.ibe_blitz_backend.repositories.PriceRepository;
import com.ibe.ibe_blitz_backend.repositories.PropertyRepository;
import com.ibe.ibe_blitz_backend.repositories.PromotionRepository;
import com.ibe.ibe_blitz_backend.repositories.RoomSpecRepository;
import com.ibe.ibe_blitz_backend.repositories.RoomTypeRepository;
import com.ibe.ibe_blitz_backend.repositories.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private GuestTypeRepository guestTypeRepository;
    @Mock
    private RoomSpecRepository roomSpecRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private FilterConfigRepository filterConfigRepository;
    @Mock
    private FiltersRepository filtersRepository;
    @Mock
    private FilterOptionsRepository filterOptionsRepository;
    @Mock
    private PriceRepository priceRepository;
    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    void runSkipsWhenDataAlreadyExists() {
        when(tenantRepository.count()).thenReturn(2L);

        dataSeeder.run();

        verify(propertyRepository, never()).saveAll(any());
        verify(priceRepository, never()).saveAll(any());
    }

    @Test
    void runSeedsDataWhenDatabaseIsEmpty() {
        when(tenantRepository.count()).thenReturn(0L, 2L);
        when(propertyRepository.count()).thenReturn(5L);
        when(roomTypeRepository.count()).thenReturn(5L);
        when(priceRepository.count()).thenReturn(455L);

        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            if (tenant.getTenantId() == null) {
                tenant.setTenantId(UUID.randomUUID());
            }
            return tenant;
        });

        when(propertyRepository.saveAll(any())).thenAnswer(invocation -> {
            List<Property> properties = invocation.getArgument(0);
            for (Property property : properties) {
                if (property.getPropertyId() == null) {
                    property.setPropertyId(UUID.randomUUID());
                }
            }
            return properties;
        });

        when(guestTypeRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(roomSpecRepository.saveAll(any())).thenAnswer(invocation -> {
            List<RoomSpec> specs = invocation.getArgument(0);
            for (RoomSpec spec : specs) {
                if (spec.getRoomSpecId() == null) {
                    spec.setRoomSpecId(UUID.randomUUID());
                }
            }
            return specs;
        });

        when(roomTypeRepository.saveAll(any())).thenAnswer(invocation -> {
            List<RoomType> roomTypes = invocation.getArgument(0);
            for (RoomType roomType : roomTypes) {
                if (roomType.getRoomTypeId() == null) {
                    roomType.setRoomTypeId(UUID.randomUUID());
                }
            }
            return roomTypes;
        });

        when(priceRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(promotionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(filterConfigRepository.save(any(FilterConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(filtersRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(filterOptionsRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        dataSeeder.run();

        verify(tenantRepository, times(2)).save(any(Tenant.class));
        verify(propertyRepository, times(1)).saveAll(any());
        verify(guestTypeRepository, times(1)).saveAll(any());
        verify(roomSpecRepository, times(1)).saveAll(any());
        verify(roomTypeRepository, times(1)).saveAll(any());
        verify(priceRepository, times(1)).saveAll(any());
        verify(promotionRepository, times(1)).saveAll(any());
        verify(filterConfigRepository, times(5)).save(any(FilterConfig.class));
        verify(filtersRepository, times(5)).saveAll(any());
        verify(filterOptionsRepository, times(5)).saveAll(any());
    }
}


