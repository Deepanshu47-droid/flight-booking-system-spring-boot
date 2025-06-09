package com.booking_service.service;

import com.booking_service.dto.CreateBookingRequest;
import org.springframework.http.ResponseEntity;
import com.booking_service.model.Booking;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BookingService {

    Booking createBooking(CreateBookingRequest request, String username);

    Booking confirmBooking(String bookingId, String username);

    String cancelBooking(String bookingId, String username);

    List<Booking> getAllBookings();

    List<Booking> getBookingsByUsername(String username);

    Booking getBookingByIdForUser(String bookingId, String username);

    String deleteBookingsByFlightNumber(String flightNumber);

    void updateStatusToCheckedIn(String bookingId);
}

