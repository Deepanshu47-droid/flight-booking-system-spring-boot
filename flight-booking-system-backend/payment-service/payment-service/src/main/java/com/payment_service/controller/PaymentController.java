package com.payment_service.controller;

import com.payment_service.dto.PaymentStatusResponse;
import com.payment_service.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Endpoint to create a payment order
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createPaymentOrder(@RequestParam double amount, @RequestParam String currency, @RequestParam String receiptId) {
        Order order = paymentService.createOrder(amount, currency, receiptId);

        String redirectUrl = String.format(
                "http://localhost:8080/payment/booking.html?orderId=%s&amount=%d&currency=%s",
                order.get("id"),
                (int)(order.get("amount")),
                order.get("currency")
        );
        Map<String, Object> response = order.toJson().toMap();
        response.put("payment_url", redirectUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-payment")
    public ResponseEntity<?> verifyPaymentStatus(@RequestParam String orderId) {
        try {
            String paymentId = paymentService.isPaymentCaptured(orderId);
            String status = paymentId!=null ? "paid" : "pending";

            return ResponseEntity.ok(new PaymentStatusResponse(orderId, paymentId, status));
        } catch (RazorpayException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Endpoint to process a refund
    @PostMapping("/refund")
    public ResponseEntity<String> refundPayment(@RequestParam String paymentId) {
        try {
            paymentService.refundPayment(paymentId);
            return ResponseEntity.ok("Refund successfully");
        } catch (RazorpayException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
