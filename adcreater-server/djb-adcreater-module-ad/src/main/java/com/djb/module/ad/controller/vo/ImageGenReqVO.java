package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
