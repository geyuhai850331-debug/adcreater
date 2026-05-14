package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.djb.framework.mybatis.core.dataobject.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用量记录 DO
 *
 * @author adcreater
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_usage_record")
public class UsageRecordDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 任务类型 */
    private String taskType;

    /** 使用的模型 */
    private String modelUsed;

    /** 消耗点数 */
    private Integer pointsConsumed;

    /** 输入 Token 数 */
    private Integer inputTokens;

    /** 输出 Token 数 */
    private Integer outputTokens;
}
