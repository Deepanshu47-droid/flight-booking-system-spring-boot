package com.booking_service.external;

import com.booking_service.dto.BookingMailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mail-service")
public interface MailServiceClient {

    @PostMapping("/api/mail/send-booking-mail")
    String  sendBookingMail(@RequestBody BookingMailRequest bookingMailRequest);

    @PostMapping("/api/mail/send-cancellation-mail")
    String sendCancellationMail(
            @RequestParam("email") String email,
            @RequestParam("bookingId") String bookingId
    );
}