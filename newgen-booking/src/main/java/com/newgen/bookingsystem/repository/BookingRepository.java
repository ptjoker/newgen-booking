package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Booking;
import com.newgen.bookingsystem.entity.BookingStatus;
import com.newgen.bookingsystem.entity.BookingType;
import com.newgen.bookingsystem.entity.Provider;
import com.newgen.bookingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    // Find bookings by user
    List<Booking> findByUser(User user);
    List<Booking> findByUser_UserId(Integer userId);
    
    // Find bookings by provider
    List<Booking> findByProvider(Provider provider);
    List<Booking> findByProvider_ProviderId(Integer providerId);
    
    // Find bookings by event
    List<Booking> findByEvent_EventId(Integer eventId);
    
    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);
    
    // Find bookings by type
    List<Booking> findByBookingType(BookingType bookingType);
    
    // Find bookings by date range
    List<Booking> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find bookings by user and status
    List<Booking> findByUser_UserIdAndStatus(Integer userId, BookingStatus status);
    
    // Find bookings by provider and status
    List<Booking> findByProvider_ProviderIdAndStatus(Integer providerId, BookingStatus status);
    
    // Find bookings by booking reference
    Optional<Booking> findByBookingReference(String bookingReference);
    
    // Find upcoming bookings for a user (after current date)
    List<Booking> findByUser_UserIdAndBookingDateAfter(Integer userId, LocalDate date);
    
    // Find checked-in bookings
    List<Booking> findByCheckedInTrue();
    
    // Count bookings by status for a provider
    Long countByProvider_ProviderIdAndStatus(Integer providerId, BookingStatus status);
}
