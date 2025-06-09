package com.booking_service.service.impl;

import com.booking_service.dto.BookingMailRequest;
import com.booking_service.dto.CreateBookingRequest;
import com.booking_service.dto.PaymentStatusResponse;
import com.booking_service.external.FlightSearchClient;
import com.booking_service.external.MailServiceClient;
import com.booking_service.external.PaymentClient;
import com.booking_service.mapper.BookingMailMapper;
import com.booking_service.model.Booking;
import com.booking_service.model.BookingStatus;
import com.booking_service.model.PaymentStatus;
import com.booking_service.repositories.BookingRepository;
import com.booking_service.service.BookingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final PaymentClient paymentClient;
    private final FlightSearchClient flightSearchClient;
    private final MailServiceClient mailServiceClient;
    private final BookingRepository bookingRepository;

    public String buildBookingId(Booking booking) {
        String seatAbbr = booking.getSeatType().name().substring(0, 2).toUpperCase();
        return booking.getFlightNumber() + "-" + seatAbbr + "-" + String.format("%04d", booking.getId());
    }

    @Override
    public Booking createBooking(CreateBookingRequest request, String username) {
        log.info("Creating booking for user: {}, flightNumber: {}", username, request.getFlightNumber());

        if (!flightSearchClient.existByFlightNumber(request.getFlightNumber())) {
            log.warn("Flight not found: {}", request.getFlightNumber());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found.");
        }

        log.info("Checking seat availability for flight: {}", request.getFlightNumber());
        flightSearchClient.checkSeatAvailability(request.getFlightNumber(), request.getSeatType(), request.getNoOfSeats());

        Booking booking = Booking.builder()
                .name(request.getName())
                .email(request.getEmail())
                .contact(request.getContact())
                .flightNumber(request.getFlightNumber())
                .seatType(request.getSeatType())
                .noOfSeats(request.getNoOfSeats())
                .username(username)
                .currency(request.getCurrency())
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking saved with ID: {}", booking.getId());

        String bookingId = buildBookingId(booking);
        booking.setBookingId(bookingId);

        Double amountResponse = flightSearchClient.getFare(booking.getFlightNumber(), booking.getSeatType());
        double amount = amountResponse * booking.getNoOfSeats();
        log.info("Fare calculated: {} {}", amount, booking.getCurrency());

        Map<String, Object> order = paymentClient.createOrder(amount, booking.getCurrency(), booking.getBookingId());
        booking.setPaymentUrl(order.get("payment_url").toString());
        booking.setOrderId(order.get("id").toString());

        log.info("Payment order created: orderId={}, paymentUrl={}", booking.getOrderId(), booking.getPaymentUrl());
        return bookingRepository.save(booking);
    }

    @Override
    public Booking confirmBooking(String bookingId, String username) {
        log.info("Confirming booking for bookingId: {}, user: {}", bookingId, username);

        Optional<Booking> bookingResponse = bookingRepository.findByBookingId(bookingId);

        if (!bookingResponse.isPresent()) {
            log.warn("Booking not found: {}", bookingId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking Id not found.");
        }

        Booking booking = bookingResponse.get();

        if (!booking.getUsername().equals(username)) {
            log.warn("Booking username mismatch for bookingId: {}", bookingId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking Id not found.");
        }

        PaymentStatusResponse paymentStatusResponse = paymentClient.verifyPayment(booking.getOrderId());
        log.info("Payment status for orderId {}: {}", booking.getOrderId(), paymentStatusResponse.getStatus());

        if (booking.getBookingStatus().equals(BookingStatus.CANCELLED)) {
            log.warn("Booking already cancelled for bookingId: {}", bookingId);
            if (paymentStatusResponse.getStatus().equalsIgnoreCase("paid")) {
                paymentClient.refundPayment(booking.getPaymentId());
                booking.setPaymentStatus(PaymentStatus.REFUNDED);
                bookingRepository.save(booking);
                log.info("Refund initiated for bookingId: {}", bookingId);
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking were cancelled if you done the payment you will be refunded in next 24 working hours.");
        }

        if (paymentStatusResponse.getStatus().equalsIgnoreCase("pending")) {
            log.warn("Payment is still pending for bookingId: {}", bookingId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment not completed. Please complete payment to proceed.");
        }

        try {
            log.info("Booking seats for flight: {}", booking.getFlightNumber());
            flightSearchClient.bookSeats(booking.getFlightNumber(), booking.getSeatType(), booking.getNoOfSeats());
        } catch (FeignException ex) {
            log.error("Seat booking failed: {}", ex.getMessage());
            if (paymentStatusResponse.getStatus().equalsIgnoreCase("paid")) {
                paymentClient.refundPayment(booking.getPaymentId());
                booking.setPaymentStatus(PaymentStatus.REFUNDED);
            }
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            throw new ResponseStatusException(HttpStatus.valueOf(ex.status()), ex.getMessage() + " if you done the payment you will be refunded in next 24 working hours.");
        }

        booking.setPaymentId(paymentStatusResponse.getPaymentId());
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        log.info("Booking confirmed for bookingId: {}", bookingId);
        BookingMailRequest mailRequest = BookingMailMapper.toBookingMailRequest(booking);
        mailServiceClient.sendBookingMail(mailRequest);
        log.info("Booking confirmation email sent to {}", booking.getEmail());

        return booking;
    }

    @Override
    public String cancelBooking(String bookingId, String username) {
        log.info("Cancelling booking with bookingId: {} for user: {}", bookingId, username);

        Optional<Booking> bookingResponse = bookingRepository.findByBookingId(bookingId);

        if (!bookingResponse.isPresent()) {
            log.warn("Booking not found: {}", bookingId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking Id not found.");
        }

        Booking booking = bookingResponse.get();

        if (!booking.getUsername().equals(username)) {
            log.warn("Username mismatch while cancelling bookingId: {}", bookingId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking Id not found.");
        }

        if (booking.getBookingStatus().equals(BookingStatus.CANCELLED)) {
            log.info("Booking already cancelled: {}", bookingId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking Already cancelled.");
        }

        paymentClient.refundPayment(booking.getPaymentId());
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.REFUNDED);

        bookingRepository.save(booking);
        log.info("Booking cancelled and refunded for bookingId: {}", bookingId);

        flightSearchClient.cancelSeats(booking.getFlightNumber(), booking.getSeatType(), booking.getNoOfSeats());

        mailServiceClient.sendCancellationMail(booking.getEmail(), booking.getBookingId());
        log.info("Cancellation email sent to {}", booking.getEmail());

        return "Booking with Booking Id " + booking.getBookingId() + " cancelled successfully.";
    }

    @Override
    public List<Booking> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll();
    }

    @Override
    public List<Booking> getBookingsByUsername(String username) {
        log.info("Fetching bookings for user: {}", username);
        return bookingRepository.findByUsername(username);
    }

    @Override
    public Booking getBookingByIdForUser(String bookingId, String username) {
        log.info("Fetching booking for bookingId: {}, user: {}", bookingId, username);
        Optional<Booking> bookingOptional = bookingRepository.findByBookingIdAndUsername(bookingId, username);

        if (bookingOptional.isEmpty()) {
            log.warn("Booking not found for bookingId: {}, user: {}", bookingId, username);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found.");
        }

        return bookingOptional.get();
    }

    @Override
    public String deleteBookingsByFlightNumber(String flightNumber) {
        log.info("Deleting all bookings for flight number: {}", flightNumber);

        List<Booking> bookings = bookingRepository.findByFlightNumber(flightNumber);

        if (bookings.isEmpty()) {
            log.warn("No bookings found for flight number: {}", flightNumber);
            throw new ResponseStatusException(HttpStatus.OK, "No bookings found for flight number: " + flightNumber);
        }

        bookingRepository.deleteAllByFlightNumber(flightNumber);
        log.info("All bookings deleted for flight number: {}", flightNumber);

        return "All bookings for flight number '" + flightNumber + "' have been deleted.";
    }

    @Override
    public void updateStatusToCheckedIn(String bookingId) {
        log.info("Updating booking status to CHECKED_IN for bookingId: {}", bookingId);

        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking not found for bookingId: {}", bookingId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
                });

        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);

        log.info("Booking status updated to CHECKED_IN for bookingId: {}", bookingId);
    }
}
