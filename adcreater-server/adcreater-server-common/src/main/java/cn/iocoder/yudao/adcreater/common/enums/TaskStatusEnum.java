package cn.iocoder.yudao.adcreater.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    DONE("done", "已完成"),
    FAILED("failed", "失败");

    private final String code;
    private final String desc;
}
