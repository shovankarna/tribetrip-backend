package com.triptribe.tripservice.dto.internal;

import com.triptribe.tripservice.entity.TripRole;
import com.triptribe.tripservice.entity.TripStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TripPermissionResponse {
    private String tripId;
    private TripRole role;
    private TripStatus status;
    private LocalDate startDate;
}
