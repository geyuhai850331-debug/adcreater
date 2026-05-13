package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import lombok.Data;

@Data
public class PromptTemplateRespVO {
    private Long id;
    private String name;
    private Long modelConfigId;
    private String templateContent;
    private String variables;
    private String category;
    private Boolean isEnabled;
}
