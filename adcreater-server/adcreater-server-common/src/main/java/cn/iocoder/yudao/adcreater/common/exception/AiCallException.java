package cn.iocoder.yudao.adcreater.common.exception;

import cn.iocoder.yudao.framework.common.exception.ServiceException;

public class AiCallException extends ServiceException {
    public AiCallException(String modelName, String detail) {
        super(500, String.format("AI模型调用失败 [%s]: %s", modelName, detail));
    }
}
