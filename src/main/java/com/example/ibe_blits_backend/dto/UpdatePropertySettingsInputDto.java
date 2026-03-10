package com.example.ibe_blits_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdatePropertySettingsInputDto {
    private UUID propertyId;
    private Integer guestAllowed;
    private Boolean guestFlag;
    private Integer roomCount;
    private Integer lengthOfStay;
    private Boolean roomFlag;
    private Boolean accessibleFlag;
}
