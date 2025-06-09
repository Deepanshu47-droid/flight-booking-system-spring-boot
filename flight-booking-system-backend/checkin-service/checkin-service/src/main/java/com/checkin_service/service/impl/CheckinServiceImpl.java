package com.checkin_service.service.impl;

import com.checkin_service.dto.CheckinMailRequestDTO;
import com.checkin_service.external.MailServiceClient;
import com.checkin_service.repositories.CheckinRepository;
import com.checkin_service.dto.CheckinResponseDTO;
import com.checkin_service.external.BookingServiceClient;
import com.checkin_service.mapper.CheckinMapper;
import com.checkin_service.model.Booking;
import com.checkin_service.model.Checkin;
import com.checkin_service.model.SeatType;
import com.checkin_service.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final BookingServiceClient bookingServiceClient;
    private final MailServiceClient mailServiceClient;
    private final CheckinRepository checkinRepository;


    public String buildSeatNumber(SeatType seatType, int seatNumber) {
        String seatAbbr = seatType.name().substring(0, 2).toUpperCase();
        return seatAbbr + "-" + String.format("%03d", seatNumber);
    }

    @Override
    public CheckinResponseDTO checkinPassenger(String bookingId, String username) {

        Booking booking = bookingServiceClient.getBookingByIdForUser(bookingId, username);

        Checkin checkin = Checkin.builder()
                .bookingId(booking.getBookingId())
                .seatType(booking.getSeatType())
                .noOfSeats(booking.getNoOfSeats())
                .username(booking.getUsername())
                .build();

        Integer lastSeat = checkinRepository.findMaxLastSeatBySeatType(booking.getSeatType());

        if(lastSeat == null) {
            lastSeat = 0;
        }

        List<String> seats = new ArrayList<>();
        for(int i=1; i<=booking.getNoOfSeats(); i++) {
            seats.add(buildSeatNumber(booking.getSeatType(), lastSeat+i+1));
        }
        checkin.setAssignedSeats(seats);

        checkin.setLastSeat(lastSeat + booking.getNoOfSeats());

        checkin = checkinRepository.save(checkin);

        CheckinMailRequestDTO mailRequest = new CheckinMailRequestDTO(
                checkin.getBookingId(),
                checkin.getCheckinId(),
                booking.getEmail(),
                checkin.getAssignedSeats()
        );

        bookingServiceClient.updateBookingStatusToCheckedIn(booking.getBookingId());
        mailServiceClient.sendCheckinMail(mailRequest);

        return CheckinMapper.toDto(checkin);
    }

    @Override
    public List<CheckinResponseDTO> getAllCheckins() {
        List<Checkin> checkins = checkinRepository.findAll();

        if (checkins.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No checkins available.");
        }
        return checkins.stream()
                .map(CheckinMapper::toDto)
                .toList();
    }
}
