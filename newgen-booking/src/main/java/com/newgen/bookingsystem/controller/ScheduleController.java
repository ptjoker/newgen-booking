package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.Schedule;
import com.newgen.bookingsystem.entity.Provider;
import com.newgen.bookingsystem.repository.ScheduleRepository;
import com.newgen.bookingsystem.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private ProviderRepository providerRepository;
    
    @GetMapping
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Integer id) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        return schedule.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getSchedulesByProvider(@PathVariable Integer providerId) {
        Optional<Provider> provider = providerRepository.findById(providerId);
        if (!provider.isPresent()) {
            return ResponseEntity.badRequest().body("Provider not found with ID: " + providerId);
        }
        List<Schedule> schedules = scheduleRepository.findByProvider_ProviderId(providerId);
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/provider/{providerId}/available")
    public ResponseEntity<?> getAvailableSchedulesByProvider(@PathVariable Integer providerId) {
        Optional<Provider> provider = providerRepository.findById(providerId);
        if (!provider.isPresent()) {
            return ResponseEntity.badRequest().body("Provider not found with ID: " + providerId);
        }
        
        // Only show schedules for verified providers
        if (!provider.get().getVerified()) {
            return ResponseEntity.ok(List.of());
        }
        
        List<Schedule> schedules = scheduleRepository.findByProvider_ProviderIdAndIsAvailableTrue(providerId);
        return ResponseEntity.ok(schedules);
    }
    
    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody Schedule schedule) {
        try {
            // Validate provider
            if (schedule.getProvider() == null || schedule.getProvider().getProviderId() == null) {
                return ResponseEntity.badRequest().body("Provider ID is required");
            }
            
            Optional<Provider> provider = providerRepository.findById(schedule.getProvider().getProviderId());
            if (!provider.isPresent()) {
                return ResponseEntity.badRequest().body("Provider not found with ID: " + 
                    schedule.getProvider().getProviderId());
            }
            
            // CHECK IF PROVIDER IS VERIFIED (business verified by admin)
            // This is the only approval check - providers must be verified to add schedules
            if (!provider.get().getVerified()) {
                return ResponseEntity.badRequest().body("Provider must be verified by admin before adding schedules.");
            }
            
            schedule.setProvider(provider.get());
            
            // Validate times
            if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
                return ResponseEntity.badRequest().body("Start time and end time are required");
            }
            
            if (schedule.getStartTime().isAfter(schedule.getEndTime())) {
                return ResponseEntity.badRequest().body("Start time must be before end time");
            }
            
            // Validate break times if provided
            if (schedule.getBreakStart() != null && schedule.getBreakEnd() != null) {
                if (schedule.getBreakStart().isAfter(schedule.getBreakEnd())) {
                    return ResponseEntity.badRequest().body("Break start must be before break end");
                }
                if (schedule.getBreakStart().isBefore(schedule.getStartTime()) ||
                    schedule.getBreakEnd().isAfter(schedule.getEndTime())) {
                    return ResponseEntity.badRequest().body("Break times must be within working hours");
                }
            }
            
            Schedule savedSchedule = scheduleRepository.save(schedule);
            return new ResponseEntity<>(savedSchedule, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating schedule: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Integer id, @RequestBody Schedule scheduleDetails) {
        Optional<Schedule> existingSchedule = scheduleRepository.findById(id);
        
        if (!existingSchedule.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Schedule schedule = existingSchedule.get();
        
        schedule.setDayOfWeek(scheduleDetails.getDayOfWeek());
        schedule.setStartTime(scheduleDetails.getStartTime());
        schedule.setEndTime(scheduleDetails.getEndTime());
        schedule.setBreakStart(scheduleDetails.getBreakStart());
        schedule.setBreakEnd(scheduleDetails.getBreakEnd());
        schedule.setMaxBookingsPerSlot(scheduleDetails.getMaxBookingsPerSlot());
        schedule.setIsAvailable(scheduleDetails.getIsAvailable());
        schedule.setEffectiveFrom(scheduleDetails.getEffectiveFrom());
        schedule.setEffectiveTo(scheduleDetails.getEffectiveTo());
        
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return ResponseEntity.ok(updatedSchedule);
    }
    
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Integer id) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(id);
        
        if (!scheduleOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Schedule schedule = scheduleOpt.get();
        schedule.setIsAvailable(!schedule.getIsAvailable());
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return ResponseEntity.ok(updatedSchedule);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteSchedule(@PathVariable Integer id) {
        try {
            if (scheduleRepository.existsById(id)) {
                scheduleRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
