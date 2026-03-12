package com.ibe.ibe_blitz_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Schema<?> graphQlRequestSchema = new ObjectSchema()
                .addProperty("query", new Schema<String>().type("string"))
                .addProperty("operationName", new Schema<String>().type("string").nullable(true))
                .addProperty("variables", new ObjectSchema().additionalProperties(true).nullable(true));

        Schema<?> graphQlResponseSchema = new ObjectSchema()
                .addProperty("data", new ObjectSchema().additionalProperties(true).nullable(true))
                .addProperty("errors", new Schema<>().type("array").nullable(true));

        Example tenantsExample = new Example().value(Map.of(
                "query", "query Tenants { tenants { tenantId tenantName } }",
                "operationName", "Tenants",
                "variables", Map.of()
        ));

        Example configExample = new Example().value(Map.of(
                "query", "query Config($tenantId: ID!) { config(tenantId: $tenantId) { tenantId tenantName tenantLogo tenantBanner tenantCopyright properties { propertyId propertyName guestAllowed guestFlag roomCount lengthOfStay roomFlag accessibleFlag guestTypes { guestTypeId guestTypeName minAge maxAge } } } }",
                "operationName", "Config",
                "variables", Map.of(
                        "tenantId", "3610cc5b-d939-4c91-a0b6-bc7c4cda0ecd"
                )
        ));

        Example configByTenantNameExample = new Example().value(Map.of(
                "query", "query ConfigByTenantName($tenantName: String!) { configByTenantName(tenantName: $tenantName) { tenantId tenantName properties { propertyId propertyName } } }",
                "operationName", "ConfigByTenantName",
                "variables", Map.of(
                        "tenantName", "hilton"
                )
        ));

        Example calendarPricesExample = new Example().value(Map.of(
                "query", "query CalendarPrices($tenantId: ID!, $propertyId: ID!, $startDate: String) { calendarPrices(tenantId: $tenantId, propertyId: $propertyId, startDate: $startDate) { date propertyId roomTypeId price } }",
                "operationName", "CalendarPrices",
                "variables", Map.of(
                        "tenantId", "3610cc5b-d939-4c91-a0b6-bc7c4cda0ecd",
                        "propertyId", "b6299de0-2340-4275-a127-04d69896afb6",
                        "startDate", "2026-04-01"
                )
        ));

        Example roomFiltersExample = new Example().value(Map.of(
                "query", "query RoomFilters($propertyId: ID!) { roomFilters(propertyId: $propertyId) { filterId filterName options { optionId value } } }",
                "operationName", "RoomFilters",
                "variables", Map.of(
                        "propertyId", "b6299de0-2340-4275-a127-04d69896afb6"
                )
        ));

        Example filterRoomResultsExample = new Example().value(Map.of(
                "query", "query FilterRoomResults($input: FilterRoomResultsInput!) { filterRoomResults(input: $input) { roomTypeId roomTypeName description occupancy amenities images baseRate roomSpec { roomSpecId bedType area minOcc maxOcc } totalPrice availableCount } }",
                "operationName", "FilterRoomResults",
                "variables", Map.of(
                        "input", Map.of(
                                "tenantId", "3610cc5b-d939-4c91-a0b6-bc7c4cda0ecd",
                                "propertyId", "b6299de0-2340-4275-a127-04d69896afb6",
                                "checkIn", "2026-04-01",
                                "checkOut", "2026-04-03",
                                "rooms", 2,
                                "accessible", true,
                                "filters", List.of(
                                        Map.of(
                                                "filterName", "Bed Type",
                                                "options", List.of("King Bed", "Twin Bed")
                                        ),
                                        Map.of(
                                                "filterName", "Amenities",
                                                "options", List.of("Wifi", "Breakfast")
                                        )
                                )
                        )
                )
        ));

        Example searchRoomsExample = new Example().value(Map.of(
                "query", "query SearchRooms($input: SearchRoomsInput!) { searchRooms(input: $input) { roomTypeId roomTypeName description occupancy amenities baseRate roomSpec { roomSpecId bedType area minOcc maxOcc } totalPrice availableCount } }",
                "operationName", "SearchRooms",
                "variables", Map.of(
                        "input", Map.of(
                                "tenantId", "3610cc5b-d939-4c91-a0b6-bc7c4cda0ecd",
                                "propertyId", "b6299de0-2340-4275-a127-04d69896afb6",
                                "checkIn", "2026-04-01",
                                "checkOut", "2026-04-03",
                                "rooms", 2,
                                "accessible", true
                        )
                )
        ));

        Example pricesExample = new Example().value(Map.of(
                "query", "query Prices($propertyId: ID!, $fromDate: String!, $toDate: String!) { prices(propertyId: $propertyId, fromDate: $fromDate, toDate: $toDate) { priceId roomTypeId roomTypeName propertyId date roomPrice quantity } }",
                "operationName", "Prices",
                "variables", Map.of(
                        "propertyId", "b6299de0-2340-4275-a127-04d69896afb6",
                        "fromDate", "2026-04-01",
                        "toDate", "2026-04-05"
                )
        ));

        Example updateTenantExample = new Example().value(Map.of(
                "query", "mutation UpdateTenant($input: UpdateTenantInput!) { updateTenant(input: $input) { tenantId tenantName tenantLogo tenantBanner tenantCopyright } }",
                "operationName", "UpdateTenant",
                "variables", Map.of(
                        "input", Map.of(
                                "tenantId", "3610cc5b-d939-4c91-a0b6-bc7c4cda0ecd",
                                "tenantName", "Radison Updated",
                                "tenantLogo", "https://example.com/radison-logo.png",
                                "tenantBanner", "https://example.com/radison-banner.png",
                                "tenantCopyright", "(c) Radison"
                        )
                )
        ));

        Example updatePropertySettingsExample = new Example().value(Map.of(
                "query", "mutation UpdatePropertySettings($input: UpdatePropertySettingsInput!) { updatePropertySettings(input: $input) { propertyId propertyName guestAllowed guestFlag roomCount lengthOfStay roomFlag accessibleFlag guestTypes { guestTypeId guestTypeName minAge maxAge } } }",
                "operationName", "UpdatePropertySettings",
                "variables", Map.of(
                        "input", Map.of(
                                "propertyId", "b6299de0-2340-4275-a127-04d69896afb6",
                                "guestAllowed", 4,
                                "guestFlag", true,
                                "roomCount", 5,
                                "lengthOfStay", 5,
                                "roomFlag", true,
                                "accessibleFlag", true
                        )
                )
        ));

        Example upsertRoomPriceExample = new Example().value(Map.of(
                "query", "mutation UpsertRoomPrice($input: UpsertRoomPriceInput!) { upsertRoomPrice(input: $input) { priceId roomTypeId roomTypeName propertyId date roomPrice quantity } }",
                "operationName", "UpsertRoomPrice",
                "variables", Map.of(
                        "input", Map.of(
                                "roomTypeId", "b6299de0-2340-4275-a127-04d69896afb6",
                                "date", "2026-04-01",
                                "roomPrice", 6400.0,
                                "quantity", 8
                        )
                )
        ));

        Map<String, Example> graphqlExamples = new LinkedHashMap<>();
        graphqlExamples.put("tenants", tenantsExample);
        graphqlExamples.put("config", configExample);
        graphqlExamples.put("configByTenantName", configByTenantNameExample);
        graphqlExamples.put("calendarPrices", calendarPricesExample);
        graphqlExamples.put("roomFilters", roomFiltersExample);
        graphqlExamples.put("filterRoomResults", filterRoomResultsExample);
        graphqlExamples.put("searchRooms", searchRoomsExample);
        graphqlExamples.put("prices", pricesExample);
        graphqlExamples.put("updateTenant", updateTenantExample);
        graphqlExamples.put("updatePropertySettings", updatePropertySettingsExample);
        graphqlExamples.put("upsertRoomPrice", upsertRoomPriceExample);

        Operation graphqlOperation = new Operation()
                .summary("Execute GraphQL operations")
                .description("Use this endpoint to run queries and mutations defined in schema.graphqls.")
                .requestBody(new RequestBody()
                        .required(true)
                        .content(new Content().addMediaType("application/json", new MediaType()
                                .schema(graphQlRequestSchema)
                                .examples(graphqlExamples))))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("GraphQL response")
                                .content(new Content().addMediaType("application/json", new MediaType()
                                        .schema(graphQlResponseSchema)))));

        return new OpenAPI()
                .info(new Info()
                        .title("IBE Blitz Backend API")
                        .version("v1")
                        .description("Swagger documents the HTTP GraphQL endpoint. Use /graphiql for the GraphQL IDE."))
                .components(new Components()
                        .addSchemas("GraphQLRequest", graphQlRequestSchema)
                        .addSchemas("GraphQLResponse", graphQlResponseSchema))
                .paths(new Paths()
                        .addPathItem("/api/graphql", new PathItem().post(graphqlOperation)));
    }
}

