package com.djb.module.billing.service;

import com.djb.module.billing.dal.dataobject.*;
import com.djb.module.billing.dal.mapper.*;
import com.djb.common.exception.InsufficientPointsException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 点数计费服务实现
 *
 * @author adcreater
 */
@Service
@Validated
@Slf4j
public class BillingServiceImpl implements BillingService {

    @Resource
    private UserPointsAccountMapper accountMapper;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @Override
    @Transactional
    public void recharge(Long userId, int amount, String remark) {
        ensureAccount(userId);
        accountMapper.addPoints(userId, amount);
        UserPointsAccountDO account = accountMapper.selectOne(
            new LambdaQueryWrapper<UserPointsAccountDO>()
                .eq(UserPointsAccountDO::getUserId, userId));

        PointsTransactionDO tx = new PointsTransactionDO();
        tx.setUserId(userId);
        tx.setType("earn");
        tx.setAmount(amount);
        tx.setBalanceAfter(account.getBalance());
        tx.setStatus("confirmed");
        tx.setRemark(remark);
        // createTime auto-filled by DefaultDBFieldHandler
        transactionMapper.insert(tx);
    }

    @Override
    @Transactional
    public PointsTransactionDO preConsume(Long userId, int amount, Long bizId) {
        ensureAccount(userId);
        UserPointsAccountDO account = accountMapper.selectByUserIdForUpdate(userId);
        if (account.getBalance() < amount) {
            throw InsufficientPointsException.of(amount, account.getBalance());
        }
        int updated = accountMapper.deductPoints(userId, amount);
        if (updated == 0) {
            throw InsufficientPointsException.of(amount, 0);
        }
        PointsTransactionDO tx = new PointsTransactionDO();
        tx.setUserId(userId);
        tx.setType("consume");
        tx.setAmount(amount);
        tx.setBalanceAfter(account.getBalance() - amount);
        tx.setStatus("pending");
        tx.setBizId(bizId);
        // createTime auto-filled by DefaultDBFieldHandler
        transactionMapper.insert(tx);
        return tx;
    }

    @Override
    @Transactional
    public void confirmConsume(Long transactionId) {
        PointsTransactionDO tx = transactionMapper.selectById(transactionId);
        if (tx == null || !"pending".equals(tx.getStatus())) return;
        tx.setStatus("confirmed");
        transactionMapper.updateById(tx);
    }

    @Override
    @Transactional
    public void rollbackConsume(Long transactionId) {
        PointsTransactionDO tx = transactionMapper.selectById(transactionId);
        if (tx == null || !"pending".equals(tx.getStatus())) return;
        accountMapper.addPoints(tx.getUserId(), tx.getAmount());
        tx.setStatus("rolled_back");
        transactionMapper.updateById(tx);
    }

    @Override
    public int getBalance(Long userId) {
        ensureAccount(userId);
        UserPointsAccountDO account = accountMapper.selectOne(
            new LambdaQueryWrapper<UserPointsAccountDO>()
                .eq(UserPointsAccountDO::getUserId, userId));
        return account != null ? account.getBalance() : 0;
    }

    @Override
    public void ensureAccount(Long userId) {
        UserPointsAccountDO existing = accountMapper.selectOne(
            new LambdaQueryWrapper<UserPointsAccountDO>()
                .eq(UserPointsAccountDO::getUserId, userId));
        if (existing == null) {
            UserPointsAccountDO account = new UserPointsAccountDO();
            account.setUserId(userId);
            account.setBalance(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
            accountMapper.insert(account);
        }
    }
}
