package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_points_account")
public class UserPointsAccountDO {
    @TableId
    private Long id;
    private Long userId;
    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
}
