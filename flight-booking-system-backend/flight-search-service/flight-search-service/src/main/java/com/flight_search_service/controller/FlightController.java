package com.flight_search_service.controller;

import com.flight_search_service.dto.FlightRequestDto;
import com.flight_search_service.dto.FlightResponseDto;
import com.flight_search_service.dto.SearchRequestDto;
import com.flight_search_service.model.Flight;
import com.flight_search_service.model.SeatType;
import com.flight_search_service.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping("/add")
    public ResponseEntity<Flight> addFlight(@Valid @RequestBody FlightRequestDto flightRequestDto) {
        Flight savedFlight = flightService.addFlight(flightRequestDto);
        return ResponseEntity.ok(savedFlight);
    }
    @PostMapping("/search")
    public ResponseEntity<List<FlightResponseDto>> searchFlights(@RequestBody @Valid SearchRequestDto requestDto) {
        List<FlightResponseDto> result = flightService.searchFlights(requestDto);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/all")
    public ResponseEntity<List<FlightResponseDto>> getAllFlights() {
        List<FlightResponseDto> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }
    @DeleteMapping("/delete/{flightNumber}")
    public ResponseEntity<String> deleteFlight(@PathVariable String flightNumber) {
        ;
        return ResponseEntity.ok(flightService.deleteFlightByFlightNumber(flightNumber));
    }

    @PutMapping("/update/{flightNumber}")
    public ResponseEntity<Flight> updateFlight(@PathVariable String flightNumber, @RequestBody FlightRequestDto flightRequestDto) {
        return ResponseEntity.ok(flightService.updateFlight(flightNumber, flightRequestDto));
    }

    @GetMapping("/get/{flightNumber}")
    public ResponseEntity<FlightResponseDto> getFlightByFlightNumber(@PathVariable String flightNumber) {

        FlightResponseDto flight = flightService.getFlightByFlightNumber(flightNumber);
        return ResponseEntity.ok(flight);

    }

    @GetMapping("/fare")
    public ResponseEntity<Double> getFare(@RequestParam String flightNumber, @RequestParam SeatType seatType){
        return ResponseEntity.ok(flightService.getFareBySeatType(flightNumber,seatType));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<String> checkSeatAvailability(
            @RequestParam String flightNumber,
            @RequestParam SeatType seatType,
            @RequestParam int noOfSeats
    ) {
        return ResponseEntity.ok(flightService.checkSeatAvailability(flightNumber, seatType, noOfSeats));
    }

    @PostMapping("/book-seats")
    public ResponseEntity<String> bookSeats(
            @RequestParam("flightNumber") String flightNumber,
            @RequestParam("seatType") SeatType seatType,
            @RequestParam("noOfSeats") int noOfSeats
    ) {
        return ResponseEntity.ok(flightService.increaseSeatsBooked(flightNumber, seatType, noOfSeats));
    }

    @PostMapping("/cancel-seats")
    public ResponseEntity<String> cancelSeats(
            @RequestParam("flightNumber") String flightNumber,
            @RequestParam("seatType") SeatType seatType,
            @RequestParam("noOfSeats") int noOfSeats
    ) {
        return ResponseEntity.ok(flightService.decreaseSeatsBooked(flightNumber, seatType, noOfSeats));
    }

    @GetMapping("/isexist/{flightNumber}")
    public boolean existByFlightNumber(@PathVariable String flightNumber) {
        return flightService.existsByFlightNumber(flightNumber);
    }

}
