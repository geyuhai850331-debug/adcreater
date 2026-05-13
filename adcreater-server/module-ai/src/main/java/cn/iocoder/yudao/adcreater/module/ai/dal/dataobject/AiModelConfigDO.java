package cn.iocoder.yudao.adcreater.module.ai.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model_config")
public class AiModelConfigDO extends BaseDO {
    @TableId
    private Long id;
    private String modelName;
    private String adapterClass;
    private String apiKey;
    private String endpointUrl;
    private Boolean isEnabled;
    private Integer priority;
    private Integer sortOrder;
    private String extraConfig;
}
