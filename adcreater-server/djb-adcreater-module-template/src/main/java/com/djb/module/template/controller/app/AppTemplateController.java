package com.djb.module.template.controller.app;

import com.djb.framework.common.pojo.CommonResult;
import com.djb.module.template.controller.app.vo.AppTemplateSimpleRespVO;
import com.djb.module.template.controller.app.vo.SyncRequestVO;
import com.djb.module.template.controller.app.vo.SyncResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户 App - 模板")
@RestController
@RequestMapping("/template")
@Validated
@Slf4j
public class AppTemplateController {

    @Resource
    private TemplateClientController templateClientController;

    @GetMapping("/list")
    @Operation(summary = "查询已发布模板列表")
    public CommonResult<List<AppTemplateSimpleRespVO>> getList() {
        return templateClientController.getList();
    }

    @PostMapping("/sync")
    @Operation(summary = "同步模板版本")
    public CommonResult<SyncResultVO> sync(@Valid @RequestBody(required = false) SyncRequestVO reqVO) {
        return templateClientController.sync(reqVO == null ? new SyncRequestVO() : reqVO);
    }
}
