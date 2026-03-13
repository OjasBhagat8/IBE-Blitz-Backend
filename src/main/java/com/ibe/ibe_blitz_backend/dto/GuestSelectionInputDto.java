package com.ibe.ibe_blitz_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuestSelectionInputDto {
    private String guestTypeName;
    private Integer count;
}
