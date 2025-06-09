package com.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentStatusResponse {
    private String orderId;
    private String paymentId;
    private String status;
}