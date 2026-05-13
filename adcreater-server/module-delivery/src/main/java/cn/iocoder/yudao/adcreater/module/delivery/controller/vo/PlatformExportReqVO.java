package cn.iocoder.yudao.adcreater.module.delivery.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class PlatformExportReqVO {
    @NotEmpty
    private List<String> platforms;
    private String baseImageUrl;
    private String copyText;
}
