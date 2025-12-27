package com.triptribe.itineraryservice.dto.template;

import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class ItineraryTemplateItemResponse {
    private UUID id;
    private String title;
    private String description;
    private Integer defaultDayOffset;
    private LocalTime defaultStartTime;
    private Integer defaultDurationMinutes;
    private String locationText;
    private String notes;
    private Integer orderIndex;
}
