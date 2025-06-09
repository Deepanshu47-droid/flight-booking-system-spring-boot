package com.mail_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckinMailRequestDTO {
    private String bookingId;
    private Long checkinId;
    private String email;
    private List<String> assignedSeats;
}
