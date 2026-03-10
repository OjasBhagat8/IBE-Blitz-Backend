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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private static final List<RoomTemplate> ROOM_TEMPLATES = List.of(new RoomTemplate("Deluxe King", "King Bed", "320.00", 1, 2, "6400", 8, "Comfort room with king bed, work desk, and walk-in shower.",
                    List.of("Wifi", "Smart TV", "Work Desk", "Tea Coffee Maker")),new RoomTemplate("Executive Twin", "Twin Bed", "380.00", 1, 3, "7200", 6,"Twin room for business or small family stays with extra seating space.",
                    List.of("Wifi", "Smart TV", "Breakfast", "Mini Fridge")),new RoomTemplate("Family Studio", "Queen + Twin", "450.00", 2, 4, "8600", 5, "Open-plan studio designed for family trips with lounge corner.",
                    List.of("Wifi", "Smart TV", "Sofa Bed", "Kitchenette")),new RoomTemplate("Club Suite", "King Bed", "560.00", 2, 4, "10200", 4, "Premium suite with separate living area and club access.",
                    List.of("Wifi", "Smart TV", "Lounge Access", "Bathtub")),new RoomTemplate("Presidential Suite", "King Bed", "720.00", 2, 5, "14800", 2,"Top-tier suite with dining area, premium bath, and city views.",
                    List.of("Wifi", "Smart TV", "Jacuzzi", "Butler Service"))
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (tenantRepository.count() > 1) {
            log.info("Skipping seed: data already exists.");
            return;
        }

        Tenant radison = tenantRepository.save(Tenant.builder()
                .tenantName("Radison")
                .tenantLogo("https://1000logos.net/wp-content/uploads/2020/01/Radisson-Logo.png")
                .tenantBanner("https://1000logos.net/wp-content/uploads/2020/01/Radisson-Logo.png")
                .tenantCopyright("(c) Radison")
                .build());

        Tenant hilton = tenantRepository.save(Tenant.builder()
                .tenantName("Hilton")
                .tenantLogo("https://upload.wikimedia.org/wikipedia/commons/8/8a/Hilton_Worldwide_logo.svg")
                .tenantBanner("https://upload.wikimedia.org/wikipedia/commons/8/8a/Hilton_Worldwide_logo.svg")
                .tenantCopyright("(c) Hilton")
                .build());

        List<Property> properties = propertyRepository.saveAll(List.of(
                property("Radison Mumbai", radison, 4, true, 4, 3, false, true),
                property("Radison Bangalore", radison, 3, false, 5, 5, true, false),
                property("Radison Delhi", radison, 5, true, 3, 2, true, false),
                property("Hilton Delhi", hilton, 2, false, 4, 4, false, true),
                property("Hilton Bangalore", hilton, 6, true, 3, 1, true, true)
        ));

        Map<String, Property> propertyByName = new LinkedHashMap<>();
        for (Property property : properties) {
            propertyByName.put(property.getPropertyName(), property);
        }

        List<GuestType> guestTypes = new ArrayList<>();
        addGuestTypes(guestTypes, propertyByName.get("Radison Mumbai"), List.of(
                guestTypeDef("Children", 3, 12),
                guestTypeDef("Adults", 13, 59)
        ));
        addGuestTypes(guestTypes, propertyByName.get("Radison Bangalore"), List.of(
                guestTypeDef("Children", 3, 12),
                guestTypeDef("Adults", 13, 59)
        ));
        addGuestTypes(guestTypes, propertyByName.get("Radison Delhi"), List.of(
                guestTypeDef("Children", 3, 12),
                guestTypeDef("Adults", 13, 59)
        ));
        addGuestTypes(guestTypes, propertyByName.get("Hilton Delhi"), List.of(
                guestTypeDef("Toddlers", 0, 2),
                guestTypeDef("Children", 3, 12),
                guestTypeDef("Adults", 13, 59),
                guestTypeDef("Senior Citizen", 60, 120)
        ));
        addGuestTypes(guestTypes, propertyByName.get("Hilton Bangalore"), List.of(
                guestTypeDef("Toddlers", 0, 2),
                guestTypeDef("Children", 3, 12),
                guestTypeDef("Adults", 13, 59),
                guestTypeDef("Senior Citizen", 60, 120)
        ));
        guestTypeRepository.saveAll(guestTypes);

        List<RoomSpec> roomSpecs = roomSpecRepository.saveAll(ROOM_TEMPLATES.stream()
                .map(template -> RoomSpec.builder()
                        .bedType(template.bedType())
                        .area(new BigDecimal(template.area()))
                        .minOcc(template.minOcc())
                        .maxOcc(template.maxOcc())
                        .build())
                .toList());

        List<RoomType> roomTypes = new ArrayList<>();
        for (Property property : properties) {
            for (int i = 0; i < ROOM_TEMPLATES.size(); i++) {
                RoomTemplate template = ROOM_TEMPLATES.get(i);
                roomTypes.add(RoomType.builder()
                        .property(property)
                        .roomTypeName(property.getPropertyName() + " " + template.roomLabel())
                        .description(template.description())
                        .occupancy(template.maxOcc())
                        .amenities(template.amenities())
                        .baseRate(new BigDecimal(template.basePrice()).setScale(2))
                        .roomSpec(roomSpecs.get(i))
                        .build());
            }
        }
        roomTypes = roomTypeRepository.saveAll(roomTypes);

        List<Prices> prices = new ArrayList<>();
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(90);
        for (RoomType roomType : roomTypes) {
            RoomTemplate template = roomTemplateFor(roomType);
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                int dow = date.getDayOfWeek().getValue() % 7; // Sunday=0, Monday=1 ... Saturday=6
                BigDecimal amount = new BigDecimal(template.basePrice())
                        .add(BigDecimal.valueOf((long) dow * 350L))
                        .setScale(2);
                prices.add(Prices.builder()
                        .roomType(roomType)
                        .property(roomType.getProperty())
                        .roomPrice(amount)
                        .quantity(template.availableRooms())
                        .date(Date.valueOf(date))
                        .build());
            }
        }
        priceRepository.saveAll(prices);

        seedFilterConfig(properties);

        log.info("Seed complete. tenants={} properties={} roomTypes={} prices={}",
                tenantRepository.count(),
                propertyRepository.count(),
                roomTypeRepository.count(),
                priceRepository.count());
    }

    private Property property(
            String name,
            Tenant tenant,
            Integer guestAllowed,
            Boolean guestFlag,
            Integer roomCount,
            Integer lengthOfStay,
            Boolean roomFlag,
            Boolean accessibleFlag
    ) {
        return Property.builder()
                .propertyName(name)
                .tenant(tenant)
                .guestAllowed(guestAllowed)
                .guestFlag(guestFlag)
                .roomCount(roomCount)
                .lengthOfStay(lengthOfStay)
                .roomFlag(roomFlag)
                .accessibleFlag(accessibleFlag)
                .build();
    }

    private GuestTypeDef guestTypeDef(String name, Integer minAge, Integer maxAge) {
        return new GuestTypeDef(name, minAge, maxAge);
    }

    private void addGuestTypes(List<GuestType> target, Property property, List<GuestTypeDef> defs) {
        for (GuestTypeDef def : defs) {
            target.add(GuestType.builder()
                    .property(property)
                    .guestTypeName(def.name())
                    .minAge(def.minAge())
                    .maxAge(def.maxAge())
                    .build());
        }
    }

    private void seedFilterConfig(List<Property> properties) {
        for (Property property : properties) {
            FilterConfig config = filterConfigRepository.save(FilterConfig.builder()
                    .property(property)
                    .build());

            Filters amenities = Filters.builder()
                    .filterName("Amenities")
                    .filterConfig(config)
                    .build();
            Filters view = Filters.builder()
                    .filterName("View")
                    .filterConfig(config)
                    .build();
            filtersRepository.saveAll(List.of(amenities, view));

            filterOptionsRepository.saveAll(List.of(
                    FilterOptions.builder().filter(amenities).value("Wifi").build(),
                    FilterOptions.builder().filter(amenities).value("Breakfast").build(),
                    FilterOptions.builder().filter(view).value("City").build(),
                    FilterOptions.builder().filter(view).value("Sea").build()
            ));
        }
    }

    private record GuestTypeDef(String name, Integer minAge, Integer maxAge) {
    }

    private RoomTemplate roomTemplateFor(RoomType roomType) {
        for (RoomTemplate template : ROOM_TEMPLATES) {
            if (roomType.getRoomTypeName() != null && roomType.getRoomTypeName().endsWith(template.roomLabel())) {
                return template;
            }
        }
        return ROOM_TEMPLATES.get(0);
    }

    private record RoomTemplate(
            String roomLabel,
            String bedType,
            String area,
            Integer minOcc,
            Integer maxOcc,
            String basePrice,
            Integer availableRooms,
            String description,
            List<String> amenities
    ) {
    }
}
