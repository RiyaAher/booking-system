package com.turf.booking_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turf.booking_system.model.TurfBooking;
import com.turf.booking_system.repository.TurfBookingRepository;

@RestController
//Base URL path for all endpoints in this file
@RequestMapping("/api/v1/bookings")
public class TurfBookingController {

    //Automatically injects our database repository engine into this controller, @autowired helps us with that.
    @Autowired
    private TurfBookingRepository bookingRepository;

    //GET Endpoint: Because we wanna return all active bookings currently saved in MySQL
    @GetMapping
    public List<TurfBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    //POST Endpoint: Submits a brand-new booking request from the user
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody TurfBooking newBooking) {
        
        // A. Ask the repository to check if this slot collides with existing bookings
        List<TurfBooking> conflicts = bookingRepository.findOverlappingBookings(
            newBooking.getTurfName(),
            newBooking.getStartTime(),
            newBooking.getEndTime()
        );

        // B. If the conflict list is NOT empty, reject the booking immediately! 
        if (!conflicts.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("❌ ERROR: Time slot collision! This slot is already booked.");
        }

        // C. Clean connection! Save the booking into MySQL
        TurfBooking savedBooking = bookingRepository.save(newBooking);
        return ResponseEntity.ok(savedBooking);
    }
}
