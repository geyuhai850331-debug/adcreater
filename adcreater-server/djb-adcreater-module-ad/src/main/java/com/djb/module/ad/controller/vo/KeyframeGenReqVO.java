package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 关键帧生成请求 VO
 */
@Data
public class KeyframeGenReqVO {

    @NotBlank(message = "关键帧 Prompt 不能为空")
    private String prompt;

    @NotNull(message = "场景序号不能为空")
    private Integer sceneIndex;

    @NotNull(message = "帧序号不能为空")
    private Integer frameIndex;
}
