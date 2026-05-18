package com.djb.module.ad.controller;

import com.djb.module.ad.controller.vo.*;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.service.ModelOrchestrationService;
import com.djb.module.ai.service.PromptService;
import com.djb.module.billing.dal.dataobject.PointsTransactionDO;
import com.djb.module.billing.service.BillingService;
import com.djb.framework.common.exception.ServiceException;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * <h2>管理端 — 广告营销策划之"生成营销策划"接口</h2>
 *
 * <p>
 * 本 Controller 对应前端「广告营销策划」面板的 <b>"生成营销策划"</b> 功能按钮，
 * 负责接收用户的商品信息与目标市场参数，驱动 AI 完成跨文化广告文案的翻译与本地化。
 * </p>
 *
 * <h3>在广告营销策划流程中的位置</h3>
 * <p>
 * 前端「广告营销策划」面板提供两个 AI 驱动的核心功能按钮：
 * </p>
 * <ol>
 *   <li><b>"分析营销策略"</b> → {@link MarketingController#analyze}
 *       —— 全量营销分析，输出 riskLevel / cultureNotes / coreStrategy / exampleAdCopy 四字段 JSON</li>
 *   <li><b>"生成营销策划"</b> → 本 Controller 的 {@link #translate(TranslateReqVO)}
 *       —— 基于用户输入的商品信息，生成目标市场的本地化广告文案</li>
 * </ol>
 *
 * <h3>用户操作流程</h3>
 * <p>
 * 用户在「广告营销策划」面板完成以下输入后点击 <b>"生成营销策划"</b>：
 * </p>
 * <ol>
 *   <li>填写 <b>商品标题</b>（productTitle）—— 如 "智能筋膜枪 Pro"</li>
 *   <li>填写 <b>商品描述</b>（productDescription）—— 产品特性与卖点说明</li>
 *   <li>填写 <b>卖点列表</b>（sellingPoints）—— 如 ["静音设计", "5档调节", "Type-C充电"]</li>
 *   <li>选择 <b>目标市场</b>（targetMarket）—— 如 "美国"、"日本"、"东南亚"</li>
 *   <li>选择 <b>目标语言</b>（targetLang）—— 如 "en"、"ja"</li>
 *   <li>选择 <b>源语言</b>（sourceLang）—— 通常为 "zh"</li>
 * </ol>
 *
 * <h3>与 App 端的关系</h3>
 * <pre>
 * 前端 App 端请求:
 *   /ad/copy/translate → {@link com.djb.module.ad.controller.app.AppAdCopyController}
 *       └── 透明转发 → 本 Controller ({@code /api/ad/copy/translate})
 *
 * 设计意图:
 *   - 本 Controller 承载完整业务逻辑（单点维护）
 *   - App 端 Controller 仅做路由代理，便于网关对不同路径施加独立策略
 * </pre>
 *
 * <h3>请求/响应模型</h3>
 * <pre>
 * 请求 {@link TranslateReqVO}:
 *   ├── productTitle       // 商品标题（必填，Prompt 中替换 {{product_name}}）
 *   ├── productDescription // 商品描述（可选，Prompt 中替换 {{description}}）
 *   ├── sellingPoints      // 卖点列表（Prompt 中替换 {{selling_points}}，逗号拼接）
 *   ├── targetMarket       // 目标市场（Prompt 中替换 {{target_market}}）
 *   ├── targetLang         // 目标语言（Prompt 中替换 {{target_lang}}）
 *   └── sourceLang         // 源语言（传入 ModelOrchestrationService）
 *
 * 响应 {@link TranslateRespVO}:
 *   ├── translatedTitle    // 翻译后的商品标题
 *   └── localizedCopy      // 本地化后的完整广告文案
 * </pre>
 *
 * @see MarketingController 同页面的"分析营销策略"接口
 * @see com.djb.module.ad.controller.app.AppAdCopyController App 端门面 Controller
 * @see PromptService#resolvePrompt Prompt 模板解析（category="copy"）
 */
@Tag(name = "用户端 - 广告营销策划")
@RestController
@RequestMapping("/api/ad/copy")
@Validated
@Slf4j
public class AdCopyController {

    /**
     * 模型编排服务 —— 统一入口，根据 DB 配置或 fallback 策略选择 AI 模型执行翻译。
     * <p>
     * {@link ModelOrchestrationService#translate(String, String, String, String)}
     * 内部会走 {@code executeWithFallback("copy", ...)}，按优先级尝试所有已启用的模型配置。
     * </p>
     */
    @Resource
    private ModelOrchestrationService orchestrationService;

    /**
     * Prompt 模板服务 —— 根据 category="copy" 查找已启用的 Prompt 模板，
     * 将用户输入的变量填充到模板中，返回可直接发给 AI 的完整 prompt 文本。
     * <p>
     * 模板中使用的占位符（与 vars Map 的 key 一一对应）：
     * </p>
     * <ul>
     *   <li>{@code {{product_name}}} → reqVO.getProductTitle()</li>
     *   <li>{@code {{description}}} → reqVO.getProductDescription()</li>
     *   <li>{@code {{selling_points}}} → reqVO.getSellingPoints() 逗号拼接</li>
     *   <li>{@code {{target_market}}} → reqVO.getTargetMarket()</li>
     *   <li>{@code {{target_lang}}} → reqVO.getTargetLang()</li>
     * </ul>
     */
    @Resource
    private PromptService promptService;

    /**
     * 计费服务 —— 执行积分预扣。
     * <p>
     * 每次"生成营销策划"消耗 <b>5 积分</b>（{@code cost = 5}）。
     * </p>
     */
    @Resource
    private BillingService billingService;

    /**
     * <h3>生成营销策划（翻译 + 本地化广告文案）</h3>
     *
     * <p>
     * 对应前端「广告营销策划」面板的 <b>"生成营销策划"</b> 按钮。
     * 核心流程分三步：Prompt 模板解析 → 积分预扣 → AI 翻译调用 → 组装响应。
     * </p>
     *
     * <h4>处理流程</h4>
     * <ol>
     *   <li><b>获取当前用户 ID</b> —— 通过 {@link WebFrameworkUtils#getLoginUserId()} 从请求上下文提取</li>
     *   <li><b>构建 Prompt 变量 Map</b> —— 将 {@link TranslateReqVO} 各字段映射为模板占位符的值：
     *       <pre>
     *       product_name  → reqVO.getProductTitle()
     *       description   → reqVO.getProductDescription() 或 ""
     *       selling_points → reqVO.getSellingPoints() 逗号拼接
     *       target_market → reqVO.getTargetMarket()
     *       target_lang   → reqVO.getTargetLang()
     *       </pre>
     *   </li>
     *   <li><b>解析 Prompt 模板</b> —— {@link PromptService#resolvePrompt(String, Map)}，
     *       category="copy"，将变量填充到 DB 模板或 fallback 硬编码模板</li>
     *   <li><b>积分预扣</b> —— {@link BillingService#preConsume(Long, int, String)}，
     *       固定消费 5 积分/次。
     *       <b>若余额不足</b>，BillingService 抛出 {@link ServiceException}(400, "点数不足: ...")，
     *       本方法捕获后转换为 <b>"积分不足，请充值"</b> 返回前端展示。</li>
     *   <li><b>AI 翻译调用</b> —— {@link ModelOrchestrationService#translate(String, String, String, String)}
     *       ，传入已解析的完整 prompt 文本</li>
     *   <li><b>组装响应</b> —— 使用 AI 返回的 {@link AiResult#getRevisedPrompt()} 作为本地化文案</li>
     *   <li><b>计费确认/回滚</b> —— AI 调用成功 → {@link BillingService#confirmConsume(Long)}；
     *       调用失败 → {@link BillingService#rollbackConsume(Long)} 退还积分</li>
     * </ol>
     *
     * <h4>完整调用链路</h4>
     * <pre>
     * AdCopyController.translate(reqVO)
     *   ├── WebFrameworkUtils.getLoginUserId()                  // 获取用户ID
     *   ├── promptService.resolvePrompt("copy", vars)           // Step 1: 解析 Prompt 模板
     *   │     ├── DB 查询 PromptTemplateDO (category="copy", enabled=true)
     *   │     │     ├── 命中 → 正则替换 {{变量名}} → 返回完整 prompt
     *   │     │     └── 未命中 → buildDefaultPrompt("copy", vars) 硬编码兜底
     *   │     └── 返回: 可直接发给 AI 的完整 prompt 字符串
     *   ├── billingService.preConsume(userId, 5, null)          // Step 2: 积分预扣（5积分）
     *   │     ├── 余额充足 → 扣减积分，返回 PointsTransactionDO (status=pending)
     *   │     └── 余额不足 → 抛出 ServiceException → 转换为 "积分不足，请充值"
     *   ├── orchestrationService.translate(                     // Step 3: AI 翻译
     *   │       prompt, sourceLang, targetLang, targetMarket)   //    传入已解析的完整 prompt
     *   │     └── executeWithFallback("copy", ...)
     *   │           ├── DB 查询已启用模型 (AiModelConfigDO, enabled=true, ORDER BY priority)
     *   │           │     ├── 有模型 → 逐个尝试 adapter.call(request, config)
     *   │           │     └── 无模型/异常 → executeWithFallbackConfig() 硬编码 fallback
     *   │           └── 返回 AiResult (success, revisedPrompt, inputTokens, outputTokens)
     *   ├── 组装 TranslateRespVO ← AiResult.getRevisedPrompt()  // Step 4: 组装响应
     *   ├── billingService.confirmConsume(tx.getId())           // Step 5a: AI成功 → 扣点生效
     *   └── billingService.rollbackConsume(tx.getId())          // Step 5b: AI失败 → 退还积分
     * </pre>
     *
     * @param reqVO 翻译请求 VO，包含商品信息与目标市场/语言参数
     * @return 通用响应包装的 {@link TranslateRespVO}（翻译后标题 + 本地化文案）
     * @see MarketingController#analyze 同面板的"分析营销策略"接口
     */
    @Operation(summary = "生成营销策划")
    @PostMapping("/translate")
    public CommonResult<TranslateRespVO> translate(@Valid @RequestBody TranslateReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();

        // Step 1: 构建 Prompt 模板变量 Map，key 对应模板中 {{变量名}} 的变量名
        Map<String, String> vars = Map.of(
            "product_name", reqVO.getProductTitle(),
            "description", reqVO.getProductDescription() != null ? reqVO.getProductDescription() : "",
            "selling_points", String.join(", ", reqVO.getSellingPoints()),
            "target_market", reqVO.getTargetMarket(),
            "target_lang", reqVO.getTargetLang()
        );

        // Step 2: 解析 Prompt 模板（category="copy"），将变量填充到模板中
        String prompt = promptService.resolvePrompt("copy", vars);

        // Step 3: 积分预扣（生成营销策划固定消耗 5 积分）
        //         若余额不足，BillingService 会抛出 ServiceException("点数不足: ...")，
        //         此处捕获后转换为面向用户的消息 "积分不足，请充值"，前端可直接展示。
        int cost = 5;
        PointsTransactionDO tx;
        try {
            tx = billingService.preConsume(userId, cost, null);
        } catch (ServiceException e) {
            // 积分不足 → 抛出用户可读的错误消息，前端弹窗/Toast 展示
            throw new ServiceException(400, "积分不足，请充值");
        }

        try {
            // Step 4: 调用 AI 模型执行翻译/本地化，将解析后的完整 prompt 传入
            AiResult result = orchestrationService.translate(
                prompt, reqVO.getSourceLang(),
                reqVO.getTargetLang(), reqVO.getTargetMarket());

            if (!result.isSuccess()) {
                throw new RuntimeException("AI 翻译失败: " + result.getErrorMessage());
            }

            // Step 5: 组装响应 VO，使用 AI 返回的本地化文案
            TranslateRespVO resp = new TranslateRespVO();
            resp.setTranslatedTitle(reqVO.getProductTitle());
            resp.setLocalizedCopy(result.getRevisedPrompt());

            // Step 6: 计费确认（AI 调用成功后扣点生效）
            billingService.confirmConsume(tx.getId());
            return success(resp);
        } catch (Exception e) {
            // 计费回滚（AI 调用失败时退还积分）
            billingService.rollbackConsume(tx.getId());
            log.error("Translation failed for user {}", userId, e);
            throw new RuntimeException("翻译失败: " + e.getMessage(), e);
        }
    }
}
