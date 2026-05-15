package com.djb.module.ai.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiApiKeyCodecTest {

    @Test
    void shouldDecodeStoredBase64ApiKey() {
        String rawApiKey = "sk-test-1234567890";

        String encodedApiKey = AiApiKeyCodec.encode(rawApiKey);

        assertEquals(rawApiKey, AiApiKeyCodec.decode(encodedApiKey));
    }

    @Test
    void shouldKeepLegacyPlaintextApiKeyWhenDecodeFails() {
        String legacyApiKey = "sk-proj-legacy-key";

        assertEquals(legacyApiKey, AiApiKeyCodec.decode(legacyApiKey));
    }

    @Test
    void shouldStripBearerPrefixAndWhitespace() {
        String rawApiKey = "  Bearer sk-deepseek-test-key  ";

        String encodedApiKey = AiApiKeyCodec.encode(rawApiKey);

        assertEquals(rawApiKey, AiApiKeyCodec.decode(encodedApiKey));
        assertEquals("sk-deepseek-test-key", AiApiKeyCodec.normalizeForAuthorization(AiApiKeyCodec.decode(encodedApiKey)));
        assertEquals("sk-deepseek-test-key", AiApiKeyCodec.normalizeForAuthorization(rawApiKey));
    }

    @Test
    void shouldNotEncodeMaskedApiKeyAsNewSecret() {
        String maskedApiKey = "sk-****abcd";

        assertEquals(maskedApiKey, AiApiKeyCodec.decode(maskedApiKey));
    }

    @Test
    void shouldKeepWhitespaceAndBearerWhenEncodingAndDecoding() {
        String rawApiKey = "  Bearer sk-preserve-this-exact-value  ";

        String encodedApiKey = AiApiKeyCodec.encode(rawApiKey);

        assertEquals(rawApiKey, AiApiKeyCodec.decode(encodedApiKey));
    }
}
