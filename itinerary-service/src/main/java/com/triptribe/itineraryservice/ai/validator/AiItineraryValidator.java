package com.triptribe.itineraryservice.ai.validator;

import com.triptribe.itineraryservice.ai.dto.AiActivityItem;
import com.triptribe.itineraryservice.ai.dto.AiDayPlan;
import com.triptribe.itineraryservice.ai.dto.AiItineraryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class AiItineraryValidator {

    public void validate(AiItineraryResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("AI response cannot be null");
        }

        validateBasicFields(response);
        validateDays(response.getDays());
    }

    private void validateBasicFields(AiItineraryResponse response) {
        if (response.getTitle() == null || response.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title is missing");
        }
        if (response.getDays() == null || response.getDays().isEmpty()) {
            throw new IllegalArgumentException("Days are missing");
        }
    }

    private void validateDays(List<AiDayPlan> days) {
        Set<Integer> dayOffsets = new HashSet<>();

        for (AiDayPlan day : days) {
            if (day.getDayOffset() < 0) {
                throw new IllegalArgumentException("Day offset cannot be negative: " + day.getDayOffset());
            }
            if (!dayOffsets.add(day.getDayOffset())) {
                throw new IllegalArgumentException("Duplicate day offset found: " + day.getDayOffset());
            }
            if (day.getItems() != null) {
                validateItems(day.getItems());
            }
        }
    }

    private void validateItems(List<AiActivityItem> items) {
        int previousOrder = -1;

        for (AiActivityItem item : items) {
            if (item.getTitle() == null || item.getTitle().isEmpty()) {
                throw new IllegalArgumentException("Activity title is missing");
            }

            if (item.getOrder() <= previousOrder) {
                throw new IllegalArgumentException("Activity order must be sequential and increasing. Found "
                        + item.getOrder() + " after " + previousOrder);
            }
            previousOrder = item.getOrder();

            if (item.getDurationMinutes() != null && item.getDurationMinutes() < 0) {
                throw new IllegalArgumentException("Duration cannot be negative for activity: " + item.getTitle());
            }
        }
    }
}
