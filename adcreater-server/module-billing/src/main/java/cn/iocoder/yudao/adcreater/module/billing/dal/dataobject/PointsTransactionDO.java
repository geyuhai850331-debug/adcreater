package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.djb.framework.mybatis.core.dataobject.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点数流水 DO (不可变, 只 INSERT)
 *
 * @author adcreater
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_points_transaction")
public class PointsTransactionDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 类型: earn/consume/refund */
    private String type;

    /** 金额 */
    private Integer amount;

    /** 操作后余额 */
    private Integer balanceAfter;

    /** 状态: pending/confirmed/rolled_back */
    private String status;

    /** 关联业务 ID */
    private Long bizId;

    /** 备注 */
    private String remark;
}
