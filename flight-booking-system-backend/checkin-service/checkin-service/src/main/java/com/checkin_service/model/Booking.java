package com.checkin_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    private String bookingId;

    private String name;

    private String email;

    private String contact;
    private String flightNumber;

    private BookingStatus bookingStatus;

    private SeatType seatType;

    private int noOfSeats;

    private String username;

    private String currency;

    private String orderId;

    private String paymentUrl;

    private String paymentId;
}
