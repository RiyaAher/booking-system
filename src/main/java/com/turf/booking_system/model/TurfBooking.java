package com.turf.booking_system.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 1. @Entity tells Spring: "Turn this Java class into a physical table in MySQL"
@Entity
// 2. @Table explicitly names our MySQL table 'turf_bookings'
@Table(name = "turf_bookings")
public class TurfBooking {

    // 3. @Id and @GeneratedValue tell MySQL this is our auto-incrementing Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 4. @Column maps these variables directly to specific columns in our table row
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "turf_name", nullable = false)
    private String turfName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // NEW COLUMN: Stores the calculated booking cost
    @Column(name = "total_price")
    private Double totalPrice;

    // --- CRUCIAL: Empty constructor required by Hibernate to boot up ---
    public TurfBooking() {}

    // --- Getters and Setters so Spring can read/write data to our fields ---
    public Long getId() { return id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTurfName() { return turfName; }
    public void setTurfName(String turfName) { this.turfName = turfName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    // Getter and Setter for totalPrice
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}