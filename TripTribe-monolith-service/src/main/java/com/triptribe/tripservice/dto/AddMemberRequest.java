package com.triptribe.tripservice.dto;

import com.triptribe.tripservice.entity.TripRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberRequest {
    private String userId;
    private TripRole role;
}
