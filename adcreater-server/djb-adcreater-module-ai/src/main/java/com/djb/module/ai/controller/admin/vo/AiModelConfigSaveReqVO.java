package com.djb.module.ai.controller.admin.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class AiModelConfigSaveReqVO {
    private Long id;
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 64, message = "模型名称最长 64 字符")
    private String modelName;
    @NotBlank(message = "适配器类名不能为空")
    @Size(max = 255, message = "适配器类名最长 255 字符")
    private String adapterClass;
    @NotBlank(message = "API Key 不能为空")
    @Size(max = 512, message = "API Key 最长 512 字符")
    private String apiKey;
    @Size(max = 255, message = "端点 URL 最长 255 字符")
    private String endpointUrl;
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
    private Integer priority;
    private Integer sortOrder;
    private String extraConfig;
}
