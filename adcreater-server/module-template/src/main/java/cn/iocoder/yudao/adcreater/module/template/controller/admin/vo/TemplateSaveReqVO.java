package cn.iocoder.yudao.adcreater.module.template.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TemplateSaveReqVO {
    private Long id;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称最长 128 字符")
    private String name;

    @Size(max = 512, message = "模板描述最长 512 字符")
    private String description;

    @Size(max = 32, message = "分类最长 32 字符")
    private String category;

    @Size(max = 512, message = "缩略图 URL 最长 512 字符")
    private String thumbnailUrl;

    @Size(max = 16, message = "状态最长 16 字符")
    private String status;
}
