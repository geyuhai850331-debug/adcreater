package com.djb.module.template.controller.app;

import com.djb.module.template.controller.app.vo.*;
import com.djb.module.template.service.TemplateService;
import com.djb.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.util.List;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - 模板同步
 *
 * @author adcreater
 */
@Tag(name = "用户端 - 模板同步")
@RestController
@RequestMapping("/api/template/client")
@Validated
@Slf4j
public class TemplateClientController {

    @Resource
    private TemplateService templateService;

    @GetMapping("/list")
    @Operation(summary = "查询已发布模板列表")
    public CommonResult<List<AppTemplateSimpleRespVO>> getList() {
        return success(templateService.getPublishedTemplateList());
    }

    @PostMapping("/sync")
    @Operation(summary = "同步模板版本")
    public CommonResult<SyncResultVO> sync(@RequestBody SyncRequestVO reqVO) {
        return success(templateService.syncTemplates(reqVO));
    }

    @GetMapping("/download/{versionId}")
    @Operation(summary = "下载模板文件")
    public CommonResult<String> download(@PathVariable("versionId") Long versionId) {
        String downloadUrl = templateService.getDownloadUrl(versionId);
        if (downloadUrl == null) {
            return CommonResult.error(404, "版本不存在");
        }
        return success(downloadUrl);
    }
}
