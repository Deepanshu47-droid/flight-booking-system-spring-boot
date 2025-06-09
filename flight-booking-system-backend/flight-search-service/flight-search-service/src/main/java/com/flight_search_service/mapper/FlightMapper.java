package com.flight_search_service.mapper;

import com.flight_search_service.dto.FareDetailsDto;
import com.flight_search_service.dto.FlightRequestDto;
import com.flight_search_service.dto.FlightResponseDto;
import com.flight_search_service.model.FareDetails;
import com.flight_search_service.model.Flight;
import com.flight_search_service.model.SeatType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class FlightMapper {


    private final FareDetails fareDetails;

    public Flight toEntity(FlightRequestDto dto) {

        fareDetails.setPriceDetails(dto.getFareDetails().getPriceDetails());

        Map<SeatType, Integer> bookedMap = new HashMap<>();

        for (SeatType type : fareDetails.getPriceDetails().keySet()) {
            bookedMap.put(type, 0);
        }

        fareDetails.setSeatsBooked(bookedMap);

        return Flight.builder()
                .flightNumber(dto.getFlightNumber())
                .airline(dto.getAirline())
                .source(dto.getSource())
                .destination(dto.getDestination())
                .departureDate(dto.getDepartureDate())
                .departureTime(dto.getDepartureTime())
                .fareDetails(fareDetails)
                .build();
    }

    public FlightResponseDto toDto(Flight flight) {
        return FlightResponseDto.builder()
                .flightNumber(flight.getFlightNumber())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .source(flight.getSource())
                .destination(flight.getDestination())
                .departureDate(flight.getDepartureDate())
                .departureTime(flight.getDepartureTime())
                .fareDetails(flight.getFareDetails())
                .build();
    }
}

