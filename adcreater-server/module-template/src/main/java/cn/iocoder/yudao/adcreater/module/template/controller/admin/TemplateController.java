package cn.iocoder.yudao.adcreater.module.template.controller.admin;

import cn.iocoder.yudao.adcreater.module.template.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.template.service.TemplateService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin-api/template")
public class TemplateController {

    @Resource
    private TemplateService templateService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('template:create')")
    public CommonResult<Long> create(@Valid @RequestBody TemplateSaveReqVO reqVO) {
        return CommonResult.success(templateService.createTemplate(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('template:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody TemplateSaveReqVO reqVO) {
        templateService.updateTemplate(reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('template:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        templateService.deleteTemplate(id);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('template:query')")
    public CommonResult<TemplateRespVO> get(@RequestParam("id") Long id) {
        return CommonResult.success(templateService.getTemplate(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('template:query')")
    public CommonResult<PageResult<TemplateRespVO>> getPage(@Valid TemplatePageReqVO pageReqVO) {
        return CommonResult.success(templateService.getTemplatePage(pageReqVO));
    }

    @PostMapping("/publish-version")
    @PreAuthorize("@ss.hasPermission('template:update')")
    public CommonResult<Long> publishVersion(@RequestParam("templateId") Long templateId,
                                              @RequestParam("fileUrl") String fileUrl,
                                              @RequestParam("changelog") String changelog) {
        return CommonResult.success(templateService.publishVersion(templateId, fileUrl, changelog));
    }

    @GetMapping("/versions")
    @PreAuthorize("@ss.hasPermission('template:query')")
    public CommonResult<List<TemplateVersionRespVO>> getVersions(@RequestParam("templateId") Long templateId) {
        return CommonResult.success(templateService.getVersions(templateId));
    }
}
