package com.djb.module.ai.adapter;

import com.djb.module.ai.dal.dataobject.AiModelConfigDO;

public interface AiModelAdapter {
    AiResult call(AiRequest request, AiModelConfigDO config);
    boolean validateConfig(AiModelConfigDO config);
    int estimateCost(AiRequest request);
}
