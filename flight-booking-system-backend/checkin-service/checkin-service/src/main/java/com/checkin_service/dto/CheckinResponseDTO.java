package com.checkin_service.dto;

import com.checkin_service.model.SeatType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinResponseDTO {

    private Long checkinId;

    private String bookingId;

    private SeatType seatType;

    private int noOfSeats;

    private List<String> assignedSeats;
}
