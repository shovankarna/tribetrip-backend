package com.triptribe.itineraryservice.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDayPlan {
    private int dayOffset;
    private List<AiActivityItem> items;
}
