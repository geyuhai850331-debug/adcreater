package cn.iocoder.yudao.adcreater.module.delivery.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
