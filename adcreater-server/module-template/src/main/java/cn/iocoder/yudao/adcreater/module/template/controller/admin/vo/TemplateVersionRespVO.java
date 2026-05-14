package cn.iocoder.yudao.adcreater.module.template.controller.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板版本响应 VO
 *
 * @author adcreater
 */
@Data
public class TemplateVersionRespVO {
    private Long id;

    /** 关联模板 ID */
    private Long templateId;

    /** 版本号 */
    private Integer version;

    /** 模板文件 URL */
    private String fileUrl;

    /** 变更日志 */
    private String changelog;

    /** 创建时间 (继承自 TenantBaseDO) */
    private LocalDateTime createTime;
}
