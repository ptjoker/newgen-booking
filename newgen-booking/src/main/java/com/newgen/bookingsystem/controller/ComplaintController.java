package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.Complaint;
import com.newgen.bookingsystem.entity.ComplaintStatus;
import com.newgen.bookingsystem.entity.ComplaintType;
import com.newgen.bookingsystem.entity.User;
import com.newgen.bookingsystem.entity.Provider;
import com.newgen.bookingsystem.entity.Booking;
import com.newgen.bookingsystem.repository.ComplaintRepository;
import com.newgen.bookingsystem.repository.UserRepository;
import com.newgen.bookingsystem.repository.ProviderRepository;
import com.newgen.bookingsystem.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Get all complaints (Admin)
    @GetMapping
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get complaints by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getComplaintsByUser(@PathVariable Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        List<Complaint> complaints = complaintRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(complaints);
    }

    // Get complaints by provider
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getComplaintsByProvider(@PathVariable Integer providerId) {
        Optional<Provider> provider = providerRepository.findById(providerId);
        if (!provider.isPresent()) {
            return ResponseEntity.badRequest().body("Provider not found");
        }
        List<Complaint> complaints = complaintRepository.findByProvider_ProviderIdOrderByCreatedAtDesc(providerId);
        return ResponseEntity.ok(complaints);
    }

    // Get complaints by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getComplaintsByStatus(@PathVariable String status) {
        try {
            ComplaintStatus complaintStatus = ComplaintStatus.valueOf(status.toUpperCase());
            List<Complaint> complaints = complaintRepository.findByStatus(complaintStatus);
            return ResponseEntity.ok(complaints);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status. Valid: PENDING, IN_REVIEW, RESOLVED, CLOSED");
        }
    }

    // Get complaints by type
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getComplaintsByType(@PathVariable String type) {
        try {
            ComplaintType complaintType = ComplaintType.valueOf(type.toUpperCase());
            List<Complaint> complaints = complaintRepository.findByType(complaintType);
            return ResponseEntity.ok(complaints);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid type");
        }
    }

    // Get single complaint
    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Integer id) {
        Optional<Complaint> complaint = complaintRepository.findById(id);
        return complaint.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Create new complaint
    @PostMapping
    public ResponseEntity<?> createComplaint(@RequestBody Complaint complaint) {
        try {
            // Validate required fields
            if (complaint.getTitle() == null || complaint.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Title is required");
            }

            if (complaint.getDescription() == null || complaint.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Description is required");
            }

            if (complaint.getType() == null) {
                return ResponseEntity.badRequest().body("Complaint type is required");
            }

            // Validate user
            if (complaint.getUser() == null || complaint.getUser().getUserId() == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            Optional<User> user = userRepository.findById(complaint.getUser().getUserId());
            if (!user.isPresent()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            complaint.setUser(user.get());

            // Validate provider if provided
            if (complaint.getProvider() != null && complaint.getProvider().getProviderId() != null) {
                Optional<Provider> provider = providerRepository.findById(complaint.getProvider().getProviderId());
                if (!provider.isPresent()) {
                    return ResponseEntity.badRequest().body("Provider not found");
                }
                complaint.setProvider(provider.get());
            }

            // Validate booking if provided
            if (complaint.getBooking() != null && complaint.getBooking().getBookingId() != null) {
                Optional<Booking> booking = bookingRepository.findById(complaint.getBooking().getBookingId());
                if (!booking.isPresent()) {
                    return ResponseEntity.badRequest().body("Booking not found");
                }
                complaint.setBooking(booking.get());
            }

            // Set default values
            complaint.setStatus(ComplaintStatus.PENDING);
            complaint.setCreatedAt(LocalDateTime.now());

            Complaint savedComplaint = complaintRepository.save(complaint);
            return new ResponseEntity<>(savedComplaint, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating complaint: " + e.getMessage());
        }
    }

    // Respond to complaint (Admin)
    @PutMapping("/{id}/respond")
    public ResponseEntity<?> respondToComplaint(
            @PathVariable Integer id,
            @RequestParam String adminResponse,
            @RequestParam Integer adminId,
            @RequestParam String status) {

        try {
            Optional<Complaint> complaintOpt = complaintRepository.findById(id);
            if (!complaintOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Optional<User> admin = userRepository.findById(adminId);
            if (!admin.isPresent() || !"ADMIN".equalsIgnoreCase(admin.get().getRole())) {
                return ResponseEntity.badRequest().body("Valid admin ID required");
            }

            Complaint complaint = complaintOpt.get();
            complaint.setAdminResponse(adminResponse);
            complaint.setRespondedBy(adminId);
            complaint.setRespondedAt(LocalDateTime.now());

            try {
                ComplaintStatus newStatus = ComplaintStatus.valueOf(status.toUpperCase());
                complaint.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status. Valid: PENDING, IN_REVIEW, RESOLVED, CLOSED");
            }

            Complaint updatedComplaint = complaintRepository.save(complaint);
            return ResponseEntity.ok(updatedComplaint);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error responding to complaint: " + e.getMessage());
        }
    }

    // Delete complaint
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteComplaint(@PathVariable Integer id) {
        try {
            if (complaintRepository.existsById(id)) {
                complaintRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getComplaintStats() {
        long total = complaintRepository.count();
        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long inReview = complaintRepository.countByStatus(ComplaintStatus.IN_REVIEW);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long closed = complaintRepository.countByStatus(ComplaintStatus.CLOSED);

        return ResponseEntity.ok(new ComplaintStats(total, pending, inReview, resolved, closed));
    }

    static class ComplaintStats {
        public long total;
        public long pending;
        public long inReview;
        public long resolved;
        public long closed;

        public ComplaintStats(long total, long pending, long inReview, long resolved, long closed) {
            this.total = total;
            this.pending = pending;
            this.inReview = inReview;
            this.resolved = resolved;
            this.closed = closed;
        }
    }
}