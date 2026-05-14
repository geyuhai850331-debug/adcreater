package cn.iocoder.yudao.adcreater.module.delivery.controller;

import cn.iocoder.yudao.adcreater.module.delivery.controller.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiRequest;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiResult;
import cn.iocoder.yudao.adcreater.module.ai.service.ModelOrchestrationService;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - 广告投放
 *
 * @author adcreater
 */
@Tag(name = "用户端 - 广告投放")
@RestController
@RequestMapping("/api/delivery")
@Validated
@Slf4j
public class DeliveryController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @Resource
    private ExecutorService executorService;

    private static final Map<String, int[][]> PLATFORM_SIZES = Map.of(
        "amazon", new int[][]{{1500, 1500}, {1200, 628}, {800, 800}},
        "facebook", new int[][]{{1200, 628}, {1080, 1080}, {1200, 444}},
        "shopee", new int[][]{{800, 800}, {1200, 1200}},
        "tiktok", new int[][]{{1080, 1920}, {720, 1280}},
        "google", new int[][]{{1200, 628}, {1200, 1200}, {970, 250}}
    );

    @Operation(summary = "生成数字人视频 (SSE 流式)")
    @PostMapping(value = "/digital-human", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateDigitalHuman(@Valid @RequestBody DigitalHumanReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                int cost = 100;
                tx = billingService.preConsume(userId, cost, null);

                String prompt = promptService.resolvePrompt("digital_human", Map.of(
                    "script", reqVO.getScript(),
                    "avatar", reqVO.getAvatarId(),
                    "duration", String.valueOf(reqVO.getDuration())
                ));

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setDuration(reqVO.getDuration());

                AiResult result = orchestrationService.generateDigitalHuman(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
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

    @Operation(summary = "多平台批量导出")
    @PostMapping("/platform/export")
    public CommonResult<List<Map<String, Object>>> platformExport(
            @Valid @RequestBody PlatformExportReqVO reqVO) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (String platform : reqVO.getPlatforms()) {
            int[][] sizes = PLATFORM_SIZES.getOrDefault(platform, new int[][]{{1200, 628}});

            List<Map<String, Object>> platformSizes = new ArrayList<>();
            for (int[] size : sizes) {
                Map<String, Object> sizeInfo = new HashMap<>();
                sizeInfo.put("width", size[0]);
                sizeInfo.put("height", size[1]);
                sizeInfo.put("status", "ready");
                sizeInfo.put("baseImageUrl", reqVO.getBaseImageUrl());
                sizeInfo.put("copyText", reqVO.getCopyText());
                platformSizes.add(sizeInfo);
            }

            Map<String, Object> platformResult = new HashMap<>();
            platformResult.put("platform", platform);
            platformResult.put("sizes", platformSizes);
            results.add(platformResult);
        }

        return success(results);
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
