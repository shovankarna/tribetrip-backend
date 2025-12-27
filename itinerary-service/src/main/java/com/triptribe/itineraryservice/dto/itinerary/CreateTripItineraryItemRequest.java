package com.triptribe.itineraryservice.dto.itinerary;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateTripItineraryItemRequest {
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private Integer durationMinutes;
    private String locationText;
    private String notes;
    private Integer orderIndex;
    private Boolean isUnscheduled;
}
