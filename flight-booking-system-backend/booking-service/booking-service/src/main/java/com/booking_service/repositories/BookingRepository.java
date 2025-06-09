package com.booking_service.repositories;

import com.booking_service.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingId(String bookingId);

    List<Booking> findByUsername(String username);

    Optional<Booking> findByBookingIdAndUsername(String bookingId, String username);

    List<Booking> findByFlightNumber(String flightNumber);

    void deleteAllByFlightNumber(String flightNumber);
}
