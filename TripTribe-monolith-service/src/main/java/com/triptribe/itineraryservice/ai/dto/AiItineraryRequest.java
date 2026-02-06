package com.triptribe.itineraryservice.ai.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiItineraryRequest {

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Month is required")
    private String month;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 30, message = "Duration cannot exceed 30 days")
    private int durationDays;

    @NotNull(message = "Budget type is required")
    private BudgetType budgetType;

    @NotNull(message = "Trip type is required")
    private TripType tripType;

    @NotNull(message = "Group type is required")
    private GroupType groupType;

    @Min(value = 1, message = "Number of people must be at least 1")
    private int numberOfPeople;

    @NotNull(message = "Pace is required")
    private Pace pace;

    public enum BudgetType {
        LOW, MID, HIGH, LUXURY
    }

    public enum TripType {
        LEISURE, ADVENTURE, CULTURE, FOODIE, RELAXED, NIGHTLIFE, MIXED
    }

    public enum GroupType {
        SOLO, COUPLE, FAMILY, FRIENDS, BUSINESS
    }

    public enum Pace {
        RELAXED, BALANCED, PACKED
    }
}
