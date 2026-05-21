package com.newgen.bookingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @OneToOne
    @JoinColumn(name = "booking_id", unique = true, nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @ManyToOne
    @JoinColumn(name = "refunded_by")
    private User refundedBy;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payment() {}

    public Payment(Booking booking, Double amount, PaymentMethod paymentMethod) {
        this.booking = booking;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.paymentDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // FIX: Ensure payment date is set consistently
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // FIX: When status changes to COMPLETED, update payment date
        if (status == PaymentStatus.COMPLETED && paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }

    // Helper method for formatted date
    public String getFormattedPaymentDate() {
        if (paymentDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return paymentDate.format(formatter);
    }

    // Getters and Setters
    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
        // FIX: When setting status to COMPLETED, update payment date
        if (status == PaymentStatus.COMPLETED && this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public User getRefundedBy() {
        return refundedBy;
    }

    public void setRefundedBy(User refundedBy) {
        this.refundedBy = refundedBy;
    }

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}