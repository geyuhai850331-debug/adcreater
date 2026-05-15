package com.djb.module.ai.adapter;

import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OpenAIAdapter implements AiModelAdapter {

    private final WebClient webClient = WebClient.create();

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        String apiKey = new String(Base64.getDecoder().decode(config.getApiKey()));
        String endpoint = buildApiPath(config.getEndpointUrl(), "/images/generations");

        String size = (request.getWidth() != null && request.getHeight() != null)
                ? request.getWidth() + "x" + request.getHeight()
                : "1024x1024";

        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : defaultModel(config),
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
        String modelName = config.getModelName();
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }

        List<Map<String, Object>> models = fetchModels(config);
        boolean matched = models.stream()
                .map(model -> model.get("id"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .anyMatch(modelName::equals);
        if (!matched) {
            throw new IllegalArgumentException("模型不存在或当前 API Key 无权访问该模型: " + modelName);
        }
        return true;
    }

    @Override
    public int estimateCost(AiRequest request) {
        return 10;
    }

    private List<Map<String, Object>> fetchModels(AiModelConfigDO config) {
        String apiKey = new String(Base64.getDecoder().decode(config.getApiKey()));
        RuntimeException lastException = null;
        for (String modelsEndpoint : buildModelListEndpoints(config.getEndpointUrl())) {
            try {
                Map<String, Object> response = webClient.get()
                        .uri(modelsEndpoint)
                        .header("Authorization", "Bearer " + apiKey)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                Object data = response != null ? response.get("data") : null;
                if (data instanceof List<?> dataList && !dataList.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> models = (List<Map<String, Object>>) dataList;
                    return models;
                }
            } catch (RuntimeException e) {
                lastException = e;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new IllegalArgumentException("模型列表为空，请检查接口地址和 API Key");
    }

    private List<String> buildModelListEndpoints(String endpointUrl) {
        String rawEndpoint = normalizeEndpoint(endpointUrl);
        Set<String> endpoints = new LinkedHashSet<>();
        endpoints.add(rawEndpoint + "/models");
        if (!rawEndpoint.endsWith("/v1")) {
            endpoints.add(rawEndpoint + "/v1/models");
        }
        if (rawEndpoint.endsWith("/v1")) {
            endpoints.add(rawEndpoint.substring(0, rawEndpoint.length() - 3) + "/models");
        }
        return List.copyOf(endpoints);
    }

    private String buildApiPath(String endpointUrl, String suffix) {
        String endpoint = normalizeEndpoint(endpointUrl);
        if (!endpoint.endsWith("/v1")) {
            endpoint = endpoint + "/v1";
        }
        return endpoint + suffix;
    }

    private String normalizeEndpoint(String endpointUrl) {
        String endpoint = endpointUrl != null && !endpointUrl.isBlank() ? endpointUrl.trim() : "https://api.openai.com";
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
    }

    private String defaultModel(AiModelConfigDO config) {
        return config.getModelName() != null && !config.getModelName().isBlank() ? config.getModelName() : "dall-e-3";
    }
}
