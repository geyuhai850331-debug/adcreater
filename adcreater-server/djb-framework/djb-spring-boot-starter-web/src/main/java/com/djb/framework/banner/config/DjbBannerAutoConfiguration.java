package com.djb.framework.banner.config;

import com.djb.framework.banner.core.BannerApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Banner 的自动配置类
 *
 * @author djbadmin
 */
@AutoConfiguration
public class DjbBannerAutoConfiguration {

    @Bean
    public BannerApplicationRunner bannerApplicationRunner() {
        return new BannerApplicationRunner();
    }

}
