package com.djb.framework.env.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 环境配置
 *
 * @author djbadmin
 */
@ConfigurationProperties(prefix = "djb.env")
@Data
public class EnvProperties {

    public static final String TAG_KEY = "djb.env.tag";

    /**
     * 环境标签
     */
    private String tag;

}
