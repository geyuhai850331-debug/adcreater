package com.djb.module.ai.service;

import com.djb.module.ai.adapter.AiRequest;
import com.djb.module.ai.adapter.AiResult;
import java.util.function.Consumer;

public interface ModelOrchestrationService {
    AiResult generateImage(AiRequest request, Consumer<String> progressCallback);
    AiResult generateVideo(AiRequest request, Consumer<String> progressCallback);
    AiResult translate(String sourceText, String sourceLang, String targetLang, String targetMarket);
    AiResult generateDigitalHuman(AiRequest request, Consumer<String> progressCallback);
}
