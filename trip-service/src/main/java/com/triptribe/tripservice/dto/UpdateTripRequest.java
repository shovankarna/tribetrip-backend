package com.triptribe.tripservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateTripRequest {
    private String name;
    private String description;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
}
