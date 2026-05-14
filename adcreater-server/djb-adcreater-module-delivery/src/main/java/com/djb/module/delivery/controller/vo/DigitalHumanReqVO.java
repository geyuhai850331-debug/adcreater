package com.djb.module.delivery.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class DigitalHumanReqVO {
    @NotBlank
    private String avatarId;
    @NotBlank
    private String script;
    private String voiceId;
    @NotNull
    private Integer duration;
    private String backgroundUrl;
}
