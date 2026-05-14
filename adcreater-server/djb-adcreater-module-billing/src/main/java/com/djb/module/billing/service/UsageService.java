package com.djb.module.billing.service;

public interface UsageService {
    void record(Long userId, String taskType, String modelUsed,
                int pointsConsumed, int inputTokens, int outputTokens);
}
