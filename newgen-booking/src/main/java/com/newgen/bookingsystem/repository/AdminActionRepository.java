package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Integer> {
    
    // Find all actions ordered by action date (latest first)
    List<AdminAction> findAllByOrderByActionDateDesc();
    
    // Find actions by admin ID ordered by action date
    List<AdminAction> findByAdmin_UserIdOrderByActionDateDesc(Integer adminId);
    
    // Find actions by action type
    List<AdminAction> findByActionTypeOrderByActionDateDesc(String actionType);
    
    // Search by description or target name
    List<AdminAction> findByDescriptionContainingOrTargetNameContainingOrderByActionDateDesc(String description, String targetName);
}
