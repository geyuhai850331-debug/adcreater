package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.util.Map;

public interface PromptService {
    Long create(PromptTemplateSaveReqVO reqVO);
    void update(PromptTemplateSaveReqVO reqVO);
    void delete(Long id);
    PromptTemplateRespVO get(Long id);
    PageResult<PromptTemplateRespVO> getPage(PromptTemplatePageReqVO pageReqVO);
    String resolvePrompt(String category, Map<String, String> variables);
}
