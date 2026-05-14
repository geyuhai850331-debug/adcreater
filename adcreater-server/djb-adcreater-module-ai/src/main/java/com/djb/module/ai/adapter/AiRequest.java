package com.djb.module.ai.adapter;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class AiRequest {
    private String prompt;
    private String negativePrompt;
    private Integer width;
    private Integer height;
    private String style;
    private Integer duration;
    private String model;
    private Map<String, Object> extraParams = new HashMap<>();
}
