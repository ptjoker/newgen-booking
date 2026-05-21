package com.newgen.bookingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Integer complaintId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintType type;
    
    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.PENDING;
    
    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;
    
    @Column(name = "responded_by")
    private Integer respondedBy;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = ComplaintStatus.PENDING;
    }
    
    // Getters and Setters
    public Integer getComplaintId() { return complaintId; }
    public void setComplaintId(Integer complaintId) { this.complaintId = complaintId; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }
    
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ComplaintType getType() { return type; }
    public void setType(ComplaintType type) { this.type = type; }
    
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }
    
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    
    public Integer getRespondedBy() { return respondedBy; }
    public void setRespondedBy(Integer respondedBy) { this.respondedBy = respondedBy; }
    
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
