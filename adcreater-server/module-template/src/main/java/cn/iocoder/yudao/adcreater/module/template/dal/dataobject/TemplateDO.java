package cn.iocoder.yudao.adcreater.module.template.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.djb.framework.mybatis.core.dataobject.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告模板 DO
 *
 * @author adcreater
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_template")
public class TemplateDO extends TenantBaseDO {

    @TableId
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
}
