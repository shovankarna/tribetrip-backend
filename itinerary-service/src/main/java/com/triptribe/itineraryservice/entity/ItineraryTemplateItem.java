package com.triptribe.itineraryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "itinerary_template_items")
public class ItineraryTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @ToString.Exclude
    private ItineraryTemplate template;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_day_offset", nullable = false)
    private Integer defaultDayOffset = 0; // 0 = first day, 1 = second day, etc.

    @Column(name = "default_start_time")
    private LocalTime defaultStartTime;

    @Column(name = "default_duration_minutes")
    private Integer defaultDurationMinutes;

    @Column(name = "location_text")
    private String locationText;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
