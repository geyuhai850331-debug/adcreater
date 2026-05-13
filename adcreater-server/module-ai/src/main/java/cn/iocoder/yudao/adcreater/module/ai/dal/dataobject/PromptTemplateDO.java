package cn.iocoder.yudao.adcreater.module.ai.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prompt_template")
public class PromptTemplateDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private Long modelConfigId;
    private String templateContent;
    private String variables;
    private String category;
    private Boolean isEnabled;
}
