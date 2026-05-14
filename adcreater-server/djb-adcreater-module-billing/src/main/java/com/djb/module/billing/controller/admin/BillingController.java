package com.djb.module.billing.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djb.module.billing.controller.admin.vo.RechargeReqVO;
import com.djb.module.billing.dal.dataobject.PointsTransactionDO;
import com.djb.module.billing.dal.mapper.PointsTransactionMapper;
import com.djb.module.billing.service.BillingService;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 计费管理
 *
 * @author adcreater
 */
@Tag(name = "管理后台 - 计费管理")
@RestController
@RequestMapping("/admin-api/billing")
@Validated
@Slf4j
public class BillingController {

    @Resource
    private BillingService billingService;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @PostMapping("/recharge")
    @Operation(summary = "用户充值")
    @PreAuthorize("@ss.hasPermission('billing:recharge')")
    public CommonResult<Boolean> recharge(@Valid @RequestBody RechargeReqVO reqVO) {
        billingService.recharge(reqVO.getUserId(), reqVO.getAmount(), reqVO.getRemark());
        return success(true);
    }

    @GetMapping("/transaction/page")
    @Operation(summary = "分页查询交易流水")
    @PreAuthorize("@ss.hasPermission('billing:query')")
    public CommonResult<PageResult<PointsTransactionDO>> getTransactionPage(@Valid PageParam pageParam) {
        return success(transactionMapper.selectPage(pageParam,
                new LambdaQueryWrapper<PointsTransactionDO>().orderByDesc(PointsTransactionDO::getId)));
    }
}
