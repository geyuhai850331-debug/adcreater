package com.djb.module.delivery.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class PlatformExportReqVO {
    @NotEmpty
    private List<String> platforms;
    private String baseImageUrl;
    private String copyText;
}
