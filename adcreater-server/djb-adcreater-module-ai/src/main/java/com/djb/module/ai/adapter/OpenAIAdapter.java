package com.djb.module.ai.adapter;

import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIAdapter implements AiModelAdapter {

    private final WebClient webClient = WebClient.create();

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        String apiKey = new String(Base64.getDecoder().decode(config.getApiKey()));
        String endpoint = (config.getEndpointUrl() != null ? config.getEndpointUrl() : "https://api.openai.com")
                + "/v1/images/generations";

        String size = (request.getWidth() != null && request.getHeight() != null)
                ? request.getWidth() + "x" + request.getHeight()
                : "1024x1024";

        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : "dall-e-3",
            "prompt", request.getPrompt(),
            "n", 1,
            "size", size,
            "quality", "hd"
        );

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
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        String url = (String) data.get(0).get("url");

        return AiResult.builder()
            .success(true)
            .url(url)
            .revisedPrompt(data.get(0).get("revised_prompt") != null
                ? (String) data.get(0).get("revised_prompt") : null)
            .build();
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
        return 10;
    }
}
