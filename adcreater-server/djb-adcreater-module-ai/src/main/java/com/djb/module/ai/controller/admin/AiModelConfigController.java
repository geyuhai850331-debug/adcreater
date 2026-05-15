package com.djb.module.ai.controller.admin;

import com.djb.module.ai.controller.admin.vo.*;
import com.djb.module.ai.service.AiModelConfigService;
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

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - AI 模型配置
 *
 * @author adcreater
 */
@Tag(name = "管理后台 - AI 模型配置")
@RestController
@RequestMapping("/ai/model-config")
@Validated
@Slf4j
public class AiModelConfigController {

    @Resource
    private AiModelConfigService service;

    @PostMapping("/create")
    @Operation(summary = "创建 AI 模型配置")
    @PreAuthorize("@ss.hasPermission('ai:model-config:create')")
    public CommonResult<Long> create(@Valid @RequestBody AiModelConfigSaveReqVO reqVO) {
        return success(service.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 AI 模型配置")
    @PreAuthorize("@ss.hasPermission('ai:model-config:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody AiModelConfigSaveReqVO reqVO) {
        service.update(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新 AI 模型配置启用状态")
    @PreAuthorize("@ss.hasPermission('ai:model-config:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody AiModelConfigStatusReqVO reqVO) {
        service.updateStatus(reqVO.getId(), reqVO.getIsEnabled());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 AI 模型配置")
    @PreAuthorize("@ss.hasPermission('ai:model-config:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        service.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "查询 AI 模型配置详情")
    @PreAuthorize("@ss.hasPermission('ai:model-config:query')")
    public CommonResult<AiModelConfigRespVO> get(@RequestParam("id") Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询 AI 模型配置")
    @PreAuthorize("@ss.hasPermission('ai:model-config:query')")
    public CommonResult<PageResult<AiModelConfigRespVO>> getPage(@Valid AiModelConfigPageReqVO pageReqVO) {
        return success(service.getPage(pageReqVO));
    }

    @PostMapping("/test-connection")
    @Operation(summary = "测试 AI 模型连接")
    @PreAuthorize("@ss.hasPermission('ai:model-config:update')")
    public CommonResult<Boolean> testConnection(@Valid @RequestBody AiModelConfigTestReqVO reqVO) {
        return success(service.testConnection(reqVO));
    }
}
