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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
 * 用户端 - 广告视频制作 (3 步子步骤流程)
 *
 * <pre>
 * Step 3-0: POST /api/ad/video/storyboard/generate      → 分镜策划 (AI 文本生成)
 * Step 3-0: POST /api/ad/video/storyboard/regenerate     → 重新生成单个分镜
 * Step 3-1: POST /api/ad/video/keyframe/generate         → 单帧关键帧图片生成
 * Step 3-1: POST /api/ad/video/keyframe/grid             → 4 宫格关键帧网格生成
 * Step 3-2: POST /api/ad/video/generate                  → 视频合成 (SSE 流式)
 * </pre>
 *
 * @author adcreater
 */
@Tag(name = "用户端 - 广告视频制作")
@RestController
@RequestMapping("/api/ad/video")
@Validated
@Slf4j
public class AdVideoController {

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

    // ════════════════════════════════════════════════════════════════
    // Step 3-0: 分镜策划
    // ════════════════════════════════════════════════════════════════

    @Operation(summary = "分镜策划：根据商品描述生成分镜脚本")
    @PostMapping("/storyboard/generate")
    public CommonResult<StoryboardRespVO> generateStoryboard(@Valid @RequestBody StoryboardGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        int cost = 5;

        PointsTransactionDO tx = billingService.preConsume(userId, cost, null);
        try {
            // 1. 构建 Prompt
            Map<String, String> vars = new HashMap<>();
            vars.put("product_description", reqVO.getProductDescription());
            vars.put("category", reqVO.getCategory() != null ? reqVO.getCategory() : "electronics");
            vars.put("target_platform", reqVO.getTargetPlatform() != null ? reqVO.getTargetPlatform() : "amazon");
            vars.put("target_lang", reqVO.getTargetLang() != null ? reqVO.getTargetLang() : "en-US");
            vars.put("template_id", reqVO.getTemplateId() != null ? reqVO.getTemplateId() : "vt1");
            String prompt = promptService.resolvePrompt("video_storyboard", vars);

            // 2. 调用 AI 生成结构化分镜数据
            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(prompt);
            AiResult result = orchestrationService.translate(prompt, "auto",
                    reqVO.getTargetLang() != null ? reqVO.getTargetLang() : "en-US",
                    reqVO.getTargetPlatform());

            if (!result.isSuccess()) {
                billingService.rollbackConsume(tx.getId());
                return error(-1, result.getErrorMessage());
            }

            // 3. 解析 AI 响应为结构化分镜
            StoryboardRespVO resp = parseStoryboardResponse(
                    result.getUrl() != null ? result.getUrl() : prompt,
                    reqVO.getProductDescription());
            billingService.confirmConsume(tx.getId());
            usageService.record(userId, "video_storyboard", "storyboard-gen", cost, 0, 0);
            return success(resp);

        } catch (Exception e) {
            try { billingService.rollbackConsume(tx.getId()); } catch (Exception ignored) {}
            return error(-1, "分镜生成失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重新生成单个分镜的关键帧描述")
    @PostMapping("/storyboard/regenerate")
    public CommonResult<StoryboardRegenRespVO> regenerateStoryboard(@Valid @RequestBody StoryboardRegenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        int cost = 3;

        PointsTransactionDO tx = billingService.preConsume(userId, cost, null);
        try {
            Map<String, String> vars = Map.of(
                    "description", reqVO.getDescription(),
                    "scene_index", String.valueOf(reqVO.getIndex())
            );
            String prompt = promptService.resolvePrompt("video_keyframe", vars);

            // AI 生成新的关键帧 prompt 列表
            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt("重新生成场景 " + reqVO.getIndex() + " 的4个关键帧描述: " + reqVO.getDescription());
            AiResult result = orchestrationService.translate(
                    aiReq.getPrompt(), "auto", "zh-CN", "generic");

            StoryboardRegenRespVO resp = new StoryboardRegenRespVO();
            if (result.isSuccess()) {
                List<StoryboardRegenRespVO.KeyFrameItem> frames = new ArrayList<>();
                String[] labels = {"远景", "中景", "特写", "细节"};
                for (int i = 0; i < 4; i++) {
                    StoryboardRegenRespVO.KeyFrameItem item = new StoryboardRegenRespVO.KeyFrameItem();
                    item.setOrder(i);
                    item.setPrompt(reqVO.getDescription() + " - " + labels[i] + " (再生版)");
                    frames.add(item);
                }
                resp.setKeyFrames(frames);
                billingService.confirmConsume(tx.getId());
            } else {
                billingService.rollbackConsume(tx.getId());
                return error(-1, result.getErrorMessage());
            }

            usageService.record(userId, "video_storyboard", "storyboard-regen", cost, 0, 0);
            return success(resp);

        } catch (Exception e) {
            try { billingService.rollbackConsume(tx.getId()); } catch (Exception ignored) {}
            return error(-1, "分镜重新生成失败: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Step 3-1: 关键帧生成
    // ════════════════════════════════════════════════════════════════

    @Operation(summary = "生成单个关键帧图片 (SSE)")
    @PostMapping(value = "/keyframe/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateKeyframe(@Valid @RequestBody KeyframeGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                int cost = 10;
                tx = billingService.preConsume(userId, cost, null);

                Map<String, String> vars = Map.of(
                        "prompt", reqVO.getPrompt(),
                        "scene", String.valueOf(reqVO.getSceneIndex()),
                        "frame", String.valueOf(reqVO.getFrameIndex())
                );
                String prompt = promptService.resolvePrompt("video_keyframe_image", vars);

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setWidth(640);
                aiReq.setHeight(360);

                sendSse(emitter, "progress", Map.of("progress", 20, "status", "generating"));

                AiResult result = orchestrationService.generateImage(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("progress", 60, "message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
                    usageService.record(userId, "video_keyframe", "keyframe-gen", cost, 0, 0);
                    sendSse(emitter, "done", Map.of("url", result.getUrl(), "imageUrl", result.getUrl()));
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

    @Operation(summary = "生成 4 宫格关键帧网格图 (SSE)")
    @PostMapping(value = "/keyframe/grid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateKeyframeGrid(@Valid @RequestBody KeyframeGridReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                int cost = 15;
                tx = billingService.preConsume(userId, cost, null);

                // 将 4 帧 prompt 合并为网格 prompt
                StringBuilder gridPrompt = new StringBuilder("4-panel storyboard grid: ");
                for (int i = 0; i < reqVO.getPrompts().size(); i++) {
                    gridPrompt.append("Panel ").append(i + 1).append(": ")
                            .append(reqVO.getPrompts().get(i)).append(". ");
                }

                Map<String, String> vars = Map.of(
                        "prompts", String.join(" | ", reqVO.getPrompts()),
                        "scene", String.valueOf(reqVO.getSceneIndex())
                );
                String prompt = promptService.resolvePrompt("video_keyframe_grid", vars);

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setWidth(1280);
                aiReq.setHeight(960);

                sendSse(emitter, "progress", Map.of("progress", 20, "status", "generating_grid"));

                AiResult result = orchestrationService.generateImage(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("progress", 60, "message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
                    usageService.record(userId, "video_keyframe", "keyframe-grid", cost, 0, 0);
                    sendSse(emitter, "done", Map.of("url", result.getUrl(), "imageUrl", result.getUrl()));
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

    // ════════════════════════════════════════════════════════════════
    // Step 3-2: 视频合成
    // ════════════════════════════════════════════════════════════════

    @Operation(summary = "合成最终广告视频 (SSE 流式)")
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter composeVideo(@Valid @RequestBody VideoComposeReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(15 * 60 * 1000L); // 视频最长 15 分钟

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                int cost = 50;
                tx = billingService.preConsume(userId, cost, null);

                // 1. 构建视频合成 Prompt
                Map<String, String> vars = new HashMap<>();
                vars.put("storyboard_count", String.valueOf(reqVO.getStoryboards().size()));
                vars.put("duration", String.valueOf(reqVO.getDuration()));

                VideoComposeReqVO.VideoSettings settings = reqVO.getSettings();
                if (settings != null) {
                    vars.put("tts_lang", settings.getTtsLang() != null ? settings.getTtsLang() : "zh-CN");
                    vars.put("tts_voice", settings.getTtsVoice() != null ? settings.getTtsVoice() : "zh-CN-XiaoxiaoNeural");
                    vars.put("bgm", settings.getBgmSelection() != null ? settings.getBgmSelection() : "none");
                } else {
                    vars.put("tts_lang", "zh-CN");
                    vars.put("tts_voice", "zh-CN-XiaoxiaoNeural");
                    vars.put("bgm", "none");
                }

                String prompt = promptService.resolvePrompt("video", vars);

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setDuration(reqVO.getDuration());
                // 传递分镜结构作为附加参数
                aiReq.getExtraParams().put("storyboards", reqVO.getStoryboards());
                aiReq.getExtraParams().put("gridImages", reqVO.getGridImages());
                if (settings != null) {
                    aiReq.getExtraParams().put("settings", settings);
                }

                // 2. 进度回调
                sendSse(emitter, "progress", Map.of("progress", 10, "status", "init"));

                AiResult result = orchestrationService.generateVideo(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("progress", 50, "message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
                    usageService.record(userId, "video", "video-compose", cost, 0, 0);
                    sendSse(emitter, "done", Map.of("url", result.getUrl(), "videoUrl", result.getUrl()));
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

    // ════════════════════════════════════════════════════════════════
    // 辅助方法
    // ════════════════════════════════════════════════════════════════

    /**
     * 将 AI 文本响应解析为结构化分镜数据。
     * 生产环境中 AI 应返回 JSON，此处提供基于文本分割的 fallback 逻辑。
     */
    private StoryboardRespVO parseStoryboardResponse(String aiResponse, String productDescription) {
        StoryboardRespVO resp = new StoryboardRespVO();
        List<StoryboardRespVO.StoryboardItem> items = new ArrayList<>();

        // 尝试作为 JSON 解析
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
                            StoryboardRespVO.KeyFrameItem kfItem = new StoryboardRespVO.KeyFrameItem();
                            kfItem.setOrder(kf.getInteger("order"));
                            kfItem.setPrompt(kf.getString("prompt"));
                            frames.add(kfItem);
                        }
                        item.setKeyFrames(frames);
                    }
                    items.add(item);
                }
                resp.setStoryboards(items);
                return resp;
            }
        } catch (Exception ignored) {
            // JSON 解析失败，使用文本分割 fallback
        }

        // Fallback: 按标点分割产品描述生成分镜
        String[] sentences = productDescription.split("[，,。.\\n、]");
        List<String> meaningful = new ArrayList<>();
        for (String s : sentences) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                meaningful.add(trimmed);
            }
        }

        int sceneCount = Math.min(meaningful.size() > 0 ? meaningful.size() : 3, 5);
        String[] keyframeLabels = {"开场远景展示产品全貌", "中景展示核心功能与使用场景",
                "特写强调产品细节与材质", "品牌标识与行动号召"};

        for (int i = 0; i < sceneCount; i++) {
            StoryboardRespVO.StoryboardItem item = new StoryboardRespVO.StoryboardItem();
            item.setOrder(i);
            item.setDescription(meaningful.size() > i
                    ? meaningful.get(i)
                    : "场景 " + (i + 1) + "：展示产品特点与优势");

            List<StoryboardRespVO.KeyFrameItem> frames = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                StoryboardRespVO.KeyFrameItem kf = new StoryboardRespVO.KeyFrameItem();
                kf.setOrder(j);
                kf.setPrompt(item.getDescription() + " - " + keyframeLabels[j]);
                frames.add(kf);
            }
            item.setKeyFrames(frames);
            items.add(item);
        }

        resp.setStoryboards(items);
        return resp;
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
