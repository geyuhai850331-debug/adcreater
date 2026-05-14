package com.djb.module.template.controller.admin;

import com.djb.module.template.controller.admin.vo.*;
import com.djb.module.template.service.TemplateService;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 广告模板
 *
 * @author adcreater
 */
@Tag(name = "管理后台 - 广告模板")
@RestController
@RequestMapping("/admin-api/template")
@Validated
@Slf4j
public class TemplateController {

    @Resource
    private TemplateService templateService;

    @PostMapping("/create")
    @Operation(summary = "创建广告模板")
    @PreAuthorize("@ss.hasPermission('template:create')")
    public CommonResult<Long> create(@Valid @RequestBody TemplateSaveReqVO reqVO) {
        return success(templateService.createTemplate(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新广告模板")
    @PreAuthorize("@ss.hasPermission('template:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody TemplateSaveReqVO reqVO) {
        templateService.updateTemplate(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除广告模板")
    @PreAuthorize("@ss.hasPermission('template:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        templateService.deleteTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "查询广告模板详情")
    @PreAuthorize("@ss.hasPermission('template:query')")
    public CommonResult<TemplateRespVO> get(@RequestParam("id") Long id) {
        return success(templateService.getTemplate(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询广告模板")
    @PreAuthorize("@ss.hasPermission('template:query')")
    public CommonResult<PageResult<TemplateRespVO>> getPage(@Valid TemplatePageReqVO pageReqVO) {
        return success(templateService.getTemplatePage(pageReqVO));
    }

    @PostMapping("/publish-version")
    @Operation(summary = "发布模板版本")
    @PreAuthorize("@ss.hasPermission('template:update')")
    public CommonResult<Long> publishVersion(@RequestParam("templateId") Long templateId,
                                              @RequestParam("fileUrl") String fileUrl,
                                              @RequestParam("changelog") String changelog) {
        return success(templateService.publishVersion(templateId, fileUrl, changelog));
    }

    @GetMapping("/versions")
    @Operation(summary = "查询模板版本列表")
    @PreAuthorize("@ss.hasPermission('template:query')")
    public CommonResult<List<TemplateVersionRespVO>> getVersions(@RequestParam("templateId") Long templateId) {
        return success(templateService.getVersions(templateId));
    }
}
