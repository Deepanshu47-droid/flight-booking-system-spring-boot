package com.flight_search_service.repositories;

import com.flight_search_service.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {
    List<Flight> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureDate(String source, String destination, LocalDate departureDate);
    Optional<Flight> findByFlightNumber(String flightNumber);
    void deleteByFlightNumber(String flightNumber);
}
