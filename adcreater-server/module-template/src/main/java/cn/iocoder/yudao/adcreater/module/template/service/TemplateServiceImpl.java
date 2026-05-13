package cn.iocoder.yudao.adcreater.module.template.service;

import cn.iocoder.yudao.adcreater.module.template.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.template.controller.app.vo.*;
import cn.iocoder.yudao.adcreater.module.template.dal.dataobject.*;
import cn.iocoder.yudao.adcreater.module.template.dal.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private TemplateVersionMapper versionMapper;

    @Override
    public Long createTemplate(TemplateSaveReqVO reqVO) {
        TemplateDO entity = BeanUtils.toBean(reqVO, TemplateDO.class);
        entity.setStatus("draft");
        entity.setCreatedAt(java.time.LocalDateTime.now());
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        templateMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateTemplate(TemplateSaveReqVO reqVO) {
        TemplateDO entity = BeanUtils.toBean(reqVO, TemplateDO.class);
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        templateMapper.updateById(entity);
    }

    @Override
    public void deleteTemplate(Long id) {
        templateMapper.deleteById(id);
    }

    @Override
    public TemplateRespVO getTemplate(Long id) {
        return BeanUtils.toBean(templateMapper.selectById(id), TemplateRespVO.class);
    }

    @Override
    public PageResult<TemplateRespVO> getTemplatePage(TemplatePageReqVO pageReqVO) {
        PageResult<TemplateDO> pageResult = templateMapper.selectPage(pageReqVO);
        PageResult<TemplateRespVO> result = new PageResult<>();
        result.setList(BeanUtils.toBean(pageResult.getList(), TemplateRespVO.class));
        result.setTotal(pageResult.getTotal());
        return result;
    }

    @Override
    @Transactional
    public Long publishVersion(Long templateId, String fileUrl, String changelog) {
        TemplateVersionDO latestVersion = versionMapper.selectLatestByTemplateId(templateId);
        int nextVersion = (latestVersion != null) ? latestVersion.getVersion() + 1 : 1;

        TemplateVersionDO versionDO = new TemplateVersionDO();
        versionDO.setTemplateId(templateId);
        versionDO.setVersion(nextVersion);
        versionDO.setFileUrl(fileUrl);
        versionDO.setChangelog(changelog);
        versionDO.setCreatedAt(java.time.LocalDateTime.now());
        versionMapper.insert(versionDO);

        // Update template status to published with correct timestamp
        TemplateDO template = templateMapper.selectById(templateId);
        if (template != null) {
            template.setStatus("published");
            template.setUpdatedAt(java.time.LocalDateTime.now());
            templateMapper.updateById(template);
        }

        return versionDO.getId();
    }

    @Override
    public List<TemplateVersionRespVO> getVersions(Long templateId) {
        List<TemplateVersionDO> versions = versionMapper.selectList(
            new LambdaQueryWrapper<TemplateVersionDO>()
                .eq(TemplateVersionDO::getTemplateId, templateId)
                .orderByDesc(TemplateVersionDO::getVersion));
        return BeanUtils.toBean(versions, TemplateVersionRespVO.class);
    }

    @Override
    public SyncResultVO syncTemplates(SyncRequestVO reqVO) {
        Map<Long, Integer> localVersionMap = new HashMap<>();
        if (reqVO.getLocalVersions() != null) {
            for (SyncRequestVO.LocalVersion lv : reqVO.getLocalVersions()) {
                localVersionMap.put(lv.getTemplateId(), lv.getVersion());
            }
        }

        // Get all published templates
        List<TemplateDO> publishedTemplates = templateMapper.selectList(
            new LambdaQueryWrapper<TemplateDO>()
                .eq(TemplateDO::getStatus, "published"));

        List<SyncResultVO.UpdatedTemplate> updated = new ArrayList<>();
        Set<Long> serverTemplateIds = new HashSet<>();

        for (TemplateDO template : publishedTemplates) {
            serverTemplateIds.add(template.getId());
            TemplateVersionDO serverVersion = versionMapper.selectLatestByTemplateId(template.getId());
            if (serverVersion == null) continue;

            Integer localVer = localVersionMap.get(template.getId());
            if (localVer == null || serverVersion.getVersion() > localVer) {
                SyncResultVO.UpdatedTemplate ut = new SyncResultVO.UpdatedTemplate();
                ut.setTemplateId(template.getId());
                ut.setVersion(serverVersion.getVersion());
                ut.setFileUrl(serverVersion.getFileUrl());
                ut.setChangelog(serverVersion.getChangelog());
                updated.add(ut);
            }
        }

        List<Long> deleted = localVersionMap.keySet().stream()
            .filter(id -> !serverTemplateIds.contains(id))
            .collect(Collectors.toList());

        SyncResultVO result = new SyncResultVO();
        result.setUpdated(updated);
        result.setDeleted(deleted);
        return result;
    }

    @Override
    public String getDownloadUrl(Long templateVersionId) {
        TemplateVersionDO version = versionMapper.selectById(templateVersionId);
        return version != null ? version.getFileUrl() : null;
    }
}
