package com.triptribe.itineraryservice.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.triptribe.itineraryservice.ai.dto.AiItineraryResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiAiClient implements AiModelClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String modelName;

    private final ObjectMapper objectMapper;
    private Client client;

    @PostConstruct
    public void init() {
        // Initialize the SDK Client with the API key
        this.client = Client.builder().apiKey(apiKey).build();
    }

    @Override
    public AiItineraryResponse generateItinerary(String systemPrompt, String developerPrompt) {
        try {
            log.info("Sending request to Gemini API via SDK. Model: {}", modelName);

            String combinedPrompt = systemPrompt + "\n\n" + developerPrompt;

            GenerateContentResponse response = client.models.generateContent(
                    modelName,
                    combinedPrompt,
                    null);

            if (response == null || response.text() == null) {
                throw new RuntimeException("Empty response from Gemini SDK");
            }

            String jsonText = response.text();

            // Cleanup markdown code blocks if present (SDK might return raw markdown)
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
}
