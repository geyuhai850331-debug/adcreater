package com.djb.module.ai.controller.admin.vo;

import cn.hutool.core.util.StrUtil;
import com.djb.framework.common.util.json.JsonUtils;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiModelConfigTestReqVO {

    private Long id;

    @Size(max = 64, message = "模型名称最长 64 字符")
    private String modelName;

    @Size(max = 255, message = "适配器类名最长 255 字符")
    private String adapterClass;

    @Size(max = 512, message = "API Key 最长 512 字符")
    private String apiKey;

    @Size(max = 255, message = "端点 URL 最长 255 字符")
    private String endpointUrl;

    private Boolean isEnabled;

    private Integer priority;

    private Integer sortOrder;

    private String extraConfig;

    @AssertTrue(message = "额外配置必须是 JSON 格式")
    public boolean isExtraConfigJson() {
        return StrUtil.isBlank(extraConfig) || JsonUtils.isJson(extraConfig);
    }

    @AssertTrue(message = "新增模型测试时，模型名称、适配器类和 API Key 不能为空")
    public boolean isRequiredFieldsValid() {
        if (id != null) {
            return true;
        }
        return StrUtil.isNotBlank(modelName)
                && StrUtil.isNotBlank(adapterClass)
                && StrUtil.isNotBlank(apiKey);
    }
}
