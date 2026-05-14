package com.djb.framework.operatelog.config;

import com.djb.framework.common.biz.system.logger.OperateLogCommonApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * OperateLog 使用到 Feign 的配置项
 *
 * @author djbadmin
 */
@AutoConfiguration
@EnableFeignClients(clients = {OperateLogCommonApi.class}) // 主要是引入相关的 API 服务
public class DjbOperateLogRpcAutoConfiguration {
}
