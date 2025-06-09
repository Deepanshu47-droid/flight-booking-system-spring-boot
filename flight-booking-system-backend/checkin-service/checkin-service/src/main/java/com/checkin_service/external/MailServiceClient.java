package com.checkin_service.external;

import com.checkin_service.dto.CheckinMailRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mail-service")
public interface MailServiceClient {

    @PostMapping("/api/mail/send-checkin-mail")
    String sendCheckinMail(@RequestBody CheckinMailRequestDTO checkinMailRequestDTO);
}