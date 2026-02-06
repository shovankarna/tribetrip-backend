package com.triptribe.itineraryservice.dto.itinerary;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TripItineraryResponse {
    private UUID id;
    private String tripId;
    private UUID templateId;
    private String createdByUserId;
    private List<TripItineraryItemResponse> items;
    private LocalDateTime createdAt;
}
