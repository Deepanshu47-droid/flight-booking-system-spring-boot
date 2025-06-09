package com.flight_search_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightRequestDto {

    @Null(message = "Don't add flight number it will be generated automatically")
    private String flightNumber;

    @NotBlank(message = "Airline name must not be blank")
    private String airline;

    @NotBlank(message = "Source must not be blank")
    private String source;

    @NotBlank(message = "Destination must not be blank")
    private String destination;

    @NotNull(message = "Departure date must not be null")
    @FutureOrPresent(message = "Departure date must be today or in the future")
    private LocalDate departureDate;

    @NotNull(message = "Departure time must not be null")
    private LocalTime departureTime;

    @Valid
    @NotNull(message = "Fare details must not be null")
    private FareDetailsDto fareDetails;

}
