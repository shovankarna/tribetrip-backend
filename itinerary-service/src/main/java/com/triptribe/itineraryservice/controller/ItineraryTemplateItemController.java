package com.triptribe.itineraryservice.controller;

import com.triptribe.itineraryservice.config.UserContext;
import com.triptribe.itineraryservice.dto.template.ItineraryTemplateItemResponse;
import com.triptribe.itineraryservice.dto.template.UpdateTemplateItemRequest;
import com.triptribe.itineraryservice.service.ItineraryTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/itinerary-template-items")
@RequiredArgsConstructor
public class ItineraryTemplateItemController {

    private final ItineraryTemplateService templateService;
    private final UserContext userContext;

    @PutMapping("/{itemId}")
    public ItineraryTemplateItemResponse updateItem(
            @PathVariable UUID itemId,
            @RequestBody UpdateTemplateItemRequest request) {
        return templateService.updateItem(itemId, request, userContext.getUserId());
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable UUID itemId) {
        templateService.deleteItem(itemId, userContext.getUserId());
    }
}
