package cn.iocoder.yudao.adcreater.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromptCategoryEnum {
    COPY("copy", "文案"),
    IMAGE("image", "图片"),
    VIDEO("video", "视频"),
    DIGITAL_HUMAN("digital_human", "数字人");

    private final String code;
    private final String desc;
}
