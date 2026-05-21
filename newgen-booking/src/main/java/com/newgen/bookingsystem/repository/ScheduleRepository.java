package com.newgen.bookingsystem.repository;

import com.newgen.bookingsystem.entity.Schedule;
import com.newgen.bookingsystem.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    // Find schedules only for verified providers
    @Query("SELECT s FROM Schedule s WHERE s.provider.verified = true AND s.provider.providerId = :providerId")
    List<Schedule> findSchedulesForVerifiedProvider(@Param("providerId") Integer providerId);

    // Find schedules by provider
    List<Schedule> findByProvider(Provider provider);
    List<Schedule> findByProvider_ProviderId(Integer providerId);
    
    // Find schedules by provider and day of week (now using Byte)
    List<Schedule> findByProvider_ProviderIdAndDayOfWeek(Integer providerId, Byte dayOfWeek);
    
    // Find schedules by availability
    List<Schedule> findByIsAvailableTrue();
    List<Schedule> findByProvider_ProviderIdAndIsAvailableTrue(Integer providerId);
    
    // Find schedules effective on a specific date
    @Query("SELECT s FROM Schedule s WHERE s.provider.providerId = :providerId " +
           "AND (s.effectiveFrom IS NULL OR s.effectiveFrom <= :date) " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)")
    List<Schedule> findEffectiveSchedulesByProviderAndDate(
        @Param("providerId") Integer providerId, 
        @Param("date") LocalDate date);
    
    // Find schedules by day of week and time range
    List<Schedule> findByDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        Byte dayOfWeek, LocalTime startTime, LocalTime endTime);
    
    // Check if schedule conflicts with existing ones (dayOfWeek as Integer for query)
    @Query("SELECT s FROM Schedule s WHERE s.provider.providerId = :providerId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND ((s.startTime <= :endTime AND s.endTime >= :startTime)) " +
           "AND (s.effectiveFrom IS NULL OR s.effectiveFrom <= :effectiveTo) " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :effectiveFrom)")
    List<Schedule> findConflictingSchedules(
        @Param("providerId") Integer providerId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("effectiveFrom") LocalDate effectiveFrom,
        @Param("effectiveTo") LocalDate effectiveTo);
    
    // Find schedules by provider and date range
    List<Schedule> findByProvider_ProviderIdAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
        Integer providerId, LocalDate date1, LocalDate date2);
    
    // Find all schedules for a specific day regardless of provider
    List<Schedule> findByDayOfWeek(Byte dayOfWeek);
    
    // Find schedules that are currently active (effective from <= today and effective to >= today)
    @Query("SELECT s FROM Schedule s WHERE s.provider.providerId = :providerId " +
           "AND (s.effectiveFrom IS NULL OR s.effectiveFrom <= :today) " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :today)")
    List<Schedule> findCurrentSchedulesByProvider(
        @Param("providerId") Integer providerId, 
        @Param("today") LocalDate today);
    
    // Count schedules by provider
    Long countByProvider_ProviderId(Integer providerId);
    
    // Delete all schedules for a provider
    void deleteByProvider_ProviderId(Integer providerId);
}
