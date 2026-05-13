package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import lombok.Data;

@Data
public class AiModelConfigRespVO {
    private Long id;
    private String modelName;
    private String adapterClass;
    private String apiKey;
    private String endpointUrl;
    private Boolean isEnabled;
    private Integer priority;
    private Integer sortOrder;
    private String extraConfig;
}
