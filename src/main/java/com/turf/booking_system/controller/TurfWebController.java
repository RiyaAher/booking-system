package com.turf.booking_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.turf.booking_system.model.TurfBooking;
import com.turf.booking_system.repository.TurfBookingRepository;
import com.turf.booking_system.service.PricingService; // 1. Added Import

//We use @Controller (not @RestController) because we want to serve a web page
@Controller
@RequestMapping("/bookings")
public class TurfWebController {

    @Autowired
    private TurfBookingRepository bookingRepository;

    @Autowired
    private PricingService pricingService; // 2. Injected PricingService

    //Load the page and inject data variables into our HTML file
    @GetMapping
    public String showBookingPage(Model model) {
        model.addAttribute("newBooking", new TurfBooking());
        model.addAttribute("allBookings", bookingRepository.findAll());
        return "index"; // Looks directly inside templates/index.html
    }

    //Process the submitted form from the web page
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

        // C. Calculate dynamic price and assign it to the booking
        double calculatedPrice = pricingService.calculateBookingPrice(
                booking.getStartTime(), 
                booking.getEndTime()
        );
        booking.setTotalPrice(calculatedPrice);

        // D. Save it and redirect with a success banner showing the calculated total!
        bookingRepository.save(booking);
        redirectAttrs.addFlashAttribute("success", "✅ SUCCESS: Booking secured! Total Price: $" + calculatedPrice);
        return "redirect:/bookings";
    }

    //Delete a booking
    @PostMapping("/delete/{id}")
    public String deleteBooking(@PathVariable("id") long id, RedirectAttributes redirectAttrs){
        // Check if the booking actually exists before trying to delete it
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            redirectAttrs.addFlashAttribute("success", "🗑️ Booking successfully canceled!");
        } else {
            redirectAttrs.addFlashAttribute("error", "❌ ERROR: Booking not found.");
        }
        // Refresh the page to show the updated list
        return "redirect:/bookings";
    }
}