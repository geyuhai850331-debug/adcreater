package cn.iocoder.yudao.adcreater.module.ad.controller;

import cn.iocoder.yudao.adcreater.module.ad.controller.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiResult;
import cn.iocoder.yudao.adcreater.module.ai.service.ModelOrchestrationService;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/ad/copy")
public class AdCopyController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @PostMapping("/translate")
    public CommonResult<TranslateRespVO> translate(@Valid @RequestBody TranslateReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();

        Map<String, String> vars = Map.of(
            "product_name", reqVO.getProductTitle(),
            "description", reqVO.getProductDescription() != null ? reqVO.getProductDescription() : "",
            "selling_points", String.join(", ", reqVO.getSellingPoints()),
            "target_market", reqVO.getTargetMarket(),
            "target_lang", reqVO.getTargetLang()
        );

        String prompt = promptService.resolvePrompt("copy", vars);

        int cost = 5;
        billingService.preConsume(userId, cost, null);

        try {
            AiResult result = orchestrationService.translate(
                reqVO.getProductTitle(), reqVO.getSourceLang(),
                reqVO.getTargetLang(), reqVO.getTargetMarket());

            TranslateRespVO resp = new TranslateRespVO();
            resp.setTranslatedTitle(reqVO.getProductTitle()); // placeholder translation
            resp.setLocalizedCopy(prompt);
            return CommonResult.success(resp);
        } catch (Exception e) {
            throw new RuntimeException("Translation failed: " + e.getMessage(), e);
        }
    }
}
