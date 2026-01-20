package com.triptribe.itineraryservice.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiItineraryResponse {
    private String title;
    private String description;
    private String pace;
    private List<String> assumptions;
    private List<AiDayPlan> days;
}
