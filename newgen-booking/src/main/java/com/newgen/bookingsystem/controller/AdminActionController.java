package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.AdminAction;
import com.newgen.bookingsystem.service.AdminActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin-actions")
@CrossOrigin(origins = "*")
public class AdminActionController {
    
    @Autowired
    private AdminActionService adminActionService;
    
    @GetMapping
    public ResponseEntity<List<AdminAction>> getAllActions() {
        return ResponseEntity.ok(adminActionService.getAllActions());
    }
    
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<AdminAction>> getActionsByAdmin(@PathVariable Integer adminId) {
        return ResponseEntity.ok(adminActionService.getActionsByAdmin(adminId));
    }
    
    @GetMapping("/type/{actionType}")
    public ResponseEntity<List<AdminAction>> getActionsByType(@PathVariable String actionType) {
        return ResponseEntity.ok(adminActionService.getActionsByType(actionType));
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getActionCount() {
        return ResponseEntity.ok(adminActionService.getActionCount());
    }
}
