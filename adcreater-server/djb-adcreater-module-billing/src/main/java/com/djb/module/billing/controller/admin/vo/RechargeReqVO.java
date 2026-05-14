package com.djb.module.billing.controller.admin.vo;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class RechargeReqVO {
    @NotNull
    private Long userId;
    @NotNull @Min(1)
    private Integer amount;
    private String remark;
}
