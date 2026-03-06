package com.example.ibe_blits_backend.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyConfigDto {
    private UUID propertyId;
    private String propertyName;
    private Integer guestAllowed;
    private Boolean guestFlag;
    private Integer roomCount;
    private Integer lengthOfStay;
    private Boolean roomFlag;
    private Boolean accessibleFlag;
    private List<GuestTypeDto> guestTypes;
}
