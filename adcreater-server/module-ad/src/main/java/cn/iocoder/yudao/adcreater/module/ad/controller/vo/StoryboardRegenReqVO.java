package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 分镜重新生成请求 VO
 */
@Data
public class StoryboardRegenReqVO {

    @NotBlank(message = "场景描述不能为空")
    private String description;

    @NotNull(message = "场景序号不能为空")
    private Integer index;
}
