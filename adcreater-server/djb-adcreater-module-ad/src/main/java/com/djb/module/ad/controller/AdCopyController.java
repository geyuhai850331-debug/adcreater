package com.djb.module.ad.controller;

import com.djb.module.ad.controller.vo.*;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.service.ModelOrchestrationService;
import com.djb.module.ai.service.PromptService;
import com.djb.module.billing.service.BillingService;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - 广告文案翻译
 *
 * @author adcreater
 */
@Tag(name = "用户端 - 广告文案翻译")
@RestController
@RequestMapping("/api/ad/copy")
@Validated
@Slf4j
public class AdCopyController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @Operation(summary = "翻译广告文案")
    @PostMapping("/translate")
    public CommonResult<TranslateRespVO> translate(@Valid @RequestBody TranslateReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();

        Map<String, String> vars = Map.of(
            "product_name", reqVO.getProductTitle(),
            "description", reqVO.getProductDescription() != null ? reqVO.getProductDescription() : "",
            "selling_points", String.join(", ", reqVO.getSellingPoints()),
            "target_market", reqVO.getTargetMarket(),
            "target_lang", reqVO.getTargetLang()
        );

        String prompt = promptService.resolvePrompt("copy", vars);

        int cost = 5;
        billingService.preConsume(userId, cost, null);

        try {
            AiResult result = orchestrationService.translate(
                reqVO.getProductTitle(), reqVO.getSourceLang(),
                reqVO.getTargetLang(), reqVO.getTargetMarket());

            TranslateRespVO resp = new TranslateRespVO();
            resp.setTranslatedTitle(reqVO.getProductTitle()); // placeholder translation
            resp.setLocalizedCopy(prompt);
            return success(resp);
        } catch (Exception e) {
            throw new RuntimeException("Translation failed: " + e.getMessage(), e);
        }
    }
}
