package com.booking_service.mapper;

import com.booking_service.dto.BookingMailRequest;
import com.booking_service.model.Booking;

public class BookingMailMapper {

    public static BookingMailRequest toBookingMailRequest(Booking booking) {
        return BookingMailRequest.builder()
                .bookingId(booking.getBookingId())
                .name(booking.getName())
                .email(booking.getEmail())
                .contact(booking.getContact())
                .flightNumber(booking.getFlightNumber())
                .bookingStatus(
                        booking.getBookingStatus().name()
                )
                .seatType(
                        booking.getSeatType().name()
                )
                .noOfSeats(booking.getNoOfSeats())
                .paymentStatus(
                        booking.getPaymentStatus().name()
                )
                .paymentId(booking.getPaymentId())
                .build();
    }
}
