package cn.iocoder.yudao.adcreater.module.ai.adapter;

import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.AiModelConfigDO;

public interface AiModelAdapter {
    AiResult call(AiRequest request, AiModelConfigDO config);
    boolean validateConfig(AiModelConfigDO config);
    int estimateCost(AiRequest request);
}
