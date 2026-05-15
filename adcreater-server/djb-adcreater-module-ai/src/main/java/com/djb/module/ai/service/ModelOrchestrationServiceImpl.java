package com.djb.module.ai.service;

import com.djb.module.ai.adapter.AiModelAdapter;
import com.djb.module.ai.adapter.AiModelAdapterResolver;
import com.djb.module.ai.adapter.AiRequest;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import com.djb.module.ai.dal.mapper.AiModelConfigMapper;
import com.djb.common.exception.AiCallException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;

@Service
@Slf4j
public class ModelOrchestrationServiceImpl implements ModelOrchestrationService {

    @Resource
    private AiModelConfigMapper aiModelConfigMapper;

    @Resource
    private ApplicationContext applicationContext;

    /** 硬编码 fallback：模型管理未实现时使用 spring.ai.openai 配置 */
    @Value("${spring.ai.openai.api-key:}")
    private String fallbackApiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String fallbackBaseUrl;

    @Override
    public AiResult generateImage(AiRequest request, Consumer<String> progressCallback) {
        return executeWithFallback("image", request, progressCallback);
    }

    @Override
    public AiResult generateVideo(AiRequest request, Consumer<String> progressCallback) {
        return executeWithFallback("video", request, progressCallback);
    }

    @Override
    public AiResult translate(String sourceText, String sourceLang, String targetLang, String targetMarket) {
        AiRequest request = new AiRequest();
        request.setPrompt(sourceText);
        request.getExtraParams().put("sourceLang", sourceLang);
        request.getExtraParams().put("targetLang", targetLang);
        request.getExtraParams().put("targetMarket", targetMarket);
        return executeWithFallback("copy", request, null);
    }

    @Override
    public AiResult generateDigitalHuman(AiRequest request, Consumer<String> progressCallback) {
        return executeWithFallback("digital_human", request, progressCallback);
    }

    private AiResult executeWithFallback(String taskType, AiRequest request,
                                          Consumer<String> progressCallback) {
        List<AiModelConfigDO> configs = aiModelConfigMapper.selectList(
            new LambdaQueryWrapper<AiModelConfigDO>()
                .eq(AiModelConfigDO::getIsEnabled, true)
                .orderByAsc(AiModelConfigDO::getPriority));

        if (configs.isEmpty()) {
            log.warn("No enabled model in DB for task type: {}, using hardcoded fallback", taskType);
            return executeWithFallbackConfig(request);
        }

        Exception lastException = null;
        for (AiModelConfigDO config : configs) {
            try {
                AiModelAdapter adapter = getAdapter(config.getAdapterClass());
                if (progressCallback != null) {
                    progressCallback.accept("正在调用 " + config.getModelName() + "...");
                }
                AiResult result = adapter.call(request, config);
                if (result.isSuccess()) {
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
                if (progressCallback != null) {
                    progressCallback.accept(config.getModelName() + " 调用失败，尝试备用模型...");
                }
            }
        }

        String msg = lastException != null ? lastException.getMessage() : "All models failed";
        return AiResult.builder().success(false).errorMessage(msg).build();
    }

    /**
     * 硬编码 fallback：模型管理未实现时，使用 application.yaml 中的 OpenAI 配置
     * 后续模型管理上线后，此方法可删除，走 DB 查询路径
     */
    private AiResult executeWithFallbackConfig(AiRequest request) {
        if (fallbackApiKey == null || fallbackApiKey.isBlank()) {
            return AiResult.builder().success(false)
                .errorMessage("No model configured. Please set spring.ai.openai.api-key in application.yaml").build();
        }
        AiModelConfigDO fallbackConfig = new AiModelConfigDO();
        fallbackConfig.setModelName("gpt-4o-mini");
        fallbackConfig.setAdapterClass("com.djb.module.ai.adapter.OpenAIChatAdapter");
        fallbackConfig.setApiKey(Base64.getEncoder().encodeToString(fallbackApiKey.getBytes()));
        fallbackConfig.setEndpointUrl(fallbackBaseUrl);
        fallbackConfig.setIsEnabled(true);
        fallbackConfig.setPriority(0);

        AiModelAdapter adapter = getAdapter(fallbackConfig.getAdapterClass());
        return adapter.call(request, fallbackConfig);
    }

    private AiModelAdapter getAdapter(String adapterClassName) {
        Map<String, AiModelAdapter> adapters = applicationContext.getBeansOfType(AiModelAdapter.class);
        return AiModelAdapterResolver.resolve(adapters, adapterClassName)
            .orElseThrow(() -> AiCallException.of("unknown",
                "Adapter not found: " + adapterClassName));
    }
}
