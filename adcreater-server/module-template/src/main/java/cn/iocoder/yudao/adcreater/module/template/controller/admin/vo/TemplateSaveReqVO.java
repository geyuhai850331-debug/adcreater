package cn.iocoder.yudao.adcreater.module.template.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class TemplateSaveReqVO {
    private Long id;

    @NotBlank(message = "模板名称不能为空")
    private String name;

    private String description;

    private String category;

    private String thumbnailUrl;

    private String status;
}
