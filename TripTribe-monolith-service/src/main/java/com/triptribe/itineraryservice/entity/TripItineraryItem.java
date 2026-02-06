package com.triptribe.itineraryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "trip_itinerary_items")
public class TripItineraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_itinerary_id", nullable = false)
    @ToString.Exclude
    private TripItinerary tripItinerary;

    @Column(nullable = false)
    private String title;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "location_text")
    private String locationText;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_unscheduled", nullable = false)
    private boolean isUnscheduled = false;

    @Column(nullable = false)
    private boolean completed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
