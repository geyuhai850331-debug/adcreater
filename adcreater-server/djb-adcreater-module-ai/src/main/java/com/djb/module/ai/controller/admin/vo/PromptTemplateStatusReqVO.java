package com.djb.module.ai.controller.admin.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromptTemplateStatusReqVO {

    @NotNull(message = "ID 不能为空")
    private Long id;

    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
}
