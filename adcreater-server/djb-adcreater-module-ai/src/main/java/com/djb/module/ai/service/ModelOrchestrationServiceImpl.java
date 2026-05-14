package com.djb.module.ai.service;

import com.djb.module.ai.adapter.AiModelAdapter;
import com.djb.module.ai.adapter.AiRequest;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import com.djb.module.ai.dal.mapper.AiModelConfigMapper;
import com.djb.common.exception.AiCallException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;

@Service
public class ModelOrchestrationServiceImpl implements ModelOrchestrationService {

    @Resource
    private AiModelConfigMapper aiModelConfigMapper;

    @Resource
    private ApplicationContext applicationContext;

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
            return AiResult.builder().success(false)
                .errorMessage("No enabled model found for task type: " + taskType).build();
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

    private AiModelAdapter getAdapter(String adapterClassName) {
        Map<String, AiModelAdapter> adapters = applicationContext.getBeansOfType(AiModelAdapter.class);
        return adapters.values().stream()
            .filter(a -> a.getClass().getName().equals(adapterClassName))
            .findFirst()
            .orElseThrow(() -> AiCallException.of("unknown",
                "Adapter not found: " + adapterClassName));
    }
}
