package com.ibe.ibe_blitz_backend.controllers;
import com.ibe.ibe_blitz_backend.dto.FilterRoomResultsInputDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResponseDto;
import com.ibe.ibe_blitz_backend.service.FilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FilterGraphqlController {
    private final FilterService filterService;

    @QueryMapping
    public RoomSearchResponseDto filterRoomResults(@Argument FilterRoomResultsInputDto input) {
        return filterService.filterRoomResults(input);
    }
}

