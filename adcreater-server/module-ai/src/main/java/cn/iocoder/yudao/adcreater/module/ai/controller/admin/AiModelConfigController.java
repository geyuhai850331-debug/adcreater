package cn.iocoder.yudao.adcreater.module.ai.controller.admin;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.service.AiModelConfigService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin-api/ai/model-config")
public class AiModelConfigController {

    @Resource
    private AiModelConfigService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('ai:model-config:create')")
    public CommonResult<Long> create(@Valid @RequestBody AiModelConfigSaveReqVO reqVO) {
        return CommonResult.success(service.create(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('ai:model-config:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody AiModelConfigSaveReqVO reqVO) {
        service.update(reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('ai:model-config:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        service.delete(id);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('ai:model-config:query')")
    public CommonResult<AiModelConfigRespVO> get(@RequestParam("id") Long id) {
        return CommonResult.success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('ai:model-config:query')")
    public CommonResult<PageResult<AiModelConfigRespVO>> getPage(@Valid AiModelConfigPageReqVO pageReqVO) {
        return CommonResult.success(service.getPage(pageReqVO));
    }

    @PostMapping("/test-connection")
    @PreAuthorize("@ss.hasPermission('ai:model-config:update')")
    public CommonResult<Boolean> testConnection(@RequestParam("id") Long id) {
        return CommonResult.success(service.testConnection(id));
    }
}
