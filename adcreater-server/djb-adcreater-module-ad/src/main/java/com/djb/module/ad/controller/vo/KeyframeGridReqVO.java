package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 关键帧网格生成请求 VO
 */
@Data
public class KeyframeGridReqVO {

    @NotEmpty(message = "关键帧 Prompt 列表不能为空")
    private List<String> prompts;

    @NotNull(message = "场景序号不能为空")
    private Integer sceneIndex;
}
