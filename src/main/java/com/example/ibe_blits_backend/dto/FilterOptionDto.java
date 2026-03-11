package com.example.ibe_blits_backend.dto;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterOptionDto {
    private UUID optionId;
    private String value;
}
