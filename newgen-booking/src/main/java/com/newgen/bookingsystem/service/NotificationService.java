package com.newgen.bookingsystem.service;

import com.newgen.bookingsystem.entity.Notification;
import com.newgen.bookingsystem.entity.User;
import com.newgen.bookingsystem.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // Send notification when booking is created
    public void sendBookingCreatedNotification(User user, Integer bookingId, String bookingReference, String bookingType, String bookingDate) {
        Notification notif = new Notification(
            user,
            "Booking Created",
            "Your " + bookingType + " booking (Reference: " + bookingReference + ") for " + bookingDate + " has been created successfully. Please wait for confirmation.",
            Notification.NotificationType.booking
        );
        notif.setReferenceId(bookingId);
        notif.setReferenceType("booking");
        notificationRepository.save(notif);
    }
    
    // Send notification when booking is confirmed
    public void sendBookingConfirmedNotification(User user, Integer bookingId, String bookingReference, String bookingType, String bookingDate) {
        Notification notif = new Notification(
            user,
            "Booking Confirmed",
            "Your " + bookingType + " booking (Reference: " + bookingReference + ") for " + bookingDate + " has been CONFIRMED.",
            Notification.NotificationType.booking
        );
        notif.setReferenceId(bookingId);
        notif.setReferenceType("booking");
        notificationRepository.save(notif);
    }
    
    // Send notification when booking is cancelled
    public void sendBookingCancelledNotification(User user, Integer bookingId, String bookingReference, String bookingType, String reason) {
        Notification notif = new Notification(
            user,
            "Booking Cancelled",
            "Your " + bookingType + " booking (Reference: " + bookingReference + ") has been CANCELLED. Reason: " + reason,
            Notification.NotificationType.booking
        );
        notif.setReferenceId(bookingId);
        notif.setReferenceType("booking");
        notificationRepository.save(notif);
    }
    
    // Send notification when payment is received
    public void sendPaymentReceivedNotification(User user, Integer paymentId, Double amount, String bookingReference) {
        Notification notif = new Notification(
            user,
            "Payment Received",
            "Payment of R" + amount + " for booking " + bookingReference + " has been received.",
            Notification.NotificationType.payment
        );
        notif.setReferenceId(paymentId);
        notif.setReferenceType("payment");
        notificationRepository.save(notif);
    }
    
    // Send reminder notification
    public void sendReminderNotification(User user, String message, Integer referenceId) {
        Notification notif = new Notification(
            user,
            "Reminder",
            message,
            Notification.NotificationType.reminder
        );
        notif.setReferenceId(referenceId);
        notif.setReferenceType("booking");
        notificationRepository.save(notif);
    }
    
    // Get unread count for a user
    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }
}
