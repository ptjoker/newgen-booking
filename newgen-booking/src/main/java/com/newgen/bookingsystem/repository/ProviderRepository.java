package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Provider;
import com.newgen.bookingsystem.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {
    
    // Find providers by service type (hotel, doctor, trip, event_organizer)
    List<Provider> findByServiceType(ServiceType serviceType);
    
    // Find providers by city
    List<Provider> findByCity(String city);
    
    // Find verified providers only
    List<Provider> findByVerifiedTrue();
    
    // Find providers by user ID
    Optional<Provider> findByUser_UserId(Integer userId);
    
    // Search providers by business name (partial match)
    List<Provider> findByBusinessNameContainingIgnoreCase(String businessName);
    
    // Find providers by verification status and city
    List<Provider> findByVerifiedAndCity(Boolean verified, String city);
}
