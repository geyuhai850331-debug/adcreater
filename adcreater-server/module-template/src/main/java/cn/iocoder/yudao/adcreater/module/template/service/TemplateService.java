package cn.iocoder.yudao.adcreater.module.template.service;

import cn.iocoder.yudao.adcreater.module.template.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.template.controller.app.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.util.List;

public interface TemplateService {
    Long createTemplate(TemplateSaveReqVO reqVO);
    void updateTemplate(TemplateSaveReqVO reqVO);
    void deleteTemplate(Long id);
    TemplateRespVO getTemplate(Long id);
    PageResult<TemplateRespVO> getTemplatePage(TemplatePageReqVO pageReqVO);
    Long publishVersion(Long templateId, String fileUrl, String changelog);
    List<TemplateVersionRespVO> getVersions(Long templateId);
    SyncResultVO syncTemplates(SyncRequestVO reqVO);
    String getDownloadUrl(Long templateVersionId);
}
