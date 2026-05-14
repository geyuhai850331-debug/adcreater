package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class VideoGenReqVO {
    @NotBlank
    private String productName;
    private String style;
    @NotNull
    private Integer duration;
}
