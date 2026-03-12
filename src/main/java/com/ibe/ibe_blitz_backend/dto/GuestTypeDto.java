package com.ibe.ibe_blitz_backend.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestTypeDto {
    private UUID guestTypeId;
    private String guestTypeName;
    private int minAge;
    private int maxAge;
}

