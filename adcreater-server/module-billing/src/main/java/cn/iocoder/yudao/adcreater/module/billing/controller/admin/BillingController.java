package cn.iocoder.yudao.adcreater.module.billing.controller.admin;

import cn.iocoder.yudao.adcreater.module.billing.controller.admin.vo.RechargeReqVO;
import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.PointsTransactionMapper;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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
        return success(transactionMapper.selectPage(pageParam));
    }
}
