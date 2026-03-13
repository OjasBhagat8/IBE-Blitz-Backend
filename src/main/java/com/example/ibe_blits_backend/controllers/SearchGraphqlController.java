package com.example.ibe_blits_backend.controllers;

import com.example.ibe_blits_backend.dto.RoomSearchResultDto;
import com.example.ibe_blits_backend.dto.PromoCodeApplyRequestDto;
import com.example.ibe_blits_backend.dto.PromotionDealResponseDto;
import com.example.ibe_blits_backend.dto.RoomPricingRequestDto;
import com.example.ibe_blits_backend.dto.RoomPricingResponseDto;
import com.example.ibe_blits_backend.dto.SearchRoomsInputDto;
import com.example.ibe_blits_backend.service.PromoCodeService;
import com.example.ibe_blits_backend.service.RoomPricingService;
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
    private final RoomPricingService roomPricingService;
    private final PromoCodeService promoCodeService;

    @QueryMapping
    public List<RoomSearchResultDto> searchRooms(@Argument SearchRoomsInputDto input) {
        return searchService.searchRooms(input);
    }

    @QueryMapping
    public RoomPricingResponseDto roomPricingOptions(@Argument RoomPricingRequestDto input) {
        return roomPricingService.getPricingOptions(
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

