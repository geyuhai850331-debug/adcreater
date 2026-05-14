package com.djb.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 * @author djbadmin
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${djb.info.base-package}
@SpringBootApplication(scanBasePackages = {"${djb.info.base-package}.server", "${djb.info.base-package}.module"},
        excludeName = {
            // RPC 相关
//            "org.springframework.cloud.openfeign.FeignAutoConfiguration",
//            "com.djb.module.system.framework.rpc.config.RpcConfiguration"
        })
public class DjbServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(DjbServerApplication.class, args);
    }

}
