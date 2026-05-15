package com.djb.module.ai.adapter;

import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import com.djb.module.ai.util.AiApiKeyCodec;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Component
public class StabilityAdapter implements AiModelAdapter {

    private final WebClient webClient = WebClient.create();

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        String apiKey = AiApiKeyCodec.normalizeForAuthorization(AiApiKeyCodec.decode(config.getApiKey()));

        Map<String, Object> body = Map.of(
            "prompt", request.getPrompt(),
            "negative_prompt", request.getNegativePrompt() != null ? request.getNegativePrompt() : "",
            "output_format", "jpeg"
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.post()
            .uri("https://api.stability.ai/v2beta/stable-image/generate/sd3")
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        String base64Image = (String) response.get("image");
        return AiResult.builder()
            .success(true)
            .url("data:image/jpeg;base64," + base64Image)
            .build();
    }

    @Override
    public boolean validateConfig(AiModelConfigDO config) {
        try {
            String apiKey = AiApiKeyCodec.normalizeForAuthorization(AiApiKeyCodec.decode(config.getApiKey()));
            Map<String, Object> response = webClient.get()
                .uri("https://api.stability.ai/v1/user/account")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            return response != null && response.containsKey("email");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int estimateCost(AiRequest request) {
        return 5;
    }
}
