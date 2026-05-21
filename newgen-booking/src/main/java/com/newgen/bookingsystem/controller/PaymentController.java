package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.*;
import com.newgen.bookingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getPaymentByBookingId(@PathVariable Integer bookingId) {
        Optional<Payment> payment = paymentRepository.findByBooking_BookingId(bookingId);
        if (payment.isPresent()) {
            return ResponseEntity.ok(payment.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<?> getPaymentByTransactionId(@PathVariable String transactionId) {
        Optional<Payment> payment = paymentRepository.findByTransactionId(transactionId);
        return payment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPaymentsByUser(@PathVariable Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found with ID: " + userId);
        }
        List<Payment> payments = paymentRepository.findByBooking_User_UserId(userId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getPaymentsByProvider(@PathVariable Integer providerId) {
        List<Payment> payments = paymentRepository.findByBooking_Provider_ProviderId(providerId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable String status) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            List<Payment> payments = paymentRepository.findByStatus(paymentStatus);
            return ResponseEntity.ok(payments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<?> getPaymentsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<Payment> payments = paymentRepository.findByPaymentDateBetween(start, end);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use: YYYY-MM-DDTHH:MM:SS");
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody Payment payment) {
        try {
            // Validate booking
            if (payment.getBooking() == null || payment.getBooking().getBookingId() == null) {
                return ResponseEntity.badRequest().body("Booking ID is required");
            }
            
            Optional<Booking> booking = bookingRepository.findById(payment.getBooking().getBookingId());
            if (!booking.isPresent()) {
                return ResponseEntity.badRequest().body("Booking not found with ID: " + 
                    payment.getBooking().getBookingId());
            }
            
            // Check if payment already exists for this booking
            Optional<Payment> existingPayment = paymentRepository.findByBooking_BookingId(
                payment.getBooking().getBookingId());
            if (existingPayment.isPresent()) {
                return ResponseEntity.badRequest().body("Payment already exists for this booking");
            }
            
            payment.setBooking(booking.get());
            
            // Generate transaction ID if not provided
            if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
                payment.setTransactionId("TXN" + System.currentTimeMillis());
            }
            
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentDate(LocalDateTime.now());
            
            Payment savedPayment = paymentRepository.save(payment);
            return new ResponseEntity<>(savedPayment, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating payment: " + e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completePayment(@PathVariable Integer id) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        
        // Update booking status if needed
        Booking booking = payment.getBooking();
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        return ResponseEntity.ok(updatedPayment);
    }
    
    @PatchMapping("/{id}/fail")
    public ResponseEntity<?> failPayment(@PathVariable Integer id) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.FAILED);
        
        Payment updatedPayment = paymentRepository.save(payment);
        return ResponseEntity.ok(updatedPayment);
    }
    
    @PostMapping("/{id}/refund")
    public ResponseEntity<?> processRefund(
            @PathVariable Integer id,
            @RequestParam Double refundAmount,
            @RequestParam(required = false) String reason,
            @RequestParam Integer adminUserId) {
        
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<User> adminOpt = userRepository.findById(adminUserId);
        if (!adminOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Admin user not found with ID: " + adminUserId);
        }
        
        Payment payment = paymentOpt.get();
        User admin = adminOpt.get();
        
        // Check if admin has admin role
        if (!"admin".equals(admin.getRole())) {
            return ResponseEntity.badRequest().body("User is not an admin");
        }
        
        // Validate refund amount
        if (refundAmount > payment.getAmount()) {
            return ResponseEntity.badRequest().body("Refund amount cannot exceed payment amount");
        }
        
        if (refundAmount <= 0) {
            return ResponseEntity.badRequest().body("Refund amount must be positive");
        }
        
        // Process refund
        payment.setRefundAmount(refundAmount);
        payment.setRefundReason(reason);
        payment.setRefundedBy(admin);
        payment.setRefundDate(LocalDateTime.now());
        
        if (refundAmount >= payment.getAmount()) {
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        
        // Update booking status
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason("Refunded: " + (reason != null ? reason : "No reason provided"));
        bookingRepository.save(booking);
        
        Payment updatedPayment = paymentRepository.save(payment);
        return ResponseEntity.ok(updatedPayment);
    }
    
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getUserPaymentSummary(@PathVariable Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found with ID: " + userId);
        }
        
        List<Payment> payments = paymentRepository.findByBooking_User_UserId(userId);
        
        Double totalSpent = payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(0.0, Double::sum);
        
        Double totalRefunded = payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.REFUNDED || 
                         p.getStatus() == PaymentStatus.PARTIALLY_REFUNDED)
            .map(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0.0)
            .reduce(0.0, Double::sum);
        
        long completedCount = payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .count();
        
        return ResponseEntity.ok(new PaymentSummary(totalSpent, totalRefunded, completedCount, payments.size()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePayment(@PathVariable Integer id) {
        try {
            if (paymentRepository.existsById(id)) {
                paymentRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Inner class for payment summary
    static class PaymentSummary {
        public Double totalSpent;
        public Double totalRefunded;
        public long completedPayments;
        public int totalPayments;
        
        public PaymentSummary(Double totalSpent, Double totalRefunded, 
                              long completedPayments, int totalPayments) {
            this.totalSpent = totalSpent;
            this.totalRefunded = totalRefunded;
            this.completedPayments = completedPayments;
            this.totalPayments = totalPayments;
        }
    }
}
