package com.djb.common.enums;

import lombok.Getter;

@Getter
public enum PromptCategoryEnum {
    COPY("copy", "文案"),
    IMAGE("image", "图片"),
    VIDEO("video", "视频"),
    VIDEO_STORYBOARD("video_storyboard", "视频-分镜策划"),
    VIDEO_KEYFRAME("video_keyframe", "视频-关键帧"),
    VIDEO_KEYFRAME_IMAGE("video_keyframe_image", "视频-关键帧图片"),
    VIDEO_KEYFRAME_GRID("video_keyframe_grid", "视频-关键帧网格"),
    DIGITAL_HUMAN("digital_human", "数字人"),
    MARKETING("marketing", "营销策划"),
    MARKETING_COPY("marketing_copy", "营销文案生成");

    private final String code;
    private final String desc;

    PromptCategoryEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
