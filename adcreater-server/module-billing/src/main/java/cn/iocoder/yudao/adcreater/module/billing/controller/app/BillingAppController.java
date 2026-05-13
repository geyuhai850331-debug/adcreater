package cn.iocoder.yudao.adcreater.module.billing.controller.app;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.PointsTransactionMapper;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingAppController {

    @Resource
    private BillingService billingService;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @GetMapping("/balance")
    public CommonResult<Map<String, Integer>> getBalance() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        int balance = billingService.getBalance(userId);
        return CommonResult.success(Map.of("balance", balance));
    }

    @GetMapping("/transaction/page")
    public CommonResult<PageResult<PointsTransactionDO>> getTransactionPage(@Valid PageParam pageParam) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        return CommonResult.success(transactionMapper.selectPage(pageParam,
            new LambdaQueryWrapper<PointsTransactionDO>()
                .eq(PointsTransactionDO::getUserId, userId)
                .orderByDesc(PointsTransactionDO::getCreatedAt)));
    }
}
