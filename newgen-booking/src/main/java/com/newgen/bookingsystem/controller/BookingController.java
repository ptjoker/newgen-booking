package com.newgen.bookingsystem.controller;

import com.newgen.bookingsystem.entity.*;
import com.newgen.bookingsystem.repository.*;
import com.newgen.bookingsystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProviderRepository providerRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // GET all bookings
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    // GET booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Integer id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // GET bookings by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found with ID: " + userId);
        }
        List<Booking> bookings = bookingRepository.findByUser(user.get());
        return ResponseEntity.ok(bookings);
    }
    
    // GET bookings by provider
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getBookingsByProvider(@PathVariable Integer providerId) {
        Optional<Provider> provider = providerRepository.findById(providerId);
        if (!provider.isPresent()) {
            return ResponseEntity.badRequest().body("Provider not found with ID: " + providerId);
        }
        List<Booking> bookings = bookingRepository.findByProvider(provider.get());
        return ResponseEntity.ok(bookings);
    }
    
    // GET bookings by event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getBookingsByEvent(@PathVariable Integer eventId) {
        List<Booking> bookings = bookingRepository.findByEvent_EventId(eventId);
        return ResponseEntity.ok(bookings);
    }
    
    // GET bookings by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getBookingsByStatus(@PathVariable String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<Booking> bookings = bookingRepository.findByStatus(bookingStatus);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }
    
    // GET bookings by type
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getBookingsByType(@PathVariable String type) {
        try {
            BookingType bookingType = BookingType.valueOf(type.toUpperCase());
            List<Booking> bookings = bookingRepository.findByBookingType(bookingType);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid booking type: " + type);
        }
    }
    
    // GET upcoming bookings for user
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<?> getUpcomingUserBookings(@PathVariable Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found with ID: " + userId);
        }
        List<Booking> bookings = bookingRepository.findByUser_UserIdAndBookingDateAfter(
            userId, LocalDate.now());
        return ResponseEntity.ok(bookings);
    }
    
    // CREATE new booking (with notification)
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            // Validate user
            if (booking.getUser() == null || booking.getUser().getUserId() == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            Optional<User> user = userRepository.findById(booking.getUser().getUserId());
            if (!user.isPresent()) {
                return ResponseEntity.badRequest().body("User not found with ID: " + booking.getUser().getUserId());
            }
            booking.setUser(user.get());
            
            // Validate provider
            if (booking.getProvider() == null || booking.getProvider().getProviderId() == null) {
                return ResponseEntity.badRequest().body("Provider ID is required");
            }
            Optional<Provider> provider = providerRepository.findById(booking.getProvider().getProviderId());
            if (!provider.isPresent()) {
                return ResponseEntity.badRequest().body("Provider not found with ID: " + booking.getProvider().getProviderId());
            }
            booking.setProvider(provider.get());
            
            // Validate event if provided
            if (booking.getEvent() != null && booking.getEvent().getEventId() != null) {
                Optional<Event> event = eventRepository.findById(booking.getEvent().getEventId());
                if (!event.isPresent()) {
                    return ResponseEntity.badRequest().body("Event not found with ID: " + booking.getEvent().getEventId());
                }
                booking.setEvent(event.get());
                
                // Check ticket availability for event bookings
                if (booking.getBookingType() == BookingType.EVENT_TICKET) {
                    Event e = event.get();
                    if (e.getAvailableTickets() < booking.getNumberOfGuests()) {
                        return ResponseEntity.badRequest().body("Not enough tickets available. Available: " + 
                            e.getAvailableTickets() + ", Requested: " + booking.getNumberOfGuests());
                    }
                    e.setAvailableTickets(e.getAvailableTickets() - booking.getNumberOfGuests());
                    eventRepository.save(e);
                }
            }
            
            // Generate booking reference if not provided
            if (booking.getBookingReference() == null || booking.getBookingReference().isEmpty()) {
                booking.setBookingReference("BK" + System.currentTimeMillis());
            }
            
            // Set default values
            booking.setStatus(BookingStatus.PENDING);
            booking.setCheckedIn(false);
            
            Booking savedBooking = bookingRepository.save(booking);
            
            // SEND NOTIFICATION TO USER - Booking Created
            String notificationMessage = "Your booking (Reference: " + savedBooking.getBookingReference() + 
                                         ") for " + savedBooking.getBookingType() + 
                                         " on " + savedBooking.getBookingDate() + 
                                         " has been created successfully. Please wait for confirmation.";
            
            notificationService.sendBookingCreatedNotification(
                savedBooking.getUser(),
                savedBooking.getBookingId(),
                savedBooking.getBookingReference(),
                savedBooking.getBookingType().toString(),
                savedBooking.getBookingDate().toString()
            );
            
            return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating booking: " + e.getMessage());
        }
    }
    
    // UPDATE booking
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Integer id, @RequestBody Booking bookingDetails) {
        Optional<Booking> existingBooking = bookingRepository.findById(id);
        
        if (!existingBooking.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Booking booking = existingBooking.get();
        
        // Update fields
        booking.setBookingDate(bookingDetails.getBookingDate());
        booking.setTimeSlot(bookingDetails.getTimeSlot());
        booking.setNumberOfGuests(bookingDetails.getNumberOfGuests());
        booking.setSpecialRequests(bookingDetails.getSpecialRequests());
        booking.setTotalAmount(bookingDetails.getTotalAmount());
        booking.setBookingReference(bookingDetails.getBookingReference());
        
        Booking updatedBooking = bookingRepository.save(booking);
        return ResponseEntity.ok(updatedBooking);
    }
    
    // CONFIRM booking (with notification)
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Integer id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        
        if (!bookingOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Booking booking = bookingOpt.get();
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking updatedBooking = bookingRepository.save(booking);
        
        // SEND NOTIFICATION TO USER - Booking Confirmed
        notificationService.sendBookingConfirmedNotification(
            booking.getUser(),
            booking.getBookingId(),
            booking.getBookingReference(),
            booking.getBookingType().toString(),
            booking.getBookingDate().toString()
        );
        
        return ResponseEntity.ok(updatedBooking);
    }
    
    // CANCEL booking (with notification)
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        
        if (!bookingOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Booking booking = bookingOpt.get();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        Booking updatedBooking = bookingRepository.save(booking);
        
        // SEND NOTIFICATION TO USER - Booking Cancelled
        notificationService.sendBookingCancelledNotification(
            booking.getUser(),
            booking.getBookingId(),
            booking.getBookingReference(),
            booking.getBookingType().toString(),
            reason != null ? reason : "No reason provided"
        );
        
        return ResponseEntity.ok(updatedBooking);
    }
    
    // CHECK-IN booking
    @PatchMapping("/{id}/checkin")
    public ResponseEntity<?> checkInBooking(@PathVariable Integer id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        
        if (!bookingOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Booking booking = bookingOpt.get();
        booking.setCheckedIn(true);
        booking.setCheckedInTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        
        return ResponseEntity.ok(updatedBooking);
    }
    
    // COMPLETE booking (without check-in)
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Integer id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        
        if (!bookingOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Booking booking = bookingOpt.get();
        booking.setStatus(BookingStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        return ResponseEntity.ok(updatedBooking);
    }
    
    // DELETE booking
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBooking(@PathVariable Integer id) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(id);
            
            if (!bookingOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            bookingRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
