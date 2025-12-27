package com.triptribe.itineraryservice.dto.internal;

import com.triptribe.itineraryservice.entity.TripRole;
import com.triptribe.itineraryservice.entity.TripStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripPermissionResponse {
    private String tripId;
    private TripRole role;
    private TripStatus status;
    private LocalDate startDate;
}
