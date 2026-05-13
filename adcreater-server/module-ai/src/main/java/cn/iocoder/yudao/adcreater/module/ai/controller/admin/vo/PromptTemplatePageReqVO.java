package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PromptTemplatePageReqVO extends PageParam {
    private String name;
    private String category;
    private Boolean isEnabled;
}
