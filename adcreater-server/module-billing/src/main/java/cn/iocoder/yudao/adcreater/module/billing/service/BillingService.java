package cn.iocoder.yudao.adcreater.module.billing.service;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;

public interface BillingService {
    void recharge(Long userId, int amount, String remark);
    PointsTransactionDO preConsume(Long userId, int amount, Long bizId);
    void confirmConsume(Long transactionId);
    void rollbackConsume(Long transactionId);
    int getBalance(Long userId);
    void ensureAccount(Long userId);
}
