package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiModelConfigPageReqVO extends PageParam {
    private String modelName;
    private Boolean isEnabled;
}
