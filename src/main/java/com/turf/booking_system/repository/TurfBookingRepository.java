package com.turf.booking_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.turf.booking_system.model.TurfBooking;


@Repository
public interface TurfBookingRepository extends JpaRepository<TurfBooking, Long> {
    
    @Query("SELECT b FROM TurfBooking b WHERE b.turfName = :turfName AND " +
           "(:start < b.endTime AND :end > b.startTime)")
    List<TurfBooking> findOverlappingBookings(
        @Param("turfName") String turfName, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
}
