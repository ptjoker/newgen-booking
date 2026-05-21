package com.newgen.bookingsystem.service;

import com.newgen.bookingsystem.entity.AdminAction;
import com.newgen.bookingsystem.entity.User;
import com.newgen.bookingsystem.repository.AdminActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminActionService {
    
    @Autowired
    private AdminActionRepository adminActionRepository;
    
    @Autowired
    private HttpServletRequest request;
    
    public void logAction(User admin, String actionType, String targetType, 
                         Integer targetId, String targetName, String description) {
        String ipAddress = getClientIp();
        String userAgent = request.getHeader("User-Agent");
        
        AdminAction action = new AdminAction();
        action.setAdmin(admin);
        action.setActionType(actionType);
        action.setTargetType(targetType);
        action.setTargetId(targetId);
        action.setTargetName(targetName);
        action.setDescription(description);
        action.setIpAddress(ipAddress);
        action.setUserAgent(userAgent);
        action.setActionDate(LocalDateTime.now());
        
        adminActionRepository.save(action);
        System.out.println("✅ Admin action logged: " + actionType + " - " + description);
    }
    
    public List<AdminAction> getAllActions() {
        return adminActionRepository.findAllByOrderByActionDateDesc();
    }
    
    public List<AdminAction> getActionsByAdmin(Integer adminId) {
        return adminActionRepository.findByAdmin_UserIdOrderByActionDateDesc(adminId);
    }
    
    public List<AdminAction> getActionsByType(String actionType) {
        return adminActionRepository.findByActionTypeOrderByActionDateDesc(actionType);
    }
    
    public long getActionCount() {
        return adminActionRepository.count();
    }
    
    private String getClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && remoteAddr.equals("0:0:0:0:0:0:0:1")) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }
}
