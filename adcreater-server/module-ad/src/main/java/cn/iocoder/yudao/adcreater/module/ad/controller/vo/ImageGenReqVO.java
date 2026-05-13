package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ImageGenReqVO {
    @NotBlank
    private String productName;
    private String style;
    private String background;
    @NotBlank
    private String targetMarket;
    @NotBlank
    private String sellingPoints;
    @NotNull
    private Integer width;
    @NotNull
    private Integer height;
}
