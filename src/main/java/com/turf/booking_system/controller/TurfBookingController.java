package com.turf.booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turf.booking_system.model.TurfBooking;
import com.turf.booking_system.repository.TurfBookingRepository;


@RestController
@RequestMapping("/api/v1/bookings")
public class TurfBookingController {

    // 1. Declare the dependency as final for immutability
    private final TurfBookingRepository bookingRepository;

    // 2. Inject via constructor
    public TurfBookingController(TurfBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public List<TurfBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody TurfBooking newBooking) {
        
        List<TurfBooking> conflicts = bookingRepository.findOverlappingBookings(
            newBooking.getTurfName(),
            newBooking.getStartTime(),
            newBooking.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("❌ ERROR: Time slot collision! This slot is already booked.");
        }

        TurfBooking savedBooking = bookingRepository.save(newBooking);
        return ResponseEntity.ok(savedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable long id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.ok("Booking " + id + " has been successfully cancelled by the Admin.");
    }
}