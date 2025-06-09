package com.flight_search_service.service;

import com.flight_search_service.dto.FlightRequestDto;
import com.flight_search_service.dto.FlightResponseDto;
import com.flight_search_service.dto.SearchRequestDto;
import com.flight_search_service.model.Flight;
import com.flight_search_service.model.SeatType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface FlightService {

    Flight addFlight(FlightRequestDto dto);
    List<FlightResponseDto> searchFlights(SearchRequestDto dto);
    List<FlightResponseDto> getAllFlights();
    String deleteFlightByFlightNumber(String flightNumber);
    Flight updateFlight(String flightNumber, FlightRequestDto flightRequestDto);
    FlightResponseDto getFlightByFlightNumber(String flightNumber);
    Double getFareBySeatType(String flightNumber, SeatType seatType);
    String increaseSeatsBooked(String flightNumber, SeatType seatType, int noOfSeats);
    String decreaseSeatsBooked(String flightNumber, SeatType seatType, int noOfSeats);
    String checkSeatAvailability(String flightNumber, SeatType seatType, int noOfSeats);
    boolean existsByFlightNumber(String flightNumber);
}
