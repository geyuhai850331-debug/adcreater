package cn.iocoder.yudao.adcreater.module.ai.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.djb.framework.mybatis.core.dataobject.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 模型配置 DO
 *
 * @author adcreater
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_model_config")
public class AiModelConfigDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 模型名称 */
    private String modelName;

    /** 适配器全限定类名 */
    private String adapterClass;

    /** API Key (AES 加密存储) */
    private String apiKey;

    /** API 端点 URL */
    private String endpointUrl;

    /** 是否启用 */
    private Boolean isEnabled;

    /** 优先级 (降级用, 越小越优先) */
    private Integer priority;

    /** 排序 */
    private Integer sortOrder;

    /** 扩展配置 JSON (计费单价等) */
    private String extraConfig;
}
