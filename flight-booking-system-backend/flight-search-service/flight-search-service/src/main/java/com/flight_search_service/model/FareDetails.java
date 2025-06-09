package com.flight_search_service.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class FareDetails {

    @ElementCollection
    private Map<SeatType, Double> priceDetails;

    @ElementCollection
    private Map<SeatType, Integer> seatsBooked;

}
