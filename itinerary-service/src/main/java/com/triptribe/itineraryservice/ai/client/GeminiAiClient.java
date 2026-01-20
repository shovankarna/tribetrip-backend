package com.triptribe.itineraryservice.ai.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptribe.itineraryservice.ai.dto.AiItineraryResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiAiClient implements AiModelClient {

    @Qualifier("geminiRestClient")
    private final RestClient restClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    @Override
    public AiItineraryResponse generateItinerary(String systemPrompt, String developerPrompt) {
        try {
            GeminiRequest request = new GeminiRequest(
                    List.of(
                            new Content("user", List.of(new TextPart(systemPrompt + "\n\n" + developerPrompt)))));

            log.info("Sending request to Gemini API");

            GeminiResponse response = restClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
                throw new RuntimeException("Empty response from Gemini API");
            }

            String jsonText = response.getCandidates().get(0).getContent().getParts().get(0).getText();

            // Cleanup markdown code blocks if present
            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.substring(3);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }

            return objectMapper.readValue(jsonText, AiItineraryResponse.class);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response", e);
            throw new RuntimeException("Invalid JSON received from AI", e);
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw new RuntimeException("AI generation failed: " + e.getMessage(), e);
        }
    }

    // internal DTOs for Gemini API
    @Data
    @RequiredArgsConstructor
    private static class GeminiRequest {
        @JsonProperty("contents")
        private final List<Content> contents;
    }

    @Data
    @RequiredArgsConstructor
    private static class Content {
        @JsonProperty("role")
        private final String role;
        @JsonProperty("parts")
        private final List<TextPart> parts;
    }

    @Data
    @RequiredArgsConstructor
    private static class TextPart {
        @JsonProperty("text")
        private final String text;
    }

    @Data
    private static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    private static class Candidate {
        private Content content;
    }
}
