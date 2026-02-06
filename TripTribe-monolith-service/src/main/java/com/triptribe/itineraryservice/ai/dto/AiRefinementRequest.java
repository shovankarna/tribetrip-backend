package com.triptribe.itineraryservice.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRefinementRequest {

    @NotNull(message = "Refinement type is required")
    private RefinementType refinementType;

    private String value;

    public enum RefinementType {
        PACE_CHANGE,
        ADD_ACTIVITY_TYPE,
        REMOVE_ACTIVITY_TYPE,
        SHORTER_DAYS,
        LONGER_DAYS
    }
}
