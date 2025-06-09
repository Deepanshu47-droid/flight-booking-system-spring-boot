package com.auth_service.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mail-service")
public interface MailClient {

    @PostMapping("/api/mail/generate-and-send-otp")
    String generateAndSendOtp(@RequestParam String email);

    @PostMapping("/api/mail/verify-otp")
    boolean verifyOtp(@RequestParam String email, @RequestParam String otp);
}