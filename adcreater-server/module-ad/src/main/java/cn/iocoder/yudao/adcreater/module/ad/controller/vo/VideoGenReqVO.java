package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class VideoGenReqVO {
    @NotBlank
    private String productName;
    private String style;
    @NotNull
    private Integer duration;
}
