package com.turf.booking_system.ai;

import com.turf.booking_system.service.PricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
import java.util.function.Function;

@Configuration
public class BookingTools {

    private final PricingService pricingService;

    public BookingTools(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    public record PriceRequest(String startTime, String endTime) {}
    public record PriceResponse(double totalPrice, double hourlyRate, String rateType) {}

    @Bean
    @Description("Calculates total booking cost for a given pitch based on weekend and weekday pricing rules.")
    public Function<PriceRequest, PriceResponse> calculatePriceTool() {
        return request -> {
            LocalDateTime start = LocalDateTime.parse(request.startTime());
            LocalDateTime end = LocalDateTime.parse(request.endTime());

            double total = pricingService.calculateBookingPrice(start, end);
            double rate = pricingService.getHourlyRate(start);
            String type = (rate > 1500.0) ? "Weekend Peak Rate (₹2300/hr)" : "Weekday Standard Rate (₹1500/hr)";

            return new PriceResponse(total, rate, type);
        };
    }
}
