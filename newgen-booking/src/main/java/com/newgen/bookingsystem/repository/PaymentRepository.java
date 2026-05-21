package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Payment;
import com.newgen.bookingsystem.entity.PaymentMethod;
import com.newgen.bookingsystem.entity.PaymentStatus;
import com.newgen.bookingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Find payment by booking ID (one-to-one relationship)
    Optional<Payment> findByBooking_BookingId(Integer bookingId);
    
    // Find payments by status
    List<Payment> findByStatus(PaymentStatus status);
    
    // Find payments by payment method
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    // Find payments by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Find payments by date range
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find payments by user (via booking)
    List<Payment> findByBooking_User_UserId(Integer userId);
    
    // Find payments by provider (via booking)
    List<Payment> findByBooking_Provider_ProviderId(Integer providerId);
    
    // Find refunded payments
    List<Payment> findByStatusIn(List<PaymentStatus> statuses);
    
    // Find payments refunded by a specific admin
    List<Payment> findByRefundedBy(User refundedBy);
}
