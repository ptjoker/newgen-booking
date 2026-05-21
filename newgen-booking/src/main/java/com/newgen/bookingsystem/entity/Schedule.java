package com.newgen.bookingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;
    
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
    
    @Column(name = "day_of_week")
    private Byte dayOfWeek; // Changed from Integer to Byte to match TINYINT in database
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "break_start")
    private LocalTime breakStart;
    
    @Column(name = "break_end")
    private LocalTime breakEnd;
    
    @Column(name = "max_bookings_per_slot")
    private Integer maxBookingsPerSlot = 1;
    
    @Column(name = "is_available", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isAvailable = true;
    
    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Schedule() {}
    
    public Schedule(Provider provider, LocalTime startTime, LocalTime endTime) {
        this.provider = provider;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = true;
        this.maxBookingsPerSlot = 1;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getScheduleId() {
        return scheduleId;
    }
    
    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }
    
    public Provider getProvider() {
        return provider;
    }
    
    public void setProvider(Provider provider) {
        this.provider = provider;
    }
    
    public Byte getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Byte dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public LocalTime getBreakStart() {
        return breakStart;
    }
    
    public void setBreakStart(LocalTime breakStart) {
        this.breakStart = breakStart;
    }
    
    public LocalTime getBreakEnd() {
        return breakEnd;
    }
    
    public void setBreakEnd(LocalTime breakEnd) {
        this.breakEnd = breakEnd;
    }
    
    public Integer getMaxBookingsPerSlot() {
        return maxBookingsPerSlot;
    }
    
    public void setMaxBookingsPerSlot(Integer maxBookingsPerSlot) {
        this.maxBookingsPerSlot = maxBookingsPerSlot;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }
    
    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
    
    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
    
    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
