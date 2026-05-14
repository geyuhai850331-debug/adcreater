package com.djb.module.ai.adapter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiResult {
    private boolean success;
    private String url;
    private String revisedPrompt;
    private Integer inputTokens;
    private Integer outputTokens;
    private String errorMessage;
}
