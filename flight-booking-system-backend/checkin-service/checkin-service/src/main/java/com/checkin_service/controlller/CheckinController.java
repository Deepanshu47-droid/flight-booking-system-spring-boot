package com.checkin_service.controlller;

import com.checkin_service.dto.CheckinResponseDTO;
import com.checkin_service.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<CheckinResponseDTO> checkinPassenger(
            @PathVariable String bookingId,
            @RequestHeader("X-Username") String username) {

        CheckinResponseDTO response = checkinService.checkinPassenger(bookingId, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CheckinResponseDTO>> getAllCheckins() {
        return ResponseEntity.ok(checkinService.getAllCheckins());
    }
}
