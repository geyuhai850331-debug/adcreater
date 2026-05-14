package com.djb.module.ad.controller.vo;

import lombok.Data;

/**
 * 关键帧/网格图/视频生成通用响应 VO
 */
@Data
public class MediaRespVO {

    private String url;       // 生成结果 URL

    private String imageUrl;  // 别名 (兼容前端不同字段名)
}
