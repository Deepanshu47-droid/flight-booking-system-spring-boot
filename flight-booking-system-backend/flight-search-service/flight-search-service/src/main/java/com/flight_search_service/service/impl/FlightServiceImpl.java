package com.flight_search_service.service.impl;

import com.flight_search_service.dto.FlightRequestDto;

import com.flight_search_service.dto.FlightResponseDto;
import com.flight_search_service.dto.SearchRequestDto;
import com.flight_search_service.external.BookingServiceClient;
import com.flight_search_service.mapper.FlightMapper;
import com.flight_search_service.model.FareDetails;
import com.flight_search_service.model.Flight;
import com.flight_search_service.model.SeatType;
import com.flight_search_service.repositories.FlightRepository;
import com.flight_search_service.service.FlightService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final BookingServiceClient bookingServiceClient;
    private final FlightMapper flightMapper;
    private static final int MAX_SEATS = 200;


    @Override
    public Flight addFlight(FlightRequestDto dto) {
        dto.setFlightNumber(generateFlightNumber(dto.getSource(), dto.getDestination(), dto.getDepartureDate(), dto.getDepartureTime()));
        Flight flight = flightMapper.toEntity(dto);
        return flightRepository.save(flight);
    }
    @Override
    public List<FlightResponseDto> searchFlights(SearchRequestDto dto) {
        List<FlightResponseDto> flights = flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureDate(
                        dto.getSource(), dto.getDestination(), dto.getDate()
                )
                .stream()
                .map(flightMapper::toDto)
                .toList();
        if(flights.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "No flights found.");
        }
        return flights;
    }
    @Override
    public List<FlightResponseDto> getAllFlights() {
        List<Flight> flights = flightRepository.findAll();

        List<FlightResponseDto> flightResponseDtos = flights.stream()
                .map(flightMapper::toDto)
                .toList();

        if(flightResponseDtos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "No flights found.");
        }
        return flightResponseDtos;
    }

    @Override
    @Transactional
    public String deleteFlightByFlightNumber(String flightNumber) {
        if (!flightRepository.findByFlightNumber(flightNumber).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found: " + flightNumber);
        }
        bookingServiceClient.deleteBookingsByFlightNumber(flightNumber);
        flightRepository.deleteByFlightNumber(flightNumber);

        return "Flight deleted successfully.";
    }


    @Override
    @Transactional
    public Flight updateFlight(String flightNumber, FlightRequestDto flightRequestDto){
        Flight existingFlight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found: " + flightNumber));

        if (flightRequestDto.getFlightNumber()!=null || flightRequestDto.getSource()!=null || flightRequestDto.getDestination()!=null || flightRequestDto.getDepartureDate()!=null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can't change flight number, source, destination or departure date");
        }

        existingFlight.setAirline(flightRequestDto.getAirline());

        if (flightRequestDto.getFareDetails() != null) {
            existingFlight.getFareDetails().setPriceDetails(flightRequestDto.getFareDetails().getPriceDetails());
        }

        return flightRepository.save(existingFlight);
    }

    @Override
    public FlightResponseDto getFlightByFlightNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found: " + flightNumber));
        return flightMapper.toDto(flight);
    }

    private String generateFlightNumber(String source, String destination, LocalDate departureDate, LocalTime departureTime) {
        String sourceInitial = source.trim().toUpperCase().substring(0, 1);
        String destInitial = destination.trim().toUpperCase().substring(0, 1);
        String datePart = departureDate.toString().replace("-", ""); // yyyyMMdd
        String timePart = departureTime.format(java.time.format.DateTimeFormatter.ofPattern("HHmm"));

        return sourceInitial + destInitial + "_" + datePart + "_" + timePart;
    }

    public Double getFareBySeatType(String flightNumber, SeatType seatType) {
        Optional<Flight> flightOptional = flightRepository.findById(flightNumber);

        // Check if the flight exists
        if (flightOptional.isPresent()) {
            Flight flight = flightOptional.get();

            Double fare = flight.getFareDetails().getPriceDetails().get(seatType);

            if (fare != null) {
                return fare;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat type not available for this flight.");
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found.");
    }

    public String checkSeatAvailability(String flightNumber, SeatType seatType, int noOfSeats) {
        Optional<Flight> flightOpt = flightRepository.findById(flightNumber);
        if (flightOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found");
        }

        Flight flight = flightOpt.get();
        FareDetails fareDetails = flight.getFareDetails();

        int booked = fareDetails.getSeatsBooked().getOrDefault(seatType, 0);
        int available = MAX_SEATS - booked;

        if (available >= noOfSeats) {
            return "Seats are available.";
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only " + available + " seats available.");
        }
    }

    @Transactional
    public String increaseSeatsBooked(String flightNumber, SeatType seatType, int noOfSeats) {
        if (noOfSeats <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of seats must be greater than 0");
        }

        Flight flight = flightRepository.findById(flightNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found: " + flightNumber));

        Map<SeatType, Integer> seatsBooked = flight.getFareDetails().getSeatsBooked();
        int currentBooked = seatsBooked.getOrDefault(seatType, 0);

        if (currentBooked + noOfSeats > MAX_SEATS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only " + (MAX_SEATS-currentBooked) + " Seats available.");
        }

        seatsBooked.put(seatType, currentBooked + noOfSeats);
        flightRepository.save(flight);

        return "Successfully booked " + noOfSeats + " seats for flight " + flightNumber + " (" + seatType + ")";
    }

    @Transactional
    public String decreaseSeatsBooked(String flightNumber, SeatType seatType, int noOfSeats) {
        if (noOfSeats <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of seats must be greater than 0");
        }

        Flight flight = flightRepository.findById(flightNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found: " + flightNumber));

        Map<SeatType, Integer> seatsBooked = flight.getFareDetails().getSeatsBooked();
        int currentBooked = seatsBooked.getOrDefault(seatType, 0);


        seatsBooked.put(seatType, currentBooked - noOfSeats);
        flightRepository.save(flight);

        return "Successfully cancelled " + noOfSeats + " seats for flight " + flightNumber + " (" + seatType + ")";
    }
    @Override
    public boolean existsByFlightNumber(String flightNumber) {
        return flightRepository.existsById(flightNumber);
    }

}
