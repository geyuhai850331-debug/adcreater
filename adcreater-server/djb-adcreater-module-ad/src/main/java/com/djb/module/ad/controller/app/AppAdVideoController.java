package com.djb.module.ad.controller.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.djb.framework.common.exception.ServiceException;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import com.djb.module.ad.controller.vo.KeyframeGenReqVO;
import com.djb.module.ad.controller.vo.KeyframeGridReqVO;
import com.djb.module.ad.controller.vo.MediaRespVO;
import com.djb.module.ad.controller.vo.StoryboardGenReqVO;
import com.djb.module.ad.controller.vo.StoryboardRegenReqVO;
import com.djb.module.ad.controller.vo.StoryboardRegenRespVO;
import com.djb.module.ad.controller.vo.StoryboardRespVO;
import com.djb.module.ad.controller.vo.VideoComposeReqVO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.djb.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 广告视频制作")
@RestController
@RequestMapping("/ad/video")
@Validated
@Slf4j
public class AppAdVideoController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @Resource
    private UsageService usageService;

    @PostMapping("/storyboard/generate")
    @Operation(summary = "生成分镜脚本")
    public CommonResult<StoryboardRespVO> generateStoryboard(@Valid @RequestBody StoryboardGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        PointsTransactionDO tx = billingService.preConsume(userId, 5, null);
        try {
            Map<String, String> vars = new HashMap<>();
            vars.put("product_description", reqVO.getProductDescription());
            vars.put("category", reqVO.getCategory() != null ? reqVO.getCategory() : "electronics");
            vars.put("target_platform", reqVO.getTargetPlatform() != null ? reqVO.getTargetPlatform() : "amazon");
            vars.put("target_lang", reqVO.getTargetLang() != null ? reqVO.getTargetLang() : "en-US");
            vars.put("template_id", reqVO.getTemplateId() != null ? reqVO.getTemplateId() : "vt1");
            String prompt = promptService.resolvePrompt("video_storyboard", vars);
            AiResult result = orchestrationService.translate(prompt, "auto",
                    reqVO.getTargetLang() != null ? reqVO.getTargetLang() : "en-US",
                    reqVO.getTargetPlatform());
            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                throw new ServiceException(500, result.getErrorMessage());
            }

            StoryboardRespVO respVO = parseStoryboardResponse(
                    result.getUrl() != null ? result.getUrl() : prompt,
                    reqVO.getProductDescription());
            billingService.confirmConsume(tx.getId());
            usageService.record(userId, "video_storyboard", "storyboard-gen", 5, 0, 0);
            return success(respVO);
        } catch (RuntimeException ex) {
            billingService.rollbackConsume(tx.getId());
            throw ex;
        }
    }

    @PostMapping("/storyboard/regenerate")
    @Operation(summary = "重新生成单个分镜")
    public CommonResult<StoryboardRegenRespVO> regenerateStoryboard(@Valid @RequestBody StoryboardRegenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        PointsTransactionDO tx = billingService.preConsume(userId, 3, null);
        try {
            AiResult result = orchestrationService.translate(
                    "重新生成场景 " + reqVO.getIndex() + " 的4个关键帧描述: " + reqVO.getDescription(),
                    "auto", "zh-CN", "generic");
            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                throw new ServiceException(500, result.getErrorMessage());
            }

            String[] labels = {"远景", "中景", "特写", "细节"};
            List<StoryboardRegenRespVO.KeyFrameItem> frames = new ArrayList<>();
            for (int i = 0; i < labels.length; i++) {
                StoryboardRegenRespVO.KeyFrameItem item = new StoryboardRegenRespVO.KeyFrameItem();
                item.setOrder(i);
                item.setPrompt(reqVO.getDescription() + " - " + labels[i] + " (再生版)");
                frames.add(item);
            }

            StoryboardRegenRespVO respVO = new StoryboardRegenRespVO();
            respVO.setKeyFrames(frames);
            billingService.confirmConsume(tx.getId());
            usageService.record(userId, "video_storyboard", "storyboard-regen", 3, 0, 0);
            return success(respVO);
        } catch (RuntimeException ex) {
            billingService.rollbackConsume(tx.getId());
            throw ex;
        }
    }

    @PostMapping("/keyframe/generate")
    @Operation(summary = "生成单个关键帧")
    public CommonResult<MediaRespVO> generateKeyframe(@Valid @RequestBody KeyframeGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        PointsTransactionDO tx = billingService.preConsume(userId, 10, null);
        try {
            Map<String, String> vars = Map.of(
                    "prompt", reqVO.getPrompt(),
                    "scene", String.valueOf(reqVO.getSceneIndex()),
                    "frame", String.valueOf(reqVO.getFrameIndex())
            );
            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(promptService.resolvePrompt("video_keyframe_image", vars));
            aiReq.setWidth(640);
            aiReq.setHeight(360);
            AiResult result = orchestrationService.generateImage(aiReq, null);
            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                throw new ServiceException(500, result.getErrorMessage());
            }

            billingService.confirmConsume(tx.getId());
            usageService.record(userId, "video_keyframe", "keyframe-gen", 10, 0, 0);
            MediaRespVO respVO = new MediaRespVO();
            respVO.setUrl(result.getUrl());
            respVO.setImageUrl(result.getUrl());
            return success(respVO);
        } catch (RuntimeException ex) {
            billingService.rollbackConsume(tx.getId());
            throw ex;
        }
    }

    @PostMapping("/keyframe/grid")
    @Operation(summary = "生成关键帧宫格")
    public CommonResult<MediaRespVO> generateKeyframeGrid(@Valid @RequestBody KeyframeGridReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        PointsTransactionDO tx = billingService.preConsume(userId, 15, null);
        try {
            Map<String, String> vars = Map.of(
                    "prompts", String.join(" | ", reqVO.getPrompts()),
                    "scene", String.valueOf(reqVO.getSceneIndex())
            );
            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(promptService.resolvePrompt("video_keyframe_grid", vars));
            aiReq.setWidth(1280);
            aiReq.setHeight(960);
            AiResult result = orchestrationService.generateImage(aiReq, null);
            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                throw new ServiceException(500, result.getErrorMessage());
            }

            billingService.confirmConsume(tx.getId());
            usageService.record(userId, "video_keyframe", "keyframe-grid", 15, 0, 0);
            MediaRespVO respVO = new MediaRespVO();
            respVO.setUrl(result.getUrl());
            respVO.setImageUrl(result.getUrl());
            return success(respVO);
        } catch (RuntimeException ex) {
            billingService.rollbackConsume(tx.getId());
            throw ex;
        }
    }

    @PostMapping("/generate")
    @Operation(summary = "合成最终视频")
    public CommonResult<MediaRespVO> composeVideo(@Valid @RequestBody VideoComposeReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        PointsTransactionDO tx = billingService.preConsume(userId, 50, null);
        try {
            Map<String, String> vars = new HashMap<>();
            vars.put("storyboard_count", String.valueOf(reqVO.getStoryboards().size()));
            vars.put("duration", String.valueOf(reqVO.getDuration()));
            VideoComposeReqVO.VideoSettings settings = reqVO.getSettings();
            vars.put("tts_lang", settings != null && settings.getTtsLang() != null ? settings.getTtsLang() : "zh-CN");
            vars.put("tts_voice", settings != null && settings.getTtsVoice() != null ? settings.getTtsVoice() : "zh-CN-XiaoxiaoNeural");
            vars.put("bgm", settings != null && settings.getBgmSelection() != null ? settings.getBgmSelection() : "none");

            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(promptService.resolvePrompt("video", vars));
            aiReq.setDuration(reqVO.getDuration());
            aiReq.getExtraParams().put("storyboards", reqVO.getStoryboards());
            aiReq.getExtraParams().put("gridImages", reqVO.getGridImages());
            if (settings != null) {
                aiReq.getExtraParams().put("settings", settings);
            }

            AiResult result = orchestrationService.generateVideo(aiReq, null);
            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                throw new ServiceException(500, result.getErrorMessage());
            }

            billingService.confirmConsume(tx.getId());
            usageService.record(userId, "video", "video-compose", 50, 0, 0);
            MediaRespVO respVO = new MediaRespVO();
            respVO.setUrl(result.getUrl());
            respVO.setImageUrl(result.getUrl());
            return success(respVO);
        } catch (RuntimeException ex) {
            billingService.rollbackConsume(tx.getId());
            throw ex;
        }
    }

    private StoryboardRespVO parseStoryboardResponse(String aiResponse, String productDescription) {
        StoryboardRespVO resp = new StoryboardRespVO();
        List<StoryboardRespVO.StoryboardItem> items = new ArrayList<>();

        try {
            JSONObject json = JSON.parseObject(aiResponse);
            if (json.containsKey("storyboards")) {
                JSONArray arr = json.getJSONArray("storyboards");
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject sb = arr.getJSONObject(i);
                    StoryboardRespVO.StoryboardItem item = new StoryboardRespVO.StoryboardItem();
                    item.setOrder(sb.getInteger("order"));
                    item.setDescription(sb.getString("description"));
                    JSONArray kfArr = sb.getJSONArray("keyFrames");
                    if (kfArr != null) {
                        List<StoryboardRespVO.KeyFrameItem> frames = new ArrayList<>();
                        for (int j = 0; j < kfArr.size(); j++) {
                            JSONObject kf = kfArr.getJSONObject(j);
                            StoryboardRespVO.KeyFrameItem frame = new StoryboardRespVO.KeyFrameItem();
                            frame.setOrder(kf.getInteger("order"));
                            frame.setPrompt(kf.getString("prompt"));
                            frames.add(frame);
                        }
                        item.setKeyFrames(frames);
                    }
                    items.add(item);
                }
                resp.setStoryboards(items);
                return resp;
            }
        } catch (Exception ignored) {
            // fall back to deterministic local parsing
        }

        String[] sentences = productDescription.split("[，,。.\\n、]");
        List<String> meaningful = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                meaningful.add(trimmed);
            }
        }

        int sceneCount = Math.min(meaningful.isEmpty() ? 3 : meaningful.size(), 5);
        String[] keyframeLabels = {"开场远景展示产品全貌", "中景展示核心功能与使用场景", "特写强调产品细节与材质", "品牌标识与行动号召"};
        for (int i = 0; i < sceneCount; i++) {
            StoryboardRespVO.StoryboardItem item = new StoryboardRespVO.StoryboardItem();
            item.setOrder(i);
            item.setDescription(meaningful.size() > i ? meaningful.get(i) : "场景 " + (i + 1) + "：展示产品特点与优势");
            List<StoryboardRespVO.KeyFrameItem> frames = new ArrayList<>();
            for (int j = 0; j < keyframeLabels.length; j++) {
                StoryboardRespVO.KeyFrameItem frame = new StoryboardRespVO.KeyFrameItem();
                frame.setOrder(j);
                frame.setPrompt(item.getDescription() + " - " + keyframeLabels[j]);
                frames.add(frame);
            }
            item.setKeyFrames(frames);
            items.add(item);
        }
        resp.setStoryboards(items);
        return resp;
    }
}
