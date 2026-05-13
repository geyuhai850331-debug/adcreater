package cn.iocoder.yudao.adcreater.module.template.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TemplateVersionRespVO {
    private Long id;
    private Long templateId;
    private Integer version;
    private String fileUrl;
    private String changelog;
    private LocalDateTime createdAt;
}
