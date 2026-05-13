package cn.iocoder.yudao.adcreater.module.template.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TemplateRespVO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String thumbnailUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
