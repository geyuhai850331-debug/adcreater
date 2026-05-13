package cn.iocoder.yudao.adcreater.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {
    COPY("copy", "文案翻译"),
    IMAGE("image", "图片生成"),
    VIDEO("video", "视频生成"),
    DIGITAL_HUMAN("digital_human", "数字人"),
    PLATFORM_EXPORT("platform_export", "平台导出");

    private final String code;
    private final String desc;
}
