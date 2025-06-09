package com.payment_service.service;

import com.payment_service.exceptions.PaymentServiceException;
import com.razorpay.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final RazorpayClient razorpayClient;

    private final String keyId;

    private final String keySecret;

    public PaymentService(
            @Value("${razorpay.key.id}") String razorpayKeyId,
            @Value("${razorpay.key.secret}") String razorpayKeySecret
    ) {
        this.keyId = razorpayKeyId;
        this.keySecret = razorpayKeySecret;
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            logger.info("RazorpayClient initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize RazorpayClient", e);
            throw new RuntimeException("Failed to initialize RazorpayClient", e);
        }
    }

    // Method to create an order and initiate payment
    public Order createOrder(double amount, String currency, String receiptId) {

        try {
            logger.info("Creating Razorpay order with amount: {}, currency: {}, receiptId: {}", amount, currency, receiptId);
            JSONObject options = new JSONObject();
            options.put("amount", amount * 100); // Convert to paise (1 INR = 100 paise)
            options.put("currency", currency);
            options.put("receipt", receiptId);

            // Creating the order
            Order order = razorpayClient.orders.create(options);
            logger.info("Razorpay order created successfully.");
            return order;

        } catch (Exception e) {
            logger.error("Error creating Razorpay payment", e);
            throw new PaymentServiceException("Error creating Razorpay payment.", e);
        }
    }

    public String isPaymentCaptured(String orderId) throws RazorpayException {
        List<Payment> payments = razorpayClient.orders.fetchPayments(orderId);

        for (Payment payment : payments) {
            String status = payment.get("status");
            if ("captured".equalsIgnoreCase(status)) {
                return payment.get("id").toString();
            }
        }

        return null;
    }

    public Refund refundPayment(String paymentId) throws RazorpayException {

        Payment payment = razorpayClient.payments.fetch(paymentId);
        logger.info("Payment fetched: {}", payment);

        String status = payment.get("status");

        if (!"captured".equals(status)) {
            logger.warn("Payment status is not 'captured'. Current status: {}", status);

            if ("authorized".equals(status)) {
                JSONObject captureRequest = new JSONObject();
                payment = razorpayClient.payments.capture(paymentId, captureRequest);
                logger.info("Payment captured successfully.");
            } else {
                throw new PaymentServiceException("Payment is not captured. Current status: " + status);
            }
        }

        // Create refund
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("payment_id", paymentId);

        Refund refund = razorpayClient.refunds.create(refundRequest);
        logger.info("Refund created successfully.");
        return refund;

    }

}
