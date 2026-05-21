package com.newgen.bookingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_actions")
public class AdminAction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Integer actionId;
    
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;
    
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // CREATE, UPDATE, DELETE, VERIFY, LOGIN, etc.
    
    @Column(name = "target_type", length = 50)
    private String targetType; // USER, PROVIDER, BOOKING, PAYMENT, etc.
    
    @Column(name = "target_id")
    private Integer targetId;
    
    @Column(name = "target_name", length = 255)
    private String targetName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "action_date")
    private LocalDateTime actionDate;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    // Constructors
    public AdminAction() {}
    
    public AdminAction(User admin, String actionType, String targetType, Integer targetId, 
                       String targetName, String description, String ipAddress) {
        this.admin = admin;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.description = description;
        this.ipAddress = ipAddress;
        this.actionDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getActionId() { return actionId; }
    public void setActionId(Integer actionId) { this.actionId = actionId; }
    
    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    
    public Integer getTargetId() { return targetId; }
    public void setTargetId(Integer targetId) { this.targetId = targetId; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

