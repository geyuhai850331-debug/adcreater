package com.djb.module.ad.controller.app;

import com.djb.framework.common.pojo.CommonResult;
import com.djb.module.ad.controller.AdCopyController;
import com.djb.module.ad.controller.vo.TranslateReqVO;
import com.djb.module.ad.controller.vo.TranslateRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户 App - 广告文案翻译")
@RestController
@RequestMapping("/ad/copy")
@Validated
@Slf4j
public class AppAdCopyController {

    @Resource
    private AdCopyController adCopyController;

    @PostMapping("/translate")
    @Operation(summary = "翻译广告文案")
    public CommonResult<TranslateRespVO> translate(@Valid @RequestBody TranslateReqVO reqVO) {
        return adCopyController.translate(reqVO);
    }
}
