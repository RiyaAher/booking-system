package com.turf.booking_system.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.turf.booking_system.model.TurfBooking;
import com.turf.booking_system.repository.TurfBookingRepository;
import com.turf.booking_system.service.PricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.function.Function;

@Configuration
public class BookingTools {

    private final PricingService pricingService;
    private final TurfBookingRepository bookingRepository;

    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd['T'][ ]HH:mm")
            .optionalStart().appendPattern(":ss").optionalEnd()
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    public BookingTools(PricingService pricingService, TurfBookingRepository bookingRepository) {
        this.pricingService = pricingService;
        this.bookingRepository = bookingRepository;
    }

    // --- 1. CALCULATE PRICE TOOL ---
    public record PriceRequest(
        @JsonPropertyDescription("Start date-time in ISO format, e.g. 2026-08-07T14:00:00")
        @JsonProperty(required = true) String startTime,

        @JsonPropertyDescription("End date-time in ISO format, e.g. 2026-08-07T16:00:00")
        @JsonProperty(required = true) String endTime
    ) {}

    public record PriceResponse(double totalPrice, double hourlyRate, String rateType) {}

    @Bean
    @Description("Calculates total booking cost for a given pitch based on weekend and weekday pricing rules.")
    public Function<PriceRequest, PriceResponse> calculatePriceTool() {
        return request -> {
            try {
                LocalDateTime start = LocalDateTime.parse(request.startTime().trim(), FLEXIBLE_FORMATTER);
                LocalDateTime end = LocalDateTime.parse(request.endTime().trim(), FLEXIBLE_FORMATTER);

                double total = pricingService.calculateBookingPrice(start, end);
                double rate = pricingService.getHourlyRate(start);
                String type = (rate > 1500.0) ? "Weekend Peak Rate (₹2300/hr)" : "Weekday Standard Rate (₹1500/hr)";

                return new PriceResponse(total, rate, type);
            } catch (Exception e) {
                return new PriceResponse(0.0, 0.0, "Unable to calculate rate: invalid date format provided.");
            }
        };
    }

    // --- 2. CHECK AVAILABILITY TOOL ---
    public record AvailabilityRequest(
        @JsonPropertyDescription("Pitch or turf name, e.g. Main Turf or Court 1")
        @JsonProperty(required = true) String turfName,

        @JsonPropertyDescription("Start date-time in ISO format, e.g. 2026-08-07T14:00:00")
        @JsonProperty(required = true) String startTime,

        @JsonPropertyDescription("End date-time in ISO format, e.g. 2026-08-07T16:00:00")
        @JsonProperty(required = true) String endTime
    ) {}

    public record AvailabilityResponse(boolean available, String message) {}

    @Bean
    @Description("Checks whether a specific turf/pitch is available for booking during a requested time slot.")
    public Function<AvailabilityRequest, AvailabilityResponse> checkAvailabilityTool() {
        return request -> {
            try {
                LocalDateTime start = LocalDateTime.parse(request.startTime().trim(), FLEXIBLE_FORMATTER);
                LocalDateTime end = LocalDateTime.parse(request.endTime().trim(), FLEXIBLE_FORMATTER);

                String turfName = (request.turfName() == null || request.turfName().isBlank()) 
                        ? "Main Turf" 
                        : request.turfName();

                if (end.isBefore(start) || end.isEqual(start)) {
                    end = start.plusHours(1);
                }

                List<TurfBooking> conflicts = bookingRepository.findOverlappingBookings(turfName, start, end);

                if (conflicts.isEmpty()) {
                    return new AvailabilityResponse(true, "The slot for " + turfName + " from " + start + " to " + end + " is available!");
                } else {
                    return new AvailabilityResponse(false, "The slot for " + turfName + " from " + start + " to " + end + " is already booked.");
                }
            } catch (Exception e) {
                return new AvailabilityResponse(false, "Could not check availability: invalid date format provided.");
            }
        };
    }
}