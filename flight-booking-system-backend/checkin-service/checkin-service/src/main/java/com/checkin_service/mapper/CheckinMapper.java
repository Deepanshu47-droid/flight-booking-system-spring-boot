package com.checkin_service.mapper;

import com.checkin_service.dto.CheckinResponseDTO;
import com.checkin_service.model.Checkin;
import com.checkin_service.model.SeatType;

import java.util.List;

public class CheckinMapper {

    public static CheckinResponseDTO toDto(Checkin checkin) {
        return CheckinResponseDTO.builder()
                .checkinId(checkin.getCheckinId())
                .bookingId(checkin.getBookingId())
                .seatType(checkin.getSeatType())
                .noOfSeats(checkin.getNoOfSeats())
                .assignedSeats(checkin.getAssignedSeats())
                .build();
    }
}