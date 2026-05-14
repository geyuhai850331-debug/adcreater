package com.djb.module.template.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板响应 VO
 *
 * @author adcreater
 */
@Data
public class TemplateRespVO {
    private Long id;

    /** 模板名称 */
    private String name;

    /** 模板描述 */
    private String description;

    /** 分类: image/video */
    private String category;

    /** 缩略图 URL */
    private String thumbnailUrl;

    /** 状态: draft/published */
    private String status;

    /** 创建时间 (继承自 TenantBaseDO) */
    private LocalDateTime createTime;

    /** 更新时间 (继承自 TenantBaseDO) */
    private LocalDateTime updateTime;
}
