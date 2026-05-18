package com.djb.module.delivery.controller.app;

import com.djb.framework.common.exception.ServiceException;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import com.djb.module.ai.adapter.AiRequest;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.service.ModelOrchestrationService;
import com.djb.module.ai.service.PromptService;
import com.djb.module.billing.dal.dataobject.PointsTransactionDO;
import com.djb.module.billing.service.BillingService;
import com.djb.module.delivery.controller.app.vo.AppDeliveryMediaRespVO;
import com.djb.module.delivery.controller.vo.DigitalHumanReqVO;
import com.djb.module.delivery.controller.vo.PlatformExportReqVO;
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

@Tag(name = "用户 App - 广告投放")
@RestController
@RequestMapping("/delivery")
@Validated
@Slf4j
public class AppDeliveryController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @PostMapping("/avatar/generate")
    @Operation(summary = "生成数字人视频")
    public CommonResult<AppDeliveryMediaRespVO> generateAvatar(@Valid @RequestBody DigitalHumanReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        PointsTransactionDO tx = billingService.preConsume(userId, 100, null);
        try {
            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(promptService.resolvePrompt("digital_human", Map.of(
                    "script", reqVO.getScript(),
                    "avatar", reqVO.getAvatarId(),
                    "duration", String.valueOf(reqVO.getDuration())
            )));
            aiReq.setDuration(reqVO.getDuration());
            AiResult result = orchestrationService.generateDigitalHuman(aiReq, null);
            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                throw new ServiceException(500, result.getErrorMessage());
            }
            billingService.confirmConsume(tx.getId());
            AppDeliveryMediaRespVO respVO = new AppDeliveryMediaRespVO();
            respVO.setUrl(result.getUrl());
            respVO.setVideoUrl(result.getUrl());
            return success(respVO);
        } catch (RuntimeException ex) {
            billingService.rollbackConsume(tx.getId());
            throw ex;
        }
    }

    @PostMapping("/export-sizes")
    @Operation(summary = "导出平台尺寸")
    public CommonResult<Map<String, Object>> exportSizes(@Valid @RequestBody PlatformExportReqVO reqVO) {
        Map<String, List<int[]>> sizeMap = Map.of(
                "amazon", List.of(new int[]{1500, 1500}, new int[]{2000, 2000}, new int[]{1600, 1600}),
                "facebook", List.of(new int[]{1200, 628}, new int[]{1080, 1080}, new int[]{1080, 1920}),
                "shopee", List.of(new int[]{1024, 1024}, new int[]{800, 800}),
                "tiktok", List.of(new int[]{1080, 1920}, new int[]{720, 1280}),
                "google", List.of(new int[]{1200, 628}, new int[]{300, 250}, new int[]{728, 90}, new int[]{160, 600}, new int[]{300, 600})
        );

        Map<String, Map<String, String>> urls = new HashMap<>();
        for (String platform : reqVO.getPlatforms()) {
            Map<String, String> platformUrls = new HashMap<>();
            for (int[] size : sizeMap.getOrDefault(platform, List.of(new int[]{1200, 628}))) {
                platformUrls.put(size[0] + "x" + size[1], reqVO.getBaseImageUrl());
            }
            urls.put(platform, platformUrls);
        }
        return success(Map.of("urls", urls));
    }
}
