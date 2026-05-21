package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
    List<Notification> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);
    long countByUser_UserIdAndIsReadFalse(Integer userId);
}
