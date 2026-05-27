package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.dao.UserDataAccess;
import com.newgen.bookingsystem.entity.User;
import com.newgen.bookingsystem.repository.UserRepository;
import com.newgen.bookingsystem.service.AdminActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataAccess userDataAccess;

    @Autowired
    private AdminActionService adminActionService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping
    public List<User> getAllUsers() {
        return userDataAccess.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        Optional<User> user = userDataAccess.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userDataAccess.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userDataAccess.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        Optional<User> userOpt = userDataAccess.getUserByEmail(email);

        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        
        User user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("First name is required");
            }
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Last name is required");
            }
            if (user.getRole() == null) {
                return ResponseEntity.badRequest().body("Role is required");
            }

            if (userDataAccess.emailExists(user.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists: " + user.getEmail());
            }

            if (userDataAccess.usernameExists(user.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists: " + user.getUsername());
            }

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("admin123");
            }

            if (user.getProviderStatus() == null && "provider".equalsIgnoreCase(user.getRole())) {
                user.setProviderStatus("pending");
            }

            User savedUser = userDataAccess.saveUser(user);

            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        try {
            Optional<User> existingUser = userDataAccess.getUserById(id);

            if (!existingUser.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = existingUser.get();
            String oldRole = user.getRole();
            String oldUsername = user.getUsername();
            String oldEmail = user.getEmail();
            StringBuilder changes = new StringBuilder();

            if (userDetails.getUsername() != null && !userDetails.getUsername().isEmpty()) {
                if (userDataAccess.usernameExists(userDetails.getUsername()) &&
                        !user.getUsername().equals(userDetails.getUsername())) {
                    return ResponseEntity.badRequest().body("Username already exists: " + userDetails.getUsername());
                }
                changes.append("Username from ").append(user.getUsername()).append(" to ").append(userDetails.getUsername()).append("; ");
                user.setUsername(userDetails.getUsername());
            }

            if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
                if (userDataAccess.emailExists(userDetails.getEmail()) &&
                        !user.getEmail().equals(userDetails.getEmail())) {
                    return ResponseEntity.badRequest().body("Email already exists: " + userDetails.getEmail());
                }
                changes.append("Email from ").append(user.getEmail()).append(" to ").append(userDetails.getEmail()).append("; ");
                user.setEmail(userDetails.getEmail());
            }

            if (userDetails.getFirstName() != null) {
                user.setFirstName(userDetails.getFirstName());
            }

            if (userDetails.getLastName() != null) {
                user.setLastName(userDetails.getLastName());
            }

            if (userDetails.getPhone() != null) {
                user.setPhone(userDetails.getPhone());
            }

            if (userDetails.getRole() != null && !userDetails.getRole().equals(oldRole)) {
                changes.append("Role from ").append(oldRole).append(" to ").append(userDetails.getRole()).append("; ");
                user.setRole(userDetails.getRole());
            }

            if (userDetails.getProfilePicture() != null) {
                user.setProfilePicture(userDetails.getProfilePicture());
            }

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(userDetails.getPassword());
            }

            if (userDetails.getProviderStatus() != null) {
                user.setProviderStatus(userDetails.getProviderStatus());
            }

            if (userDetails.getBusinessName() != null) {
                user.setBusinessName(userDetails.getBusinessName());
            }

            User updatedUser = userDataAccess.saveUser(user);

            // Log to audit log if changes were made
            if (changes.length() > 0) {
                // Get admin user ID from request header or parameter
                // For now, we'll use a default admin ID - you can pass adminId as a parameter
                Integer adminId = getAdminIdFromRequest();
                if (adminId != null) {
                    Optional<User> admin = userRepository.findById(adminId);
                    if (admin.isPresent() && "ADMIN".equalsIgnoreCase(admin.get().getRole())) {
                        adminActionService.logAction(
                                admin.get(),
                                "UPDATE",
                                "USER",
                                user.getUserId(),
                                user.getUsername(),
                                "Updated user: " + changes.toString()
                        );
                    }
                }
            }

            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            if (!userDataAccess.userExists(id)) {
                return ResponseEntity.notFound().build();
            }

            // Get user details before deletion for audit log
            Optional<User> userToDelete = userDataAccess.getUserById(id);

            userDataAccess.deleteUser(id);

            // Log to audit log
            if (userToDelete.isPresent()) {
                User deletedUser = userToDelete.get();
                Integer adminId = getAdminIdFromRequest();
                if (adminId != null) {
                    Optional<User> admin = userRepository.findById(adminId);
                    if (admin.isPresent() && "ADMIN".equalsIgnoreCase(admin.get().getRole())) {
                        adminActionService.logAction(
                                admin.get(),
                                "DELETE",
                                "USER",
                                deletedUser.getUserId(),
                                deletedUser.getUsername(),
                                "Deleted user: " + deletedUser.getUsername() + " (Role: " + deletedUser.getRole() + ")"
                        );
                    }
                }
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        long count = userDataAccess.getUserCount();
        return ResponseEntity.ok(count);
    }

    // Helper method to get admin ID from request header
    private Integer getAdminIdFromRequest() {
        String adminIdHeader = request.getHeader("X-Admin-Id");
        if (adminIdHeader != null && !adminIdHeader.isEmpty()) {
            try {
                return Integer.parseInt(adminIdHeader);
            } catch (NumberFormatException e) {
                // Fall through
            }
        }
        // Default admin ID - you can change this
        return 6; // admin_jane ID
    }
}