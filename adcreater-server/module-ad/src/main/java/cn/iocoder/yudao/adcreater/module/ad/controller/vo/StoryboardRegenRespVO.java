package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import java.util.List;

/**
 * 分镜重新生成响应 VO
 */
@Data
public class StoryboardRegenRespVO {

    private List<KeyFrameItem> keyFrames;

    @Data
    public static class KeyFrameItem {
        private Integer order;
        private String prompt;
    }
}
