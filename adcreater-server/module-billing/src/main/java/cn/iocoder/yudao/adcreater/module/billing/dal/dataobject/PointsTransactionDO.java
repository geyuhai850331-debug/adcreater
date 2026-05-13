package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("points_transaction")
public class PointsTransactionDO {
    @TableId
    private Long id;
    private Long userId;
    private String type;
    private Integer amount;
    private Integer balanceAfter;
    private String status;
    private Long bizId;
    private String remark;
    private LocalDateTime createdAt;
}
