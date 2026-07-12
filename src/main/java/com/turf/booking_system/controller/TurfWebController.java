package com.turf.booking_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.turf.booking_system.model.TurfBooking;
import com.turf.booking_system.repository.TurfBookingRepository;

// 1. We use @Controller (not @RestController) because we want to serve a web page
@Controller
@RequestMapping("/bookings")
public class TurfWebController {

    @Autowired
    private TurfBookingRepository bookingRepository;

    // 2. Load the page and inject data variables into our HTML file
    @GetMapping
    public String showBookingPage(Model model) {
        model.addAttribute("newBooking", new TurfBooking());
        model.addAttribute("allBookings", bookingRepository.findAll());
        return "index"; // Looks directly inside templates/index.html
    }

    // 3. Process the submitted form from the web page
    @PostMapping
    public String processBooking(@ModelAttribute("newBooking") TurfBooking booking, RedirectAttributes redirectAttrs) {
        
        // A. Run our overlap query rule engine
        List<TurfBooking> conflicts = bookingRepository.findOverlappingBookings(
                booking.getTurfName(),
                booking.getStartTime(),
                booking.getEndTime()
        );

        // B. If it collides, reject and redirect with a warning message
        if (!conflicts.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "❌ ERROR: Time slot collision! This slot is already booked.");
            return "redirect:/bookings";
        }

        // C. Save it and redirect with a success banner!
        bookingRepository.save(booking);
        redirectAttrs.addFlashAttribute("success", "✅ SUCCESS: Booking secured!");
        return "redirect:/bookings";
    }
}
