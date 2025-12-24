package com.triptribe.tripservice.mapper;

import com.triptribe.tripservice.dto.TripMemberResponse;
import com.triptribe.tripservice.dto.TripResponse;
import com.triptribe.tripservice.entity.Trip;
import com.triptribe.tripservice.entity.TripMember;
import com.triptribe.tripservice.entity.TripRole;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {

    public TripResponse toDto(Trip trip, TripRole myRole) {
        if (trip == null) {
            return null;
        }
        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .description(trip.getDescription())
                .destination(trip.getDestination())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .createdByUserId(trip.getCreatedByUserId())
                .status(trip.getStatus())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .myRole(myRole)
                .build();
    }

    public TripMemberResponse toDto(TripMember member) {
        if (member == null) {
            return null;
        }
        return TripMemberResponse.builder()
                .userId(member.getUserId())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .active(member.isActive())
                .build();
    }
}
