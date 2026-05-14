package com.djb.common.exception;

import com.djb.framework.common.exception.ServiceException;

public final class AiCallException {

    private AiCallException() {
    }

    public static ServiceException of(String modelName, String detail) {
        return new ServiceException(500, String.format("AI模型调用失败 [%s]: %s", modelName, detail));
    }
}
