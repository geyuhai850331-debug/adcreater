package cn.iocoder.yudao.adcreater.module.billing.service;

public interface UsageService {
    void record(Long userId, String taskType, String modelUsed,
                int pointsConsumed, int inputTokens, int outputTokens);
}
