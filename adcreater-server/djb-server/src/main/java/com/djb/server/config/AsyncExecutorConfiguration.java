package com.djb.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
public class AsyncExecutorConfiguration {

    @Bean(name = "executorService", destroyMethod = "shutdown")
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(4, 16,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
