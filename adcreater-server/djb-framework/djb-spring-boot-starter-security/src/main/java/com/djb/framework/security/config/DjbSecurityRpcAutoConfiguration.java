package com.djb.framework.security.config;

import com.djb.framework.common.biz.system.permission.PermissionCommonApi;
import com.djb.framework.security.core.rpc.LoginUserRequestInterceptor;
import com.djb.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * Security 使用到 Feign 的配置项
 *
 * @author djbadmin
 */
@AutoConfiguration
@ConditionalOnClass(name = "feign.RequestInterceptor")
@EnableFeignClients(clients = {OAuth2TokenCommonApi.class, // 主要是引入相关的 API 服务
        PermissionCommonApi.class})
public class DjbSecurityRpcAutoConfiguration {

    @Bean
    public LoginUserRequestInterceptor loginUserRequestInterceptor() {
        return new LoginUserRequestInterceptor();
    }

}
