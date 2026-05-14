package com.djb.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.djb.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户点数账户 DO
 *
 * @author adcreater
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_user_points_account")
public class UserPointsAccountDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 用户 ID (关联 system_users.id) */
    private Long userId;

    /** 当前余额 */
    private Integer balance;

    /** 累计充值 */
    private Integer totalEarned;

    /** 累计消费 */
    private Integer totalSpent;
}
