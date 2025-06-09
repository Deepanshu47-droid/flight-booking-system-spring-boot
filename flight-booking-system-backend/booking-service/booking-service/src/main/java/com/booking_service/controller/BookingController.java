package com.booking_service.controller;

import com.booking_service.dto.CreateBookingRequest;
import com.booking_service.model.Booking;
import com.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create-booking")
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader("X-Username") String username
    ) {
        Booking createdBooking = bookingService.createBooking(request, username);
        return ResponseEntity.ok(createdBooking);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable String bookingId, @RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId, username));
    }
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable String bookingId, @RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, username));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();

        if (bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "No bookings found.");
        }

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/all")
    public ResponseEntity<List<Booking>> getUserBookings(@RequestHeader("X-Username") String username) {
        List<Booking> bookings = bookingService.getBookingsByUsername(username);

        if (bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "No booking found");
        }

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{bookingId}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable String bookingId,
            @RequestHeader("X-Username") String username
    ) {
        return ResponseEntity.ok(bookingService.getBookingByIdForUser(bookingId, username));
    }

    @DeleteMapping("/delete/flight/{flightNumber}")
    public ResponseEntity<String> deleteBookingsByFlightNumber(@PathVariable String flightNumber) {
        return ResponseEntity.ok(bookingService.deleteBookingsByFlightNumber(flightNumber));
    }

    @PutMapping("/status/{bookingId}")
    public ResponseEntity<String> updateBookingStatusToCheckedIn(@PathVariable String bookingId) {
        bookingService.updateStatusToCheckedIn(bookingId);
        return ResponseEntity.ok("Booking status updated to CHECKED_IN");
    }

}
