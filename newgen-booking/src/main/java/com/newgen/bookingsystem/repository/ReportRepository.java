package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    List<Report> findAllByOrderByGeneratedDateDesc();

    List<Report> findByAdmin_UserIdOrderByGeneratedDateDesc(Integer adminId);

    // Add these count methods
    long countByStatus(Report.ReportStatus status);
}