package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import java.util.List;

@Data
public class TranslateRespVO {
    private String translatedTitle;
    private String translatedDescription;
    private List<String> translatedSellingPoints;
    private String localizedCopy;
}
