package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 分镜重新生成请求 VO
 */
@Data
public class StoryboardRegenReqVO {

    @NotBlank(message = "场景描述不能为空")
    private String description;

    @NotNull(message = "场景序号不能为空")
    private Integer index;
}
