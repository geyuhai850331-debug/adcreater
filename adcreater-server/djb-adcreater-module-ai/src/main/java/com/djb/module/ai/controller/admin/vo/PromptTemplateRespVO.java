package com.djb.module.ai.controller.admin.vo;

import lombok.Data;

@Data
public class PromptTemplateRespVO {
    private Long id;
    private String name;
    private String modelName;
    private String systemPrompt;
    private String templateContent;
    private String variables;
    private String category;
    private Boolean isEnabled;
}
