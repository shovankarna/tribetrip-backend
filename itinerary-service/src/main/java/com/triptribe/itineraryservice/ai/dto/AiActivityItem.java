package com.triptribe.itineraryservice.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiActivityItem {
    private String title;
    private String description;
    private String startTime; // format HH:mm
    private Integer durationMinutes;
    private String location;
    private int order;
    private String notes;
}
