package com.triptribe.tripservice.controller.internal;

import com.triptribe.tripservice.dto.internal.TripPermissionResponse;
import com.triptribe.tripservice.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/trips")
@RequiredArgsConstructor
public class TripInternalController {

    private final TripService tripService;

    @GetMapping("/{tripId}/permissions")
    public TripPermissionResponse getTripPermissions(
            @PathVariable String tripId,
            @RequestParam String userId) {
        return tripService.getTripPermission(tripId, userId);
    }
}
