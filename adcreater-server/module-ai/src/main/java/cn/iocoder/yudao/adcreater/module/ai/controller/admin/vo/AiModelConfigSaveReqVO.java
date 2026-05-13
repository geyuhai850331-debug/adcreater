package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AiModelConfigSaveReqVO {
    private Long id;
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    @NotBlank(message = "适配器类名不能为空")
    private String adapterClass;
    @NotBlank(message = "API Key 不能为空")
    private String apiKey;
    private String endpointUrl;
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
    private Integer priority;
    private Integer sortOrder;
    private String extraConfig;
}
