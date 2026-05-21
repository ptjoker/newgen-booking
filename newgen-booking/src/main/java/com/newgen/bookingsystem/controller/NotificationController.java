package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.Notification;
import com.newgen.bookingsystem.repository.NotificationRepository;
import com.newgen.bookingsystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping("/user/{userId}")
    public List<Notification> getNotificationsByUser(@PathVariable Integer userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }
    
    @GetMapping("/user/{userId}/unread")
    public List<Notification> getUnreadNotifications(@PathVariable Integer userId) {
        return notificationRepository.findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    @GetMapping("/user/{userId}/unread-count")
    public long getUnreadCount(@PathVariable Integer userId) {
        return notificationService.getUnreadCount(userId);
    }
    
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer id) {
        Optional<Notification> notif = notificationRepository.findById(id);
        if (notif.isPresent()) {
            notif.get().setIsRead(true);
            notif.get().setReadAt(LocalDateTime.now());
            notificationRepository.save(notif.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@PathVariable Integer userId) {
        List<Notification> notifications = notificationRepository.findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification notif : notifications) {
            notif.setIsRead(true);
            notif.setReadAt(LocalDateTime.now());
            notificationRepository.save(notif);
        }
        return ResponseEntity.ok().build();
    }
}
