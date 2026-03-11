package com.example.ibe_blits_backend.controllers;
import com.example.ibe_blits_backend.dto.RoomFilterDto;
import com.example.ibe_blits_backend.service.FilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class FilterGraphqlController {
    private final FilterService filterService;

    @QueryMapping
    public List<RoomFilterDto> roomFilters(@Argument UUID propertyId) {
        return filterService.getPropertyFilters(propertyId);
    }
}
