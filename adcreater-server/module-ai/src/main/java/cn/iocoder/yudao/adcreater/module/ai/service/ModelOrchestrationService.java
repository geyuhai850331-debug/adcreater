package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.adapter.AiRequest;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiResult;
import java.util.function.Consumer;

public interface ModelOrchestrationService {
    AiResult generateImage(AiRequest request, Consumer<String> progressCallback);
    AiResult generateVideo(AiRequest request, Consumer<String> progressCallback);
    AiResult translate(String sourceText, String sourceLang, String targetLang, String targetMarket);
    AiResult generateDigitalHuman(AiRequest request, Consumer<String> progressCallback);
}
