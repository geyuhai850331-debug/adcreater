package cn.iocoder.yudao.adcreater.common.exception;

import cn.iocoder.yudao.framework.common.exception.ServiceException;

public class InsufficientPointsException extends ServiceException {
    public InsufficientPointsException(int required, int available) {
        super(400, String.format("点数不足: 需要 %d, 当前余额 %d", required, available));
    }
}
