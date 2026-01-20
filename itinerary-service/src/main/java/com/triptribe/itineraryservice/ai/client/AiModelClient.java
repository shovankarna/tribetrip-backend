package com.triptribe.itineraryservice.ai.client;

import com.triptribe.itineraryservice.ai.dto.AiItineraryResponse;

public interface AiModelClient {
    AiItineraryResponse generateItinerary(String systemPrompt, String developerPrompt);
}
