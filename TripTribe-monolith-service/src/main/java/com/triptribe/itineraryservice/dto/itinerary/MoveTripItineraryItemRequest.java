package com.triptribe.itineraryservice.dto.itinerary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveTripItineraryItemRequest {
    private LocalDate newDate;
    private Integer newOrderIndex;
}
