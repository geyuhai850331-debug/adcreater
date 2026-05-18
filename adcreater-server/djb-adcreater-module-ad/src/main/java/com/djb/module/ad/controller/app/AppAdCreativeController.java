package com.djb.module.ad.controller.app;

import com.djb.framework.common.exception.ServiceException;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import com.djb.module.ad.controller.vo.BatchImageGenReqVO;
import com.djb.module.ad.controller.vo.ImageGenReqVO;
import com.djb.module.ad.controller.vo.MediaRespVO;
import com.djb.module.ai.adapter.AiRequest;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.service.ModelOrchestrationService;
import com.djb.module.ai.service.PromptService;
import com.djb.module.billing.dal.dataobject.PointsTransactionDO;
import com.djb.module.billing.service.BillingService;
import com.djb.module.billing.service.UsageService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.djb.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 广告创意生成")
@RestController
@RequestMapping("/ad")
@Validated
@Slf4j
public class AppAdCreativeController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @Resource
    private UsageService usageService;

    @PostMapping("/image/generate")
    @Operation(summary = "生成单张广告图片")
    public CommonResult<MediaRespVO> generateImage(@Valid @RequestBody ImageGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        PointsTransactionDO tx = billingService.preConsume(userId, 10, null);
        try {
            Map<String, String> vars = new HashMap<>();
            vars.put("product_name", reqVO.getProductName());
            vars.put("style", reqVO.getStyle() != null ? reqVO.getStyle() : "modern minimalist");
            vars.put("background", reqVO.getBackground() != null ? reqVO.getBackground() : "clean studio white");
            vars.put("target_market", reqVO.getTargetMarket());
            vars.put("selling_points", reqVO.getSellingPoints());
            vars.put("width", String.valueOf(reqVO.getWidth()));
            vars.put("height", String.valueOf(reqVO.getHeight()));

            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(promptService.resolvePrompt("image", vars));
            aiReq.setWidth(reqVO.getWidth());
            aiReq.setHeight(reqVO.getHeight());
            aiReq.setStyle(reqVO.getStyle());

            AiResult result = orchestrationService.generateImage(aiReq, null);
            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                throw new ServiceException(500, result.getErrorMessage());
            }

            billingService.confirmConsume(tx.getId());
            usageService.record(userId, "image", "image-gen", 10,
                    result.getInputTokens() != null ? result.getInputTokens() : 0,
                    result.getOutputTokens() != null ? result.getOutputTokens() : 0);

            MediaRespVO respVO = new MediaRespVO();
            respVO.setUrl(result.getUrl());
            respVO.setImageUrl(result.getUrl());
            return success(respVO);
        } catch (RuntimeException ex) {
            billingService.rollbackConsume(tx.getId());
            throw ex;
        }
    }

    @PostMapping("/image/batch")
    @Operation(summary = "批量生成多尺寸广告图片")
    public CommonResult<List<Map<String, Object>>> batchGenerate(@Valid @RequestBody BatchImageGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        List<Map<String, Object>> results = new java.util.ArrayList<>();
        for (BatchImageGenReqVO.SizeSpec size : reqVO.getSizes()) {
            PointsTransactionDO tx = billingService.preConsume(userId, 10, null);
            try {
                Map<String, String> vars = Map.of(
                        "product_name", reqVO.getProductName(),
                        "width", String.valueOf(size.getWidth()),
                        "height", String.valueOf(size.getHeight()),
                        "platform", size.getPlatform() != null ? size.getPlatform() : "generic"
                );
                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(promptService.resolvePrompt("image", vars));
                aiReq.setWidth(size.getWidth());
                aiReq.setHeight(size.getHeight());
                AiResult result = orchestrationService.generateImage(aiReq, null);
                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    results.add(Map.of(
                            "platform", size.getPlatform(),
                            "error", result.getErrorMessage()
                    ));
                    continue;
                }
                billingService.confirmConsume(tx.getId());
                results.add(Map.of(
                        "platform", size.getPlatform(),
                        "width", size.getWidth(),
                        "height", size.getHeight(),
                        "url", result.getUrl()
                ));
            } catch (RuntimeException ex) {
                billingService.rollbackConsume(tx.getId());
                throw ex;
            }
        }
        return success(results);
    }
}
