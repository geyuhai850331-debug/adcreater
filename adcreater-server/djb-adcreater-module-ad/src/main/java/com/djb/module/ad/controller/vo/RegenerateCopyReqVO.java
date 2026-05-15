package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RegenerateCopyReqVO {

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotBlank(message = "产品描述不能为空")
    private String productDescription;

    private String chineseAdCopy;

    @NotBlank(message = "目标市场不能为空")
    private String targetMarket;

    @NotBlank(message = "合规风险等级不能为空")
    private String riskLevel;

    @NotBlank(message = "文化本土化建议不能为空")
    private String cultureNotes;

    @NotBlank(message = "核心营销策略不能为空")
    private String coreStrategy;
}
