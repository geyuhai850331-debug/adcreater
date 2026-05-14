package cn.iocoder.yudao.adcreater.module.template.controller.app;

import cn.iocoder.yudao.adcreater.module.template.controller.app.vo.*;
import cn.iocoder.yudao.adcreater.module.template.service.TemplateService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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
