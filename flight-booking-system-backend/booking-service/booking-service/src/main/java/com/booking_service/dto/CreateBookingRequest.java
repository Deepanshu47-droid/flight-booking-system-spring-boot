package com.booking_service.dto;

import com.booking_service.model.SeatType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBookingRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Contact is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact must be a 10-digit number")
    private String contact;

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotNull(message = "Seat type is required")
    private SeatType seatType;

    @Min(value = 1, message = "At least one seat should be booked")
    private int noOfSeats;

    @NotBlank(message = "Currency is required")
    private String currency;
}

