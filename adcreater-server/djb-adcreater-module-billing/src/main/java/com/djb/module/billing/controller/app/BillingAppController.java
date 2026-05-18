package com.djb.module.billing.controller.app;

import com.djb.module.billing.dal.dataobject.PointsTransactionDO;
import com.djb.module.billing.controller.app.vo.AppBillingRechargeReqVO;
import com.djb.module.billing.dal.mapper.PointsTransactionMapper;
import com.djb.module.billing.service.BillingService;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.pojo.PageParam;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - 计费
 *
 * @author adcreater
 */
@Tag(name = "用户端 - 计费")
@RestController
@RequestMapping("/api/billing")
@Validated
@Slf4j
public class BillingAppController {

    @Resource
    private BillingService billingService;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @GetMapping("/balance")
    @Operation(summary = "查询余额")
    public CommonResult<Map<String, Integer>> getBalance() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        int balance = billingService.getBalance(userId);
        return success(Map.of("balance", balance));
    }

    @GetMapping("/transaction/page")
    @Operation(summary = "分页查询交易流水")
    public CommonResult<PageResult<PointsTransactionDO>> getTransactionPage(@Valid PageParam pageParam) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        return success(transactionMapper.selectPage(pageParam,
            new LambdaQueryWrapper<PointsTransactionDO>()
                .eq(PointsTransactionDO::getUserId, userId)
                .orderByDesc(PointsTransactionDO::getCreateTime)));
    }

    @PostMapping("/recharge")
    @Operation(summary = "用户充值")
    public CommonResult<Map<String, Object>> recharge(@Valid @RequestBody AppBillingRechargeReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        billingService.recharge(userId, reqVO.getPoints(), "用户充值套餐 " + reqVO.getPackageId());
        return success(Map.of(
                "payUrl", null,
                "message", "充值成功",
                "packageId", reqVO.getPackageId()
        ));
    }
}
