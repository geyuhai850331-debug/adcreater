package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import java.util.List;

/**
 * 分镜策划响应 VO
 */
@Data
public class StoryboardRespVO {

    private List<StoryboardItem> storyboards;

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
}
