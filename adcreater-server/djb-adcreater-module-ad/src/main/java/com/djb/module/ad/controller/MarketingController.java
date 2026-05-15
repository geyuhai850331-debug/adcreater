package com.djb.module.ad.controller;

import com.djb.module.ad.controller.vo.*;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.service.ModelOrchestrationService;
import com.djb.module.ai.service.PromptService;
import com.djb.module.billing.dal.dataobject.PointsTransactionDO;
import com.djb.module.billing.service.BillingService;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - 广告营销策划
 *
 * @author adcreater
 */
@Tag(name = "用户端 - 广告营销策划")
@RestController
@RequestMapping("/api/ad/marketing")
@Validated
@Slf4j
public class MarketingController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
        "\\{[^{}]*\"risk_level\"[^{}]*\"culture_notes\"[^{}]*\"core_strategy\"[^{}]*\"example_ad_copy\"[^{}]*\\}",
        Pattern.DOTALL);

    @Operation(summary = "分析营销策略")
    @PostMapping("/analyze")
    public CommonResult<MarketingAnalyzeRespVO> analyze(@Valid @RequestBody MarketingAnalyzeReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();

        Map<String, String> vars = Map.of(
            "product_name", reqVO.getProductName(),
            "product_description", reqVO.getProductDescription(),
            "chinese_ad_copy", reqVO.getChineseAdCopy(),
            "target_market", reqVO.getTargetMarket()
        );

        String prompt = promptService.resolvePrompt("marketing", vars);

        int cost = 5;
        PointsTransactionDO tx = billingService.preConsume(userId, cost, null);

        try {
            AiResult result = orchestrationService.translate(
                prompt, "auto", "en", reqVO.getTargetMarket());

            if (!result.isSuccess()) {
                throw new RuntimeException("AI 分析失败: " + result.getErrorMessage());
            }

            String aiContent = result.getRevisedPrompt();
            MarketingAnalyzeRespVO resp = parseMarketingResponse(aiContent);

            billingService.confirmConsume(tx.getId());
            return success(resp);
        } catch (Exception e) {
            billingService.rollbackConsume(tx.getId());
            log.error("Marketing analyze failed for user {}", userId, e);
            throw new RuntimeException("营销分析失败: " + e.getMessage(), e);
        }
    }

    private MarketingAnalyzeRespVO parseMarketingResponse(String aiContent) {
        MarketingAnalyzeRespVO resp = new MarketingAnalyzeRespVO();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> json = OBJECT_MAPPER.readValue(aiContent, Map.class);
            resp.setRiskLevel(String.valueOf(json.getOrDefault("risk_level", "warning")));
            resp.setCultureNotes(String.valueOf(json.getOrDefault("culture_notes", "")));
            resp.setCoreStrategy(String.valueOf(json.getOrDefault("core_strategy", "")));
            resp.setExampleAdCopy(String.valueOf(json.getOrDefault("example_ad_copy", "")));
        } catch (Exception e) {
            // Fallback: regex extract JSON block
            Matcher matcher = JSON_BLOCK_PATTERN.matcher(aiContent);
            if (matcher.find()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> json = OBJECT_MAPPER.readValue(matcher.group(), Map.class);
                    resp.setRiskLevel(String.valueOf(json.getOrDefault("risk_level", "warning")));
                    resp.setCultureNotes(String.valueOf(json.getOrDefault("culture_notes", "")));
                    resp.setCoreStrategy(String.valueOf(json.getOrDefault("core_strategy", "")));
                    resp.setExampleAdCopy(String.valueOf(json.getOrDefault("example_ad_copy", "")));
                    return resp;
                } catch (Exception ignored) {}
            }
            // Final fallback: return raw text as cultureNotes
            resp.setRiskLevel("warning");
            resp.setCultureNotes("AI 返回格式异常，原始响应：\n" + aiContent);
            resp.setCoreStrategy("请重新生成分析");
            resp.setExampleAdCopy("");
        }
        return resp;
    }
}
