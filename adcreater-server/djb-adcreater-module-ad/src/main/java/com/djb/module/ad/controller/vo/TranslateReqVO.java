package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TranslateReqVO {
    @NotBlank
    private String productTitle;
    private String productDescription;
    @NotEmpty
    private List<String> sellingPoints;
    @NotBlank
    private String sourceLang;
    @NotBlank
    private String targetLang;
    @NotBlank
    private String targetMarket;
}