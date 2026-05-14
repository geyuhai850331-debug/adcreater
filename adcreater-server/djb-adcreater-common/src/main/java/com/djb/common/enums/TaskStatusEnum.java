package com.djb.common.enums;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    DONE("done", "已完成"),
    FAILED("failed", "失败");

    private final String code;
    private final String desc;

    TaskStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
