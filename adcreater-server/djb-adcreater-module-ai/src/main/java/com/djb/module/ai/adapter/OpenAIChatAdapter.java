package com.djb.module.ai.adapter;

import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Component
public class OpenAIChatAdapter implements AiModelAdapter {

    private final WebClient webClient = WebClient.create();

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        try {
            String apiKey = new String(Base64.getDecoder().decode(config.getApiKey()));
            String endpoint = (config.getEndpointUrl() != null ? config.getEndpointUrl() : "https://api.openai.com")
                    + "/v1/chat/completions";

            String model = request.getModel() != null ? request.getModel() : "gpt-4o-mini";

            List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", request.getPrompt())
            );

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 2048);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .uri(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return AiResult.builder()
                    .success(false)
                    .errorMessage("AI response contained no choices")
                    .build();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message != null ? (String) message.get("content") : null;
            if (content == null) {
                return AiResult.builder()
                    .success(false)
                    .errorMessage("AI response contained no content (possibly content-filtered)")
                    .build();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            int inputTokens = usage != null && usage.get("prompt_tokens") instanceof Number
                ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
            int outputTokens = usage != null && usage.get("completion_tokens") instanceof Number
                ? ((Number) usage.get("completion_tokens")).intValue() : 0;

            return AiResult.builder()
                .success(true)
                .revisedPrompt(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .build();
        } catch (Exception e) {
            return AiResult.builder()
                .success(false)
                .errorMessage("Chat completion failed: " + e.getMessage())
                .build();
        }
    }

    @Override
    public boolean validateConfig(AiModelConfigDO config) {
        try {
            String apiKey = new String(Base64.getDecoder().decode(config.getApiKey()));
            String endpoint = (config.getEndpointUrl() != null ? config.getEndpointUrl() : "https://api.openai.com");
            Map<String, Object> response = webClient.get()
                .uri(endpoint + "/v1/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            return response != null && response.containsKey("data");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int estimateCost(AiRequest request) {
        return 5;
    }
}
