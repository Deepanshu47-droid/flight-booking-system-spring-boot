package com.checkin_service.external;

import com.checkin_service.model.Booking;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {

    @GetMapping("/api/bookings/user/{bookingId}")
    Booking getBookingByIdForUser(
            @PathVariable("bookingId") String bookingId,
            @RequestHeader("X-Username") String username
    );

    @PutMapping("/api/bookings/status/{bookingId}")
    String updateBookingStatusToCheckedIn(@PathVariable String bookingId);
}

