package cn.iocoder.yudao.adcreater.module.billing.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

@Data
public class RechargeReqVO {
    @NotNull
    private Long userId;
    @NotNull @Min(1)
    private Integer amount;
    private String remark;
}
