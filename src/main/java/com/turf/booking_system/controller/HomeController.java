package com.turf.booking_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToBookings() {
        // Redirects root URL (http://localhost:8080/) directly to your booking dashboard
        return "redirect:/bookings";
    }
}