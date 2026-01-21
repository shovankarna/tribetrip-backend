package com.triptribe.itineraryservice.ai.prompt;

import com.triptribe.itineraryservice.ai.dto.AiItineraryRequest;
import com.triptribe.itineraryservice.ai.dto.AiRefinementRequest;
import org.springframework.stereotype.Component;

@Component
public class DeveloperPromptProvider {

    public String createGenerationPrompt(AiItineraryRequest request) {
        return String.format("""
                Generate a %d-day %s itinerary for a %s group of %d people to %s.

                Constraints:
                - Travel Month: %s
                - Budget: %s
                - Pace: %s
                - Trip Type: %s

                Please provide a day-by-day plan including suggested activities, locations, and approximate timings.
                Consider the typical weather and seasonal events for the specified month in the destination.
                IMPORTANT: The output must use relative days (e.g., "Day 1", "Day 2") and NOT specific calendar dates.
                Ensure the activities match the %s pace and %s budget.
                """,
                request.getDurationDays(),
                request.getTripType(),
                request.getGroupType(),
                request.getNumberOfPeople(),
                request.getDestination(),
                request.getMonth(),
                request.getBudgetType(),
                request.getPace(),
                request.getTripType(),
                request.getPace(),
                request.getBudgetType());
    }

    public String createRefinementPrompt(String currentItineraryJson, AiRefinementRequest request) {
        return String.format("""
                Based on the following existing itinerary:
                %s

                Please MODIFY this itinerary according to the following instruction:
                Refinement Type: %s
                Value: %s

                Return the FULL updated JSON structure with the changes applied.
                Do not change parts of the itinerary that are not affected by this refinement.
                """,
                currentItineraryJson,
                request.getRefinementType(),
                request.getValue());
    }
}
