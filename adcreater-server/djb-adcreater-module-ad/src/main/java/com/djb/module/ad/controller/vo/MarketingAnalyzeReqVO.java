package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class MarketingAnalyzeReqVO {

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotBlank(message = "产品描述不能为空")
    private String productDescription;

    @NotBlank(message = "中文广告词不能为空")
    private String chineseAdCopy;

    @NotBlank(message = "目标市场不能为空")
    private String targetMarket;
}
