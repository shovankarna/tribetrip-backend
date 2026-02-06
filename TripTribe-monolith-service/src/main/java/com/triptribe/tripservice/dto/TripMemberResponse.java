package com.triptribe.tripservice.dto;

import com.triptribe.tripservice.entity.TripRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripMemberResponse {
    private String userId;
    private TripRole role;
    private Instant joinedAt;
    private boolean active;
}
