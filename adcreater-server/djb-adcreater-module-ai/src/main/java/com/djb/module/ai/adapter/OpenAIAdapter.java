package com.djb.module.ai.adapter;

import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import com.djb.module.ai.util.AiApiKeyCodec;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
public class OpenAIAdapter implements AiModelAdapter {

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        try {
            String apiKey = config.getApiKey();
            String endpoint = config.getEndpointUrl();
            String model = config.getModelName() ;

            // Build OpenAiApi with dynamic endpoint and API key
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(endpoint)
                    .apiKey(apiKey)
                    .build();

            // Build OpenAiChatModel with non-streaming options
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(0.7)
                    .maxTokens(2048)
                    .build();
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(options)
                    .build();

            // Non-streaming call
            ChatResponse response = chatModel.call(
                    new Prompt(new UserMessage(request.getPrompt())));

            if (response == null || response.getResult() == null) {
                return AiResult.builder()
                        .success(false)
                        .errorMessage("AI response was empty")
                        .build();
            }

            String content = response.getResult().getOutput().getText();
            if (content == null || content.isEmpty()) {
                return AiResult.builder()
                        .success(false)
                        .errorMessage("AI response contained no content (possibly content-filtered)")
                        .build();
            }

            Usage usage = response.getMetadata().getUsage();
            int inputTokens = usage != null ? usage.getPromptTokens().intValue() : 0;
            int outputTokens = usage != null ? usage.getCompletionTokens().intValue() : 0;

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
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            return false;
        }
        // Cannot validate API key without a models-list endpoint; optimistic validation
        return true;
    }

    @Override
    public int estimateCost(AiRequest request) {
        return 5;
    }
}
