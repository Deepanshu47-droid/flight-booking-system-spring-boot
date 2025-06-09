package com.flight_search_service.dto;

import com.flight_search_service.model.SeatType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareDetailsDto {

    @NotEmpty(message = "Price details map cannot be empty")
    private Map<SeatType, @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") Double> priceDetails;

    // No need to take booked from admin — will be set to 0 in mapper
}

