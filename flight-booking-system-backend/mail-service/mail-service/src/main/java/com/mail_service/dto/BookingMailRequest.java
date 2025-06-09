package com.mail_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingMailRequest {
    private String bookingId;
    private String name;
    private String email;
    private String contact;
    private String flightNumber;
    private String bookingStatus;
    private String seatType;
    private int noOfSeats;
    private String paymentStatus;
    private String paymentId;
}

