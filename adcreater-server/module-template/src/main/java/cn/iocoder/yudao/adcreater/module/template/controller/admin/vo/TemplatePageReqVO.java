package cn.iocoder.yudao.adcreater.module.template.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplatePageReqVO extends PageParam {
    private String name;
    private String category;
    private String status;
}
