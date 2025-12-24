package com.triptribe.tripservice.dto;

import com.triptribe.tripservice.entity.TripRole;
import com.triptribe.tripservice.entity.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripResponse {
    private String id;
    private String name;
    private String description;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdByUserId;
    private TripStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // Derived field
    private TripRole myRole;
}
