package com.example.ibe_blits_backend.config;

import com.example.ibe_blits_backend.entities.FilterConfig;
import com.example.ibe_blits_backend.entities.FilterOptions;
import com.example.ibe_blits_backend.entities.Filters;
import com.example.ibe_blits_backend.entities.GuestType;
import com.example.ibe_blits_backend.entities.Prices;
import com.example.ibe_blits_backend.entities.Property;
import com.example.ibe_blits_backend.entities.RoomSpec;
import com.example.ibe_blits_backend.entities.RoomType;
import com.example.ibe_blits_backend.entities.Tenant;
import com.example.ibe_blits_backend.repositories.FilterConfigRepository;
import com.example.ibe_blits_backend.repositories.FilterOptionsRepository;
import com.example.ibe_blits_backend.repositories.FiltersRepository;
import com.example.ibe_blits_backend.repositories.GuestTypeRepository;
import com.example.ibe_blits_backend.repositories.PriceRepository;
import com.example.ibe_blits_backend.repositories.PropertyRepository;
import com.example.ibe_blits_backend.repositories.RoomSpecRepository;
import com.example.ibe_blits_backend.repositories.RoomTypeRepository;
import com.example.ibe_blits_backend.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final GuestTypeRepository guestTypeRepository;
    private final RoomSpecRepository roomSpecRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final FilterConfigRepository filterConfigRepository;
    private final FiltersRepository filtersRepository;
    private final FilterOptionsRepository filterOptionsRepository;
    private final PriceRepository priceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (tenantRepository.count() > 0) {
            log.info("Skipping seed: data already exists.");
            return;
        }

        Tenant tenant = Tenant.builder()
                .tenantName("kickdrum")
                .tenantLogo("https://example.com/logo.png")
                .tenantBanner("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1600&q=80")
                .tenantCopyright("© Kickdrum IBE 2026")
                .build();
        tenant = tenantRepository.save(tenant);

        Property property = Property.builder()
                .propertyName("IBE Tokyo Hotel")
                .tenant(tenant)
                .guestAllowed(4)
                .guestFlag(true)
                .roomCount(4)
                .lengthOfStay(7)
                .roomFlag(true)
                .accessibleFlag(true)
                .build();
        property = propertyRepository.save(property);

        GuestType adult = GuestType.builder()
                .property(property)
                .guestTypeName("Adult")
                .minAge(18)
                .maxAge(120)
                .build();
        GuestType child = GuestType.builder()
                .property(property)
                .guestTypeName("Child")
                .minAge(2)
                .maxAge(17)
                .build();
        guestTypeRepository.saveAll(List.of(adult, child));

        RoomSpec standardSpec = RoomSpec.builder()
                .bedType("Queen")
                .area(new BigDecimal("25.00"))
                .minOcc(1)
                .maxOcc(2)
                .quantity(20)
                .build();
        RoomSpec familySpec = RoomSpec.builder()
                .bedType("Twin")
                .area(new BigDecimal("32.00"))
                .minOcc(1)
                .maxOcc(4)
                .quantity(10)
                .build();
        roomSpecRepository.saveAll(List.of(standardSpec, familySpec));

        RoomType standard = RoomType.builder()
                .property(property)
                .roomTypeName("Standard Room")
                .roomSpec(standardSpec)
                .build();
        RoomType family = RoomType.builder()
                .property(property)
                .roomTypeName("Family Suite")
                .roomSpec(familySpec)
                .build();
        roomTypeRepository.saveAll(List.of(standard, family));

        FilterConfig filterConfig = FilterConfig.builder()
                .property(property)
                .build();
        filterConfig = filterConfigRepository.save(filterConfig);

        Filters amenities = Filters.builder()
                .filterName("Amenities")
                .filterConfig(filterConfig)
                .build();
        Filters view = Filters.builder()
                .filterName("View")
                .filterConfig(filterConfig)
                .build();
        filtersRepository.saveAll(List.of(amenities, view));

        filterOptionsRepository.saveAll(List.of(
                FilterOptions.builder().filter(amenities).value("Wifi").build(),
                FilterOptions.builder().filter(amenities).value("Breakfast").build(),
                FilterOptions.builder().filter(view).value("City").build(),
                FilterOptions.builder().filter(view).value("Sea").build()
        ));

        LocalDate start = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            Date current = Date.valueOf(start.plusDays(i));
            priceRepository.save(Prices.builder()
                    .roomType(standard)
                    .property(property)
                    .roomPrice(new BigDecimal("12000.00"))
                    .quantity(20)
                    .date(current)
                    .build());
            priceRepository.save(Prices.builder()
                    .roomType(family)
                    .property(property)
                    .roomPrice(new BigDecimal("18500.00"))
                    .quantity(10)
                    .date(current)
                    .build());
        }

        log.info("Seed complete. tenantId={} propertyId={}", tenant.getTenantId(), property.getPropertyId());
    }
}
