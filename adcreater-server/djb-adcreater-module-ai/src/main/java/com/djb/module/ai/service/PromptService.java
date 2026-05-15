package com.djb.module.ai.service;

import com.djb.module.ai.controller.admin.vo.*;
import com.djb.framework.common.pojo.PageResult;
import java.util.Map;

public interface PromptService {
    Long create(PromptTemplateSaveReqVO reqVO);
    void update(PromptTemplateSaveReqVO reqVO);
    void updateStatus(Long id, Boolean isEnabled);
    void delete(Long id);
    PromptTemplateRespVO get(Long id);
    PageResult<PromptTemplateRespVO> getPage(PromptTemplatePageReqVO pageReqVO);
    String resolvePrompt(String category, Map<String, String> variables);
}
