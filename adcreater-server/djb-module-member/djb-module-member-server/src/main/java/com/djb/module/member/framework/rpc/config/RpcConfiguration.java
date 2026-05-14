package com.djb.module.member.framework.rpc.config;

import com.djb.module.system.api.logger.LoginLogApi;
import com.djb.module.system.api.sms.SmsCodeApi;
import com.djb.module.system.api.social.SocialClientApi;
import com.djb.module.system.api.social.SocialUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "memberRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {SmsCodeApi.class, LoginLogApi.class, SocialUserApi.class, SocialClientApi.class})
public class RpcConfiguration {
}
