package com.triptribe.tripservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTripRequest {
    private String name;
    private String description;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
}
