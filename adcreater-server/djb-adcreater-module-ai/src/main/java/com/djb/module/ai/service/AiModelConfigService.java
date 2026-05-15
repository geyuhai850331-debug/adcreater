package com.djb.module.ai.service;

import com.djb.module.ai.controller.admin.vo.*;
import com.djb.framework.common.pojo.PageResult;

public interface AiModelConfigService {
    Long create(AiModelConfigSaveReqVO reqVO);
    void update(AiModelConfigSaveReqVO reqVO);
    void updateStatus(Long id, Boolean isEnabled);
    void delete(Long id);
    AiModelConfigRespVO get(Long id);
    PageResult<AiModelConfigRespVO> getPage(AiModelConfigPageReqVO pageReqVO);
    boolean testConnection(Long id);
    boolean testConnection(AiModelConfigTestReqVO reqVO);
}
