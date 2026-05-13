package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

public interface AiModelConfigService {
    Long create(AiModelConfigSaveReqVO reqVO);
    void update(AiModelConfigSaveReqVO reqVO);
    void delete(Long id);
    AiModelConfigRespVO get(Long id);
    PageResult<AiModelConfigRespVO> getPage(AiModelConfigPageReqVO pageReqVO);
    boolean testConnection(Long id);
}
