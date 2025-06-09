package com.booking_service.external;

import com.booking_service.dto.PaymentStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payment/create-order")
    Map<String, Object> createOrder(@RequestParam("amount") double amount,
                                    @RequestParam("currency") String currency,
                                    @RequestParam("receiptId") String receiptId);

    @GetMapping("/api/payment/verify-payment")
    PaymentStatusResponse verifyPayment(@RequestParam("orderId") String orderId);

    @PostMapping("/api/payment/refund")
    String refundPayment(@RequestParam("paymentId") String paymentId);
}
