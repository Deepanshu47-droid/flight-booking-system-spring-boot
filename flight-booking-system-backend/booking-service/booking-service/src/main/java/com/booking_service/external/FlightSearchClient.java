package com.booking_service.external;

import com.booking_service.model.SeatType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "flight-search-service")
public interface FlightSearchClient {

    @GetMapping("/api/flights/fare")
    Double getFare(
            @RequestParam("flightNumber") String flightNumber,
            @RequestParam("seatType") SeatType seatType
    );

    @PostMapping("/api/flights/book-seats")
    String bookSeats(
            @RequestParam("flightNumber") String flightNumber,
            @RequestParam("seatType") SeatType seatType,
            @RequestParam("noOfSeats") int noOfSeats
    );

    @PostMapping("/api/flights/cancel-seats")
    String cancelSeats(
            @RequestParam("flightNumber") String flightNumber,
            @RequestParam("seatType") SeatType seatType,
            @RequestParam("noOfSeats") int noOfSeats
    );

    @GetMapping("/api/flights/isexist/{flightNumber}")
    boolean existByFlightNumber(@PathVariable String flightNumber);

    @GetMapping("/api/flights/check-availability")
    String checkSeatAvailability(
            @RequestParam("flightNumber") String flightNumber,
            @RequestParam("seatType") SeatType seatType,
            @RequestParam("noOfSeats") int noOfSeats
    );
}
