package com.djb.module.ai.controller.admin.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class PromptTemplateSaveReqVO {
    private Long id;
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 64, message = "模板名称最长 64 字符")
    private String name;

    @Size(max = 128, message = "模型最长 128 字符")
    private String modelName;

    @NotBlank(message = "系统提示不能为空")
    private String systemPrompt;

    private String templateContent;
    @Size(max = 2000, message = "变量定义最长 2000 字符")
    private String variables;
    @NotBlank(message = "分类不能为空")
    @Size(max = 32, message = "分类最长 32 字符")
    private String category;
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
}
