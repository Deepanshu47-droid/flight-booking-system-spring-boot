package com.mail_service.service;

import com.mail_service.dto.BookingMailRequest;
import com.mail_service.dto.CheckinMailRequestDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final Map<String, String> otpStorage = new HashMap<>();

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void generateAndSendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        logger.info("Generated OTP for {}: {}", email, otp);
        sendOtpEmail(email.trim(), otp);
    }

    public boolean verifyOtp(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(email);
            logger.info("OTP verified for {}", email);
            return true;
        } else {
            logger.warn("OTP verification failed for {}", email);
            return false;
        }
    }

    private void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP from Flight Booking System is: " + otp);

        logger.info("Sending email to {}", email);
        mailSender.send(message); // Will throw exception if config is wrong
        logger.info("Email sent successfully to {}", email);
    }

    public void sendBookingEmail(BookingMailRequest booking) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(booking.getEmail());
        message.setSubject("Booking Confirmation - " + booking.getBookingId());

        String mailText = "Hello " + booking.getName() + ",\n\n" +
                "Thank you for your booking!\n\n" +
                "Booking ID: " + booking.getBookingId() + "\n" +
                "Flight Number: " + booking.getFlightNumber() + "\n" +
                "Seats: " + booking.getNoOfSeats() + " (" + booking.getSeatType() + ")\n" +
                "Booking Status: " + booking.getBookingStatus() + "\n" +
                "Payment Status: " + booking.getPaymentStatus() + "\n" +
                "Payment ID: " + booking.getPaymentId() + "\n" +
                "Thank you,\nFlight Booking System";

        message.setText(mailText);

        logger.info("Sending booking email to {}", booking.getEmail());
        mailSender.send(message);
    }
    public void sendBookingCancellationEmail(String email, String bookingId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Booking Cancelled: " + bookingId);
        message.setText("Dear customer,\n\nYour booking with ID " + bookingId + " has been cancelled successfully.\n You will be refunded in next 24 working hours.\n\nRegards,\nFlight Booking Team");

        logger.info("Sending cancellation email to {}", email);
        mailSender.send(message);
        logger.info("Cancellation email sent successfully to {}", email);
    }

    public String sendCheckinMail(CheckinMailRequestDTO checkin) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(checkin.getEmail());
        message.setSubject("Check-in Confirmation - Booking ID: " + checkin.getBookingId());

        String mailText = "Dear Passenger,\n\n" +
                "Your check-in is successful!\n\n" +
                "Booking ID: " + checkin.getBookingId() + "\n" +
                "Check-in ID: " + checkin.getCheckinId() + "\n" +
                "Assigned Seats: " + String.join(", ", checkin.getAssignedSeats()) + "\n\n" +
                "We wish you a pleasant journey.\n\n" +
                "Regards,\nFlight Booking Team";

        message.setText(mailText);

        logger.info("Sending check-in email to {}", checkin.getEmail());
        mailSender.send(message);
        logger.info("Check-in email sent successfully to {}", checkin.getEmail());

        return "Checkin mail sent successfully.";
    }
}
