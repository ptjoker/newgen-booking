package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Complaint;
import com.newgen.bookingsystem.entity.ComplaintStatus;
import com.newgen.bookingsystem.entity.ComplaintType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

    // Get all complaints ordered by creation date (latest first)
    List<Complaint> findAllByOrderByCreatedAtDesc();

    // Get complaints by user ID
    List<Complaint> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    // Get complaints by provider ID
    List<Complaint> findByProvider_ProviderIdOrderByCreatedAtDesc(Integer providerId);

    // Get complaints by status
    List<Complaint> findByStatus(ComplaintStatus status);

    // Get complaints by type
    List<Complaint> findByType(ComplaintType type);

    // Count by status (for statistics)
    long countByStatus(ComplaintStatus status);
}