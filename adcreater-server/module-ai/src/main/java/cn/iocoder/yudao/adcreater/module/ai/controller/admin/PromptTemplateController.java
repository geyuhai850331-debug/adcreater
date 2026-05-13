package cn.iocoder.yudao.adcreater.module.ai.controller.admin;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin-api/ai/prompt-template")
public class PromptTemplateController {

    @Resource
    private PromptService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('ai:prompt:create')")
    public CommonResult<Long> create(@Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        return CommonResult.success(service.create(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('ai:prompt:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        service.update(reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('ai:prompt:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        service.delete(id);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('ai:prompt:query')")
    public CommonResult<PromptTemplateRespVO> get(@RequestParam("id") Long id) {
        return CommonResult.success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('ai:prompt:query')")
    public CommonResult<PageResult<PromptTemplateRespVO>> getPage(@Valid PromptTemplatePageReqVO pageReqVO) {
        return CommonResult.success(service.getPage(pageReqVO));
    }
}
