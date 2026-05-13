package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("usage_record")
public class UsageRecordDO {
    @TableId
    private Long id;
    private Long userId;
    private String taskType;
    private String modelUsed;
    private Integer pointsConsumed;
    private Integer inputTokens;
    private Integer outputTokens;
    private LocalDateTime createdAt;
}
