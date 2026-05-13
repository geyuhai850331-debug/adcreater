package cn.iocoder.yudao.adcreater.module.template.controller.app.vo;

import lombok.Data;
import java.util.List;

@Data
public class SyncResultVO {

    private List<UpdatedTemplate> updated;
    private List<Long> deleted;

    @Data
    public static class UpdatedTemplate {
        private Long templateId;
        private Integer version;
        private String fileUrl;
        private String changelog;
    }
}
