package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.Provider;
import com.newgen.bookingsystem.entity.ServiceType;
import com.newgen.bookingsystem.entity.User;
import com.newgen.bookingsystem.repository.ProviderRepository;
import com.newgen.bookingsystem.repository.UserRepository;
import com.newgen.bookingsystem.service.AdminActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*")
public class ProviderController {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminActionService adminActionService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping
    public List<Provider> getAllProviders() {
        System.out.println("GET /api/providers - Fetching all providers");
        return providerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable Integer id) {
        System.out.println("GET /api/providers/" + id);
        Optional<Provider> provider = providerRepository.findById(id);
        return provider.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/service/{serviceType}")
    public ResponseEntity<?> getProvidersByServiceType(@PathVariable String serviceType) {
        System.out.println("GET /api/providers/service/" + serviceType);
        try {
            ServiceType type = ServiceType.valueOf(serviceType.toUpperCase());
            List<Provider> providers = providerRepository.findByServiceType(type);
            return ResponseEntity.ok(providers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid service type: " + serviceType);
        }
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Provider>> getProvidersByCity(@PathVariable String city) {
        System.out.println("GET /api/providers/city/" + city);
        List<Provider> providers = providerRepository.findByCity(city);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/verified")
    public ResponseEntity<List<Provider>> getVerifiedProviders() {
        System.out.println("GET /api/providers/verified");
        List<Provider> providers = providerRepository.findByVerifiedTrue();
        System.out.println("Found " + providers.size() + " verified providers");
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProviderByUserId(@PathVariable Integer userId) {
        System.out.println("GET /api/providers/user/" + userId);
        Optional<Provider> provider = providerRepository.findByUser_UserId(userId);
        if (provider.isPresent()) {
            return ResponseEntity.ok(provider.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createProvider(@RequestBody Provider provider) {
        System.out.println("=== CREATE PROVIDER REQUEST ===");
        System.out.println("Provider data received: " + provider);

        try {
            if (provider.getUser() == null || provider.getUser().getUserId() == null) {
                System.out.println("ERROR: User ID is missing");
                return ResponseEntity.badRequest().body("User ID is required");
            }

            Integer userId = provider.getUser().getUserId();
            System.out.println("Looking for user with ID: " + userId);

            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                System.out.println("ERROR: User not found with ID: " + userId);
                return ResponseEntity.badRequest().body("User not found with ID: " + userId);
            }

            System.out.println("User found: " + user.get().getEmail());
            System.out.println("User role: " + user.get().getRole());

            Optional<Provider> existingProvider = providerRepository.findByUser_UserId(userId);
            if (existingProvider.isPresent()) {
                System.out.println("ERROR: Provider profile already exists for this user");
                return ResponseEntity.badRequest().body("User already has a provider profile");
            }

            provider.setVerified(false);
            provider.setUser(user.get());

            if (provider.getServiceType() == null) {
                System.out.println("ERROR: Service type is missing");
                return ResponseEntity.badRequest().body("Service type is required");
            }

            Provider savedProvider = providerRepository.save(provider);
            System.out.println("SUCCESS: Provider created with ID: " + savedProvider.getProviderId());
            System.out.println("Business name: " + savedProvider.getBusinessName());
            System.out.println("Verified status: " + savedProvider.getVerified());

            return new ResponseEntity<>(savedProvider, HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("EXCEPTION in createProvider: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating provider: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProvider(@PathVariable Integer id, @RequestBody Provider providerDetails) {
        System.out.println("PUT /api/providers/" + id);

        Optional<Provider> existingProvider = providerRepository.findById(id);

        if (!existingProvider.isPresent()) {
            System.out.println("ERROR: Provider not found with ID: " + id);
            return ResponseEntity.notFound().build();
        }

        Provider provider = existingProvider.get();

        if (providerDetails.getBusinessName() != null) provider.setBusinessName(providerDetails.getBusinessName());
        if (providerDetails.getServiceType() != null) provider.setServiceType(providerDetails.getServiceType());
        if (providerDetails.getRegistrationNumber() != null) provider.setRegistrationNumber(providerDetails.getRegistrationNumber());
        if (providerDetails.getAddress() != null) provider.setAddress(providerDetails.getAddress());
        if (providerDetails.getCity() != null) provider.setCity(providerDetails.getCity());
        if (providerDetails.getProvince() != null) provider.setProvince(providerDetails.getProvince());
        if (providerDetails.getPostalCode() != null) provider.setPostalCode(providerDetails.getPostalCode());
        if (providerDetails.getPhone() != null) provider.setPhone(providerDetails.getPhone());
        if (providerDetails.getWebsite() != null) provider.setWebsite(providerDetails.getWebsite());
        if (providerDetails.getDescription() != null) provider.setDescription(providerDetails.getDescription());
        if (providerDetails.getBusinessHours() != null) provider.setBusinessHours(providerDetails.getBusinessHours());

        Provider updatedProvider = providerRepository.save(provider);
        System.out.println("SUCCESS: Provider updated: " + updatedProvider.getProviderId());
        return ResponseEntity.ok(updatedProvider);
    }

    // VERIFY provider - gets admin ID from header automatically
    @PatchMapping("/{id}/verify")
    public ResponseEntity<?> verifyProvider(@PathVariable Integer id, @RequestHeader(value = "X-Admin-Id", required = false) Integer adminUserId) {
        System.out.println("PATCH /api/providers/" + id + "/verify - Admin ID from header: " + adminUserId);

        Optional<Provider> providerOpt = providerRepository.findById(id);
        if (!providerOpt.isPresent()) {
            System.out.println("ERROR: Provider not found with ID: " + id);
            return ResponseEntity.notFound().build();
        }

        Provider provider = providerOpt.get();

        // If admin ID not in header, try to get from parameter
        if (adminUserId == null) {
            String adminIdParam = request.getParameter("adminUserId");
            if (adminIdParam != null) {
                try {
                    adminUserId = Integer.parseInt(adminIdParam);
                } catch (NumberFormatException e) {}
            }
        }

        if (adminUserId == null) {
            System.out.println("ERROR: Admin ID not provided");
            return ResponseEntity.badRequest().body("Admin ID is required");
        }

        Optional<User> adminOpt = userRepository.findById(adminUserId);
        if (!adminOpt.isPresent()) {
            System.out.println("ERROR: Admin user not found with ID: " + adminUserId);
            return ResponseEntity.badRequest().body("Admin user not found");
        }

        String adminRole = adminOpt.get().getRole();
        if (adminRole == null || (!adminRole.equalsIgnoreCase("ADMIN") && !adminRole.equalsIgnoreCase("admin"))) {
            System.out.println("ERROR: User is not admin. Role: " + adminRole);
            return ResponseEntity.badRequest().body("User is not an admin. Cannot verify provider.");
        }

        provider.setVerified(true);
        provider.setVerificationDate(LocalDateTime.now());
        provider.setVerifiedBy(adminOpt.get());

        Provider verifiedProvider = providerRepository.save(provider);
        System.out.println("SUCCESS: Provider verified: " + verifiedProvider.getBusinessName());

        // LOG TO AUDIT LOG with actual admin
        adminActionService.logAction(
                adminOpt.get(),
                "VERIFY",
                "PROVIDER",
                provider.getProviderId(),
                provider.getBusinessName(),
                "Verified provider: " + provider.getBusinessName()
        );

        return ResponseEntity.ok(verifiedProvider);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteProvider(@PathVariable Integer id) {
        System.out.println("DELETE /api/providers/" + id);

        try {
            Optional<Provider> providerOpt = providerRepository.findById(id);
            if (!providerOpt.isPresent()) {
                System.out.println("ERROR: Provider not found: " + id);
                return ResponseEntity.notFound().build();
            }

            Provider provider = providerOpt.get();
            User user = provider.getUser();
            String providerName = provider.getBusinessName();

            // Get admin ID from header
            Integer adminId = getAdminIdFromRequest();
            if (adminId != null) {
                Optional<User> admin = userRepository.findById(adminId);
                if (admin.isPresent() && "ADMIN".equalsIgnoreCase(admin.get().getRole())) {
                    adminActionService.logAction(
                            admin.get(),
                            "DELETE",
                            "PROVIDER",
                            provider.getProviderId(),
                            providerName,
                            "Deleted provider: " + providerName
                    );
                }
            }

            providerRepository.deleteById(id);
            System.out.println("SUCCESS: Provider deleted: " + id);

            if (user != null) {
                userRepository.delete(user);
                System.out.println("SUCCESS: User also deleted: " + user.getUserId());
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            System.err.println("EXCEPTION in deleteProvider: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Integer getAdminIdFromRequest() {
        String adminIdHeader = request.getHeader("X-Admin-Id");
        if (adminIdHeader != null && !adminIdHeader.isEmpty()) {
            try {
                return Integer.parseInt(adminIdHeader);
            } catch (NumberFormatException e) {}
        }

        String adminIdParam = request.getParameter("adminId");
        if (adminIdParam != null && !adminIdParam.isEmpty()) {
            try {
                return Integer.parseInt(adminIdParam);
            } catch (NumberFormatException e) {}
        }

        return null;
    }
}