package com.djb.module.infra.framework.file.config;

import com.djb.module.infra.framework.file.core.client.FileClientFactory;
import com.djb.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author djbadmin
 */
@Configuration(proxyBeanMethods = false)
public class DjbFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
