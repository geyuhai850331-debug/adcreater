package cn.iocoder.yudao.adcreater.module.template.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.djb.framework.mybatis.core.dataobject.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板版本 DO
 *
 * @author adcreater
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_template_version")
public class TemplateVersionDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 关联模板 ID */
    private Long templateId;

    /** 版本号 */
    private Integer version;

    /** 模板文件 URL */
    private String fileUrl;

    /** 变更日志 */
    private String changelog;
}
