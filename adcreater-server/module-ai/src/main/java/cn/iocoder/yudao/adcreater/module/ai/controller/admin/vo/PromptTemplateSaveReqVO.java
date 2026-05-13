package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PromptTemplateSaveReqVO {
    private Long id;
    @NotBlank(message = "模板名称不能为空")
    private String name;
    private Long modelConfigId;
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
    private String variables;
    @NotBlank(message = "分类不能为空")
    private String category;
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
}
