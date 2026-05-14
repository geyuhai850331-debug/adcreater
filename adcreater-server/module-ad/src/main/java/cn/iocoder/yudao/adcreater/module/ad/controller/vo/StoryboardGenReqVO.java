package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 分镜策划请求 VO
 */
@Data
public class StoryboardGenReqVO {

    @NotBlank(message = "商品描述不能为空")
    private String productDescription;

    private String category;        // electronics/home/beauty/food/sports/apparel/baby

    private String targetPlatform;  // amazon/facebook/instagram/tiktok/youtube

    private String targetLang;      // zh-CN/en-US/ja-JP/ko-KR/de-DE/fr-FR

    private String templateId;      // vt1/vt2/vt3/vt4
}
