package com.djb.module.template.service;

import com.djb.module.template.controller.admin.vo.*;
import com.djb.module.template.controller.app.vo.*;
import com.djb.framework.common.pojo.PageResult;
import java.util.List;

public interface TemplateService {
    Long createTemplate(TemplateSaveReqVO reqVO);
    void updateTemplate(TemplateSaveReqVO reqVO);
    void deleteTemplate(Long id);
    TemplateRespVO getTemplate(Long id);
    PageResult<TemplateRespVO> getTemplatePage(TemplatePageReqVO pageReqVO);
    List<AppTemplateSimpleRespVO> getPublishedTemplateList();
    Long publishVersion(Long templateId, String fileUrl, String changelog);
    List<TemplateVersionRespVO> getVersions(Long templateId);
    SyncResultVO syncTemplates(SyncRequestVO reqVO);
    String getDownloadUrl(Long templateVersionId);
}
