package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.Event;
import com.newgen.bookingsystem.entity.EventStatus;
import com.newgen.bookingsystem.entity.EventType;
import com.newgen.bookingsystem.entity.Provider;
import com.newgen.bookingsystem.repository.EventRepository;
import com.newgen.bookingsystem.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ProviderRepository providerRepository;
    
    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Integer id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getEventsByProvider(@PathVariable Integer providerId) {
        Optional<Provider> provider = providerRepository.findById(providerId);
        if (!provider.isPresent()) {
            return ResponseEntity.badRequest().body("Provider not found with ID: " + providerId);
        }
        List<Event> events = eventRepository.findByProvider(provider.get());
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/type/{eventType}")
    public ResponseEntity<?> getEventsByType(@PathVariable String eventType) {
        try {
            EventType type = EventType.valueOf(eventType);
            List<Event> events = eventRepository.findByEventType(type);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid event type: " + eventType);
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getEventsByStatus(@PathVariable String status) {
        try {
            EventStatus eventStatus = EventStatus.valueOf(status);
            List<Event> events = eventRepository.findByStatus(eventStatus);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }
    
    @GetMapping("/upcoming")
    public List<Event> getUpcomingEvents() {
        return eventRepository.findByEventDateAfter(LocalDateTime.now());
    }
    
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            if (event.getProvider() == null || event.getProvider().getProviderId() == null) {
                return ResponseEntity.badRequest().body("Provider ID is required");
            }
            
            Optional<Provider> provider = providerRepository.findById(event.getProvider().getProviderId());
            if (!provider.isPresent()) {
                return ResponseEntity.badRequest().body("Provider not found with ID: " + event.getProvider().getProviderId());
            }
            
            event.setProvider(provider.get());
            
            if (event.getAvailableTickets() == null) {
                event.setAvailableTickets(event.getTotalTickets());
            }
            
            Event savedEvent = eventRepository.save(event);
            return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating event: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id, @RequestBody Event eventDetails) {
        Optional<Event> existingEvent = eventRepository.findById(id);
        
        if (!existingEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Event event = existingEvent.get();
        
        event.setEventName(eventDetails.getEventName());
        event.setDescription(eventDetails.getDescription());
        event.setEventType(eventDetails.getEventType());
        event.setEventDate(eventDetails.getEventDate());
        event.setEndDate(eventDetails.getEndDate());
        event.setLocation(eventDetails.getLocation());
        event.setVenueDetails(eventDetails.getVenueDetails());
        event.setTotalTickets(eventDetails.getTotalTickets());
        event.setAvailableTickets(eventDetails.getAvailableTickets());
        event.setPrice(eventDetails.getPrice());
        event.setEarlyBirdPrice(eventDetails.getEarlyBirdPrice());
        event.setGroupDiscount(eventDetails.getGroupDiscount());
        event.setImageUrl(eventDetails.getImageUrl());
        event.setStatus(eventDetails.getStatus());
        
        Event updatedEvent = eventRepository.save(event);
        return ResponseEntity.ok(updatedEvent);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            EventStatus newStatus = EventStatus.valueOf(status);
            Optional<Event> eventOpt = eventRepository.findById(id);
            
            if (!eventOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Event event = eventOpt.get();
            event.setStatus(newStatus);
            Event updatedEvent = eventRepository.save(event);
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteEvent(@PathVariable Integer id) {
        try {
            if (eventRepository.existsById(id)) {
                eventRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
