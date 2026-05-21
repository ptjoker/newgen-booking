package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Event;
import com.newgen.bookingsystem.entity.EventStatus;
import com.newgen.bookingsystem.entity.EventType;
import com.newgen.bookingsystem.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    
    // Find events by provider
    List<Event> findByProvider(Provider provider);
    List<Event> findByProvider_ProviderId(Integer providerId);
    
    // Find events by type
    List<Event> findByEventType(EventType eventType);
    
    // Find events by status
    List<Event> findByStatus(EventStatus status);
    
    // Find events by location
    List<Event> findByLocationContainingIgnoreCase(String location);
    
    // Find upcoming events (after current date)
    List<Event> findByEventDateAfter(LocalDateTime date);
    
    // Find events between dates
    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Find events with available tickets
    List<Event> findByAvailableTicketsGreaterThan(Integer tickets);
    
    // Find published events with available tickets
    List<Event> findByStatusAndAvailableTicketsGreaterThan(EventStatus status, Integer tickets);
    
    // Find events by provider and status
    List<Event> findByProvider_ProviderIdAndStatus(Integer providerId, EventStatus status);
}
