package com.djb.module.ad.controller.app.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppProjectRespVO {

    private String name;

    private String type;

    private String status;

    private LocalDateTime createdAt;
}
