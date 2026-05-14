package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchImageGenReqVO {
    @NotBlank
    private String productName;
    @NotEmpty
    private List<SizeSpec> sizes;

    @Data
    public static class SizeSpec {
        private String platform;
        private Integer width;
        private Integer height;
    }
}
