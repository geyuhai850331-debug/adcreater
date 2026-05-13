package cn.iocoder.yudao.adcreater.module.template.controller.app;

import cn.iocoder.yudao.adcreater.module.template.controller.app.vo.*;
import cn.iocoder.yudao.adcreater.module.template.service.TemplateService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/template/client")
public class TemplateClientController {

    @Resource
    private TemplateService templateService;

    @PostMapping("/sync")
    public CommonResult<SyncResultVO> sync(@RequestBody SyncRequestVO reqVO) {
        return CommonResult.success(templateService.syncTemplates(reqVO));
    }

    @GetMapping("/download/{versionId}")
    public CommonResult<String> download(@PathVariable("versionId") Long versionId) {
        String downloadUrl = templateService.getDownloadUrl(versionId);
        if (downloadUrl == null) {
            return CommonResult.error(404, "版本不存在");
        }
        return CommonResult.success(downloadUrl);
    }
}
