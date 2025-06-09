package com.checkin_service.service;

import com.checkin_service.dto.CheckinResponseDTO;

import java.util.List;

public interface CheckinService {
    CheckinResponseDTO checkinPassenger(String bookingId, String username);
    List<CheckinResponseDTO> getAllCheckins();
}
