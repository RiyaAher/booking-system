package com.turf.booking_system.service;

import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PricingService {

    // Define base hourly rates
    public static final double WEEKDAY_HOURLY_RATE = 1500.0;
    public static final double WEEKEND_HOURLY_RATE = 2300.0;

    /**
     * Calculates the total price for a given duration.
     */
    public double calculateBookingPrice(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
            return 0.0;
        }

        // Duration in exact hours (fractional)
        double hours = Duration.between(startTime, endTime).toMinutes() / 60.0;
        double hourlyRate = getHourlyRate(startTime);

        // Round to 2 decimal places
        return Math.round(hours * hourlyRate * 100.0) / 100.0;
    }

    /**
     * Determines whether a given slot is subject to Weekend or Weekday pricing.
     */
    public double getHourlyRate(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
        return isWeekend ? WEEKEND_HOURLY_RATE : WEEKDAY_HOURLY_RATE;
    }
}
