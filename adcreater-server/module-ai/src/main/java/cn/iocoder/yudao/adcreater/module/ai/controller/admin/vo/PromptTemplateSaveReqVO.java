package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class PromptTemplateSaveReqVO {
    private Long id;
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 64, message = "模板名称最长 64 字符")
    private String name;
    private Long modelConfigId;
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
    @Size(max = 2000, message = "变量定义最长 2000 字符")
    private String variables;
    @NotBlank(message = "分类不能为空")
    @Size(max = 32, message = "分类最长 32 字符")
    private String category;
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
}
