package com.triptribe.itineraryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "trip_itineraries")
public class TripItinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trip_id", nullable = false, unique = true) // One active itinerary per trip
    private String tripId;

    @Column(name = "template_id")
    private UUID templateId; // Optional reference to source template

    @Column(name = "created_by_user_id", nullable = false)
    private String createdByUserId;

    @OneToMany(mappedBy = "tripItinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripItineraryItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
