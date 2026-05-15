package com.djb.module.ai.controller.admin.vo;

import com.djb.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiModelConfigPageReqVO extends PageParam {
    private String modelName;
    private String category;
    private Boolean isEnabled;
}
