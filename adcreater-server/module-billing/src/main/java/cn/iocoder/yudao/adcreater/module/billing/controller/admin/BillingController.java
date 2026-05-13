package cn.iocoder.yudao.adcreater.module.billing.controller.admin;

import cn.iocoder.yudao.adcreater.module.billing.controller.admin.vo.RechargeReqVO;
import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.PointsTransactionMapper;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin-api/billing")
public class BillingController {

    @Resource
    private BillingService billingService;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @PostMapping("/recharge")
    @PreAuthorize("@ss.hasPermission('billing:recharge')")
    public CommonResult<Boolean> recharge(@Valid @RequestBody RechargeReqVO reqVO) {
        billingService.recharge(reqVO.getUserId(), reqVO.getAmount(), reqVO.getRemark());
        return CommonResult.success(true);
    }

    @GetMapping("/transaction/page")
    @PreAuthorize("@ss.hasPermission('billing:query')")
    public CommonResult<PageResult<PointsTransactionDO>> getTransactionPage(@Valid PageParam pageParam) {
        return CommonResult.success(transactionMapper.selectPage(pageParam));
    }
}
