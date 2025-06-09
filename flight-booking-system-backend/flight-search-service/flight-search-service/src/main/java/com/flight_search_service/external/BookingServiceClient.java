package com.flight_search_service.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service") // Replace with the actual service name in your config
public interface BookingServiceClient {

    @DeleteMapping("/api/bookings/delete/flight/{flightNumber}")
    String deleteBookingsByFlightNumber(@PathVariable("flightNumber") String flightNumber);
}