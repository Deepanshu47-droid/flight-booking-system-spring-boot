package com.flight_search_service.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequestDto {

    @NotBlank(message = "Source location must not be blank")
    private String source;

    @NotBlank(message = "Destination location must not be blank")
    private String destination;

    @NotNull(message = "Date of travel must not be null")
    @FutureOrPresent(message = "Date must be today or in the future")
    private LocalDate date;
}
