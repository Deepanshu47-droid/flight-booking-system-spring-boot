package com.mail_service.controller;

import com.mail_service.dto.BookingMailRequest;
import com.mail_service.dto.CheckinMailRequestDTO;
import com.mail_service.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/generate-and-send-otp")
    public ResponseEntity<String> generateAndSendOtp(@RequestParam String email) {
        mailService.generateAndSendOtp(email);
        return ResponseEntity.ok("OTP sent successfully to " + email);
    }

    @PostMapping("/verify-otp")
    public boolean verifyOtp(@RequestParam String email, @RequestParam String otp) {
        return mailService.verifyOtp(email, otp);
    }

    @PostMapping("/send-booking-mail")
    public ResponseEntity<String> sendBookingMail(@RequestBody BookingMailRequest booking) {
        mailService.sendBookingEmail(booking);
        return ResponseEntity.ok("Booking email sent to " + booking.getEmail());
    }
    @PostMapping("/send-cancellation-mail")
    public ResponseEntity<String> sendCancellationMail(
            @RequestParam String email,
            @RequestParam String bookingId
    ) {
        mailService.sendBookingCancellationEmail(email, bookingId);
        return ResponseEntity.ok("Cancellation email sent to " + email);
    }

    @PostMapping("/send-checkin-mail")
    public ResponseEntity<String> sendCheckinMail(@RequestBody CheckinMailRequestDTO request) {
        return ResponseEntity.ok(mailService.sendCheckinMail(request));
    }
}
