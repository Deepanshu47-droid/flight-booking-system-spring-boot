package com.flight_search_service.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    private String flightNumber;
    private String airline;
    private String source;
    private String destination;

    private LocalDate departureDate;
    private LocalTime departureTime;

    @Embedded
    private FareDetails fareDetails;
}

