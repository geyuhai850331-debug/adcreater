package com.djb.module.template.controller.app.vo;

import lombok.Data;
import java.util.List;

@Data
public class SyncRequestVO {

    private List<LocalVersion> localVersions;

    @Data
    public static class LocalVersion {
        private Long templateId;
        private Integer version;
    }
}
