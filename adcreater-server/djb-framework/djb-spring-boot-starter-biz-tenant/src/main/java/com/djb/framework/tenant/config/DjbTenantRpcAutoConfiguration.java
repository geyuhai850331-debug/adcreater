package com.djb.framework.tenant.config;

import com.djb.framework.tenant.core.rpc.TenantRequestInterceptor;
import com.djb.framework.common.biz.system.tenant.TenantCommonApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "feign.RequestInterceptor")
@ConditionalOnProperty(prefix = "djb.tenant", value = "enable", matchIfMissing = true) // 允许使用 djb.tenant.enable=false 禁用多租户
@EnableFeignClients(clients = TenantCommonApi.class) // 主要是引入相关的 API 服务
public class DjbTenantRpcAutoConfiguration {

    @Bean
    public TenantRequestInterceptor tenantRequestInterceptor() {
        return new TenantRequestInterceptor();
    }

}
