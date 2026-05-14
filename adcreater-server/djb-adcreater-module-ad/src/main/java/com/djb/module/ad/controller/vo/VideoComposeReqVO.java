package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 视频合成请求 VO
 */
@Data
public class VideoComposeReqVO {

    @NotEmpty(message = "分镜列表不能为空")
    private List<StoryboardItem> storyboards;

    /**
     * 场景序号 → 网格图 URL 的映射
     */
    private Map<String, String> gridImages;

    @NotNull(message = "视频时长不能为空")
    private Integer duration;   // 15/30/45/60/90 秒

    private VideoSettings settings;

    // ── 内嵌 DTO ──

    @Data
    public static class StoryboardItem {
        private Integer order;
        private String description;
        private List<KeyFrameItem> keyFrames;
    }

    @Data
    public static class KeyFrameItem {
        private Integer order;
        private String prompt;
    }

    @Data
    public static class VideoSettings {
        private String ttsLang;           // zh-CN/en-US/ja-JP/ko-KR
        private String ttsVoice;          // zh-CN-XiaoxiaoNeural 等
        private Integer ttsRate;          // -50 ~ 50
        private Integer ttsPitch;         // -50 ~ 50
        private String bgmSelection;      // none/upbeat/warm/electronic/nature/jpop/beat/classical
        private Integer bgmVolume;        // 0 ~ 100
        private Integer narrationVolume;  // 0 ~ 100
    }
}
