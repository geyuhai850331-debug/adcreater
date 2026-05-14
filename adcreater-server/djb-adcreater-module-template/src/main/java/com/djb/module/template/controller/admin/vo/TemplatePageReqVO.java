package com.djb.module.template.controller.admin.vo;

import com.djb.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplatePageReqVO extends PageParam {
    private String name;
    private String category;
    private String status;
}
