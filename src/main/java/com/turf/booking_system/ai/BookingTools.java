package com.turf.booking_system.ai;

import com.turf.booking_system.service.PricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.function.Function;

@Configuration
public class BookingTools {

    private final PricingService pricingService;

    // Flexible ISO formatter that handles missing seconds or varying precision
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd['T'][ ]HH:mm")
            .optionalStart().appendPattern(":ss").optionalEnd()
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    public BookingTools(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    public record PriceRequest(
        @Description("Start date-time in ISO format, e.g. 2026-07-25T14:00:00") String startTime,
        @Description("End date-time in ISO format, e.g. 2026-07-25T16:00:00") String endTime
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
                // Return an error structure back to the LLM so it can politely inform the user
                return new PriceResponse(0.0, 0.0, "Unable to calculate rate: invalid date format provided.");
            }
        };
    }
}