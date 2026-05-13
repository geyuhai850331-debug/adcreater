package cn.iocoder.yudao.adcreater.module.ad.controller;

import cn.iocoder.yudao.adcreater.module.ad.controller.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiRequest;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiResult;
import cn.iocoder.yudao.adcreater.module.ai.service.ModelOrchestrationService;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.adcreater.module.billing.service.UsageService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/api/ad/creative")
public class AdCreativeController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @Resource
    private UsageService usageService;

    @Resource
    private ExecutorService executorService;

    @PostMapping(value = "/image/text-to-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter textToImage(@Valid @RequestBody ImageGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                Map<String, String> vars = new HashMap<>();
                vars.put("product_name", reqVO.getProductName());
                vars.put("style", reqVO.getStyle() != null ? reqVO.getStyle() : "modern minimalist");
                vars.put("background", reqVO.getBackground() != null ? reqVO.getBackground() : "clean studio white");
                vars.put("target_market", reqVO.getTargetMarket());
                vars.put("selling_points", reqVO.getSellingPoints());
                vars.put("width", String.valueOf(reqVO.getWidth()));
                vars.put("height", String.valueOf(reqVO.getHeight()));
                String prompt = promptService.resolvePrompt("image", vars);

                int cost = 10;
                tx = billingService.preConsume(userId, cost, null);

                sendSse(emitter, "progress", Map.of("progress", 10, "status", "processing"));

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setWidth(reqVO.getWidth());
                aiReq.setHeight(reqVO.getHeight());
                aiReq.setStyle(reqVO.getStyle());

                AiResult result = orchestrationService.generateImage(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("progress", 50, "message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
                    usageService.record(userId, "image", "image-gen", cost,
                            result.getInputTokens() != null ? result.getInputTokens() : 0,
                            result.getOutputTokens() != null ? result.getOutputTokens() : 0);
                    sendSse(emitter, "done", Map.of("imageUrl", result.getUrl()));
                }
                emitter.complete();
            } catch (Exception e) {
                if (tx != null) {
                    try { billingService.rollbackConsume(tx.getId()); } catch (Exception ignored) {}
                }
                sendSse(emitter, "error", Map.of("message", e.getMessage()));
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/image/batch")
    public CommonResult<List<Map<String, Object>>> batchGenerate(@Valid @RequestBody BatchImageGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        List<Map<String, Object>> results = new ArrayList<>();

        for (BatchImageGenReqVO.SizeSpec size : reqVO.getSizes()) {
            Map<String, String> vars = Map.of(
                "product_name", reqVO.getProductName(),
                "width", String.valueOf(size.getWidth()),
                "height", String.valueOf(size.getHeight()),
                "platform", size.getPlatform() != null ? size.getPlatform() : "generic"
            );
            String prompt = promptService.resolvePrompt("image", vars);

            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(prompt);
            aiReq.setWidth(size.getWidth());
            aiReq.setHeight(size.getHeight());

            int cost = 10;
            PointsTransactionDO tx = billingService.preConsume(userId, cost, null);
            try {
                AiResult result = orchestrationService.generateImage(aiReq, null);
                if (result.isSuccess()) {
                    billingService.confirmConsume(tx.getId());
                    Map<String, Object> item = new HashMap<>();
                    item.put("platform", size.getPlatform());
                    item.put("width", size.getWidth());
                    item.put("height", size.getHeight());
                    item.put("url", result.getUrl());
                    results.add(item);
                } else {
                    billingService.rollbackConsume(tx.getId());
                    Map<String, Object> item = new HashMap<>();
                    item.put("platform", size.getPlatform());
                    item.put("error", result.getErrorMessage());
                    results.add(item);
                }
            } catch (Exception e) {
                try { billingService.rollbackConsume(tx.getId()); } catch (Exception ignored) {}
            }
        }

        return CommonResult.success(results);
    }

    @PostMapping(value = "/video", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateVideo(@Valid @RequestBody VideoGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                int cost = 50;
                tx = billingService.preConsume(userId, cost, null);

                Map<String, String> vars = Map.of(
                    "product_name", reqVO.getProductName(),
                    "duration", String.valueOf(reqVO.getDuration()),
                    "style", reqVO.getStyle() != null ? reqVO.getStyle() : "professional"
                );
                String prompt = promptService.resolvePrompt("video", vars);

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setDuration(reqVO.getDuration());
                aiReq.setStyle(reqVO.getStyle());

                AiResult result = orchestrationService.generateVideo(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
                    usageService.record(userId, "video", "video-gen", cost, 0, 0);
                    sendSse(emitter, "done", Map.of("videoUrl", result.getUrl()));
                }
                emitter.complete();
            } catch (Exception e) {
                if (tx != null) {
                    try { billingService.rollbackConsume(tx.getId()); } catch (Exception ignored) {}
                }
                sendSse(emitter, "error", Map.of("message", e.getMessage()));
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private void sendSse(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event()
                .name(event)
                .data(data, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
