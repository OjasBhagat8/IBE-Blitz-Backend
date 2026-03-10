package com.example.ibe_blits_backend.controllers;

import com.example.ibe_blits_backend.dto.RoomSearchResultDto;
import com.example.ibe_blits_backend.dto.SearchRoomsInputDto;
import com.example.ibe_blits_backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchGraphqlController {
    private final SearchService searchService;

    @QueryMapping
    public List<RoomSearchResultDto> searchRooms(@Argument SearchRoomsInputDto input) {
        return searchService.searchRooms(input);
    }
}

