package com.djb.common.exception;

import com.djb.framework.common.exception.ServiceException;

public final class InsufficientPointsException {

    private InsufficientPointsException() {
    }

    public static ServiceException of(int required, int available) {
        return new ServiceException(400, String.format("点数不足: 需要 %d, 当前余额 %d", required, available));
    }
}
