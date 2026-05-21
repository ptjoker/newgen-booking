package com.newgen.bookingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Integer eventId;
    
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
    
    @Column(name = "event_name", length = 255)
    private String eventName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType = EventType.OTHER;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(nullable = false, length = 255)
    private String location;
    
    @Column(name = "venue_details", length = 255)
    private String venueDetails;
    
    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;
    
    @Column(name = "available_tickets", nullable = false)
    private Integer availableTickets;
    
    @Column
    private Double price;
    
    @Column(name = "early_bird_price")
    private Double earlyBirdPrice;
    
    @Column(name = "group_discount", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean groupDiscount = false;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.DRAFT;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Event() {}
    
    public Event(Provider provider, String eventName, LocalDateTime eventDate, 
                 String location, Integer totalTickets, Double price) {
        this.provider = provider;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.location = location;
        this.totalTickets = totalTickets;
        this.availableTickets = totalTickets;
        this.price = price;
        this.status = EventStatus.DRAFT;
        this.eventType = EventType.OTHER;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (availableTickets == null) {
            availableTickets = totalTickets;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getEventId() {
        return eventId;
    }
    
    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }
    
    public Provider getProvider() {
        return provider;
    }
    
    public void setProvider(Provider provider) {
        this.provider = provider;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    public LocalDateTime getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getVenueDetails() {
        return venueDetails;
    }
    
    public void setVenueDetails(String venueDetails) {
        this.venueDetails = venueDetails;
    }
    
    public Integer getTotalTickets() {
        return totalTickets;
    }
    
    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }
    
    public Integer getAvailableTickets() {
        return availableTickets;
    }
    
    public void setAvailableTickets(Integer availableTickets) {
        this.availableTickets = availableTickets;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Double getEarlyBirdPrice() {
        return earlyBirdPrice;
    }
    
    public void setEarlyBirdPrice(Double earlyBirdPrice) {
        this.earlyBirdPrice = earlyBirdPrice;
    }
    
    public Boolean getGroupDiscount() {
        return groupDiscount;
    }
    
    public void setGroupDiscount(Boolean groupDiscount) {
        this.groupDiscount = groupDiscount;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public EventStatus getStatus() {
        return status;
    }
    
    public void setStatus(EventStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
