package com.djb.module.ai.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AI API Key 存储编解码。
 */
public final class AiApiKeyCodec {

    private static final String BEARER_PREFIX = "bearer ";

    private AiApiKeyCodec() {
    }

    public static String encode(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            return rawApiKey;
        }
        return Base64.getEncoder().encodeToString(rawApiKey.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String storedApiKey) {
        if (storedApiKey == null || storedApiKey.isBlank()) {
            return storedApiKey;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(storedApiKey), StandardCharsets.UTF_8);
            return isPrintableSecret(decoded) ? decoded : storedApiKey;
        } catch (IllegalArgumentException ex) {
            // 兼容历史明文数据，避免详情页和模型调用在读取旧数据时直接报错。
            return storedApiKey;
        }
    }

    public static String normalizeForAuthorization(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        String normalized = apiKey.trim();
        if (normalized.length() > BEARER_PREFIX.length()
                && normalized.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return normalized.substring(BEARER_PREFIX.length()).trim();
        }
        return normalized;
    }

    private static boolean isPrintableSecret(String value) {
        if (value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isISOControl(ch) && !Character.isWhitespace(ch)) {
                return false;
            }
        }
        return true;
    }
}
