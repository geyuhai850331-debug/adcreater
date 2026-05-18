package com.djb.module.billing.controller.app.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppBillingRechargeReqVO {

    @NotBlank
    private String packageId;

    @NotNull
    @Min(1)
    private Integer points;

    @NotNull
    @Min(1)
    private Integer amount;
}
