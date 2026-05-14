package cn.iocoder.yudao.adcreater.module.ai.controller.admin;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - Prompt 模板
 *
 * @author adcreater
 */
@Tag(name = "管理后台 - Prompt 模板")
@RestController
@RequestMapping("/admin-api/ai/prompt-template")
@Validated
@Slf4j
public class PromptTemplateController {

    @Resource
    private PromptService service;

    @PostMapping("/create")
    @Operation(summary = "创建 Prompt 模板")
    @PreAuthorize("@ss.hasPermission('ai:prompt:create')")
    public CommonResult<Long> create(@Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        return success(service.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Prompt 模板")
    @PreAuthorize("@ss.hasPermission('ai:prompt:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        service.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Prompt 模板")
    @PreAuthorize("@ss.hasPermission('ai:prompt:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        service.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "查询 Prompt 模板详情")
    @PreAuthorize("@ss.hasPermission('ai:prompt:query')")
    public CommonResult<PromptTemplateRespVO> get(@RequestParam("id") Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询 Prompt 模板")
    @PreAuthorize("@ss.hasPermission('ai:prompt:query')")
    public CommonResult<PageResult<PromptTemplateRespVO>> getPage(@Valid PromptTemplatePageReqVO pageReqVO) {
        return success(service.getPage(pageReqVO));
    }
}
