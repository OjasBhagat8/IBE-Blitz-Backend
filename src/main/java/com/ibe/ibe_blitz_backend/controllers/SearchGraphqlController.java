package com.ibe.ibe_blitz_backend.controllers;

import com.ibe.ibe_blitz_backend.dto.PromoCodeApplyRequestDto;
import com.ibe.ibe_blitz_backend.dto.PromotionDealResponseDto;
import com.ibe.ibe_blitz_backend.dto.RoomDealsRequestDto;
import com.ibe.ibe_blitz_backend.dto.RoomDealsResponseDto;
import com.ibe.ibe_blitz_backend.dto.RoomSearchResultDto;
import com.ibe.ibe_blitz_backend.dto.SearchRoomsInputDto;
import com.ibe.ibe_blitz_backend.service.PromoCodeService;
import com.ibe.ibe_blitz_backend.service.RoomDealsService;
import com.ibe.ibe_blitz_backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchGraphqlController {
    private final SearchService searchService;
    private final RoomDealsService roomDealsService;
    private final PromoCodeService promoCodeService;

    @QueryMapping
    public List<RoomSearchResultDto> searchRooms(@Argument SearchRoomsInputDto input) {
        return searchService.searchRooms(input);
    }

    @QueryMapping
    public RoomDealsResponseDto roomDeals(@Argument RoomDealsRequestDto input) {
        return roomDealsService.getRoomDeals(
                input.getRoomTypeId(),
                input.getPropertyId(),
                input.getCheckIn(),
                input.getCheckOut(),
                input.getGuestSelections()
        );
    }

    @QueryMapping
    public PromotionDealResponseDto applyPromoCode(@Argument PromoCodeApplyRequestDto input) {
        return promoCodeService.applyPromoCode(input);
    }
}


