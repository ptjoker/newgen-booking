package com.newgen.bookingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false)
    private BookingType bookingType;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "time_slot")
    private LocalTime timeSlot;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests = 1;

    @Column(name = "special_requests", length = 255)
    private String specialRequests;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "checked_in", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean checkedIn = false;

    @Column(name = "checked_in_time")
    private LocalDateTime checkedInTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "booking_reference", length = 255)
    private String bookingReference;

    public Booking() {}

    public Booking(User user, Provider provider, BookingType bookingType,
                   LocalDate bookingDate, Double totalAmount) {
        this.user = user;
        this.provider = provider;
        this.bookingType = bookingType;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.numberOfGuests = 1;
        this.status = BookingStatus.PENDING;
        this.checkedIn = false;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingReference == null) {
            bookingReference = "BK" + System.currentTimeMillis();
        }
        // FIX: Ensure checked_in_time is set properly when checking in
        if (checkedIn != null && checkedIn && checkedInTime == null) {
            checkedInTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // FIX: When checked_in becomes true, set checked_in_time
        if (checkedIn != null && checkedIn && checkedInTime == null) {
            checkedInTime = LocalDateTime.now();
        }
    }

    // Helper method to get formatted date
    public String getFormattedBookingDate() {
        if (bookingDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return bookingDate.format(formatter);
    }

    // Helper method to get formatted time
    public String getFormattedTimeSlot() {
        if (timeSlot == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return timeSlot.format(formatter);
    }

    // Getters and Setters
    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(LocalTime timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Boolean getCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
        // FIX: When setting checked_in to true, automatically set checked_in_time
        if (checkedIn != null && checkedIn && this.checkedInTime == null) {
            this.checkedInTime = LocalDateTime.now();
        }
    }

    public LocalDateTime getCheckedInTime() {
        return checkedInTime;
    }

    public void setCheckedInTime(LocalDateTime checkedInTime) {
        this.checkedInTime = checkedInTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
}