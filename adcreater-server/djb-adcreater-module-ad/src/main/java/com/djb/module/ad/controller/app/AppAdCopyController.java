package com.djb.module.ad.controller.app;

import com.djb.framework.common.pojo.CommonResult;
import com.djb.module.ad.controller.AdCopyController;
import com.djb.module.ad.controller.vo.TranslateReqVO;
import com.djb.module.ad.controller.vo.TranslateRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h2>用户端 App — 广告营销策划之"生成营销策划"接口</h2>
 *
 * <p>
 * 本 Controller 是 <b>App 端轻量门面</b>，对应前端「广告营销策划」面板中
 * <b>"生成营销策划"</b> 功能按钮的后端入口。
 * </p>
 *
 * <h3>职责定位</h3>
 * <p>
 * 自身不承载业务逻辑，仅作为 <b>路由代理</b> 将请求直接委托给
 * {@link AdCopyController#translate(TranslateReqVO)}。
 * 这种双层 Controller 设计的分工：
 * </p>
 * <ul>
 *   <li><b>AdCopyController（管理端）</b> —— 位于 {@code controller} 包，
 *       路径 {@code /api/ad/copy}，承载完整业务闭环（Prompt 解析 → 计费扣点 → AI 调用 → 结果返回）</li>
 *   <li><b>AppAdCopyController（用户端）</b> —— 位于 {@code controller/app} 包，
 *       路径 {@code /ad/copy}，仅做透明转发，便于在网关层对两类请求施加不同的限流/鉴权策略</li>
 * </ul>
 *
 * <h3>所属页面流程</h3>
 * <p>
 * 在前端「广告营销策划」工作流中，用户完成以下步骤后点击 <b>"生成营销策划"</b> 触发本接口：
 * </p>
 * <ol>
 *   <li>输入/粘贴 <b>原始中文广告文案</b></li>
 *   <li>选择 <b>目标市场</b>（如 美国、日本、东南亚）</li>
 *   <li>选择 <b>目标语种</b>（如 英语、日语）</li>
 *   <li>填写 <b>商品标题</b> + <b>商品描述</b> + <b>卖点列表</b></li>
 *   <li>点击"生成营销策划" → 本接口接收 {@link TranslateReqVO}，
 *       委托 {@link AdCopyController} 完成 AI 驱动的跨文化广告文案本地化</li>
 * </ol>
 *
 * <h3>请求/响应模型</h3>
 * <pre>
 * 请求 {@link TranslateReqVO}:
 *   ├── productTitle       // 商品标题
 *   ├── productDescription // 商品描述（可选）
 *   ├── sellingPoints      // 卖点列表
 *   ├── targetMarket       // 目标市场（如 "美国"）
 *   ├── targetLang         // 目标语言（如 "en"）
 *   ├── sourceLang         // 源语言（如 "zh"）
 *   └── targetMarket       // 目标市场（与上方重复，实际使用 reqVO.getTargetMarket()）
 *
 * 响应 {@link TranslateRespVO}:
 *   ├── translatedTitle    // 翻译后的商品标题
 *   └── localizedCopy      // 本地化后的广告文案
 * </pre>
 *
 * @see AdCopyController 管理端广告文案翻译 Controller（承载实际业务逻辑）
 * @see com.djb.module.ad.controller.MarketingController 营销分析 Controller（同页面的"分析营销策略"按钮）
 */
@Tag(name = "用户 App - 广告文案翻译")
@RestController
@RequestMapping("/ad/copy")
@Validated
@Slf4j
public class AppAdCopyController {

    /**
     * 管理端广告文案翻译 Controller 引用。
     * <p>
     * 本类所有接口都直接委托给该 Controller 处理，自身不执行业务逻辑。
     * 委托模式的优势：统一业务入口 + 网关可以针对 `/ad/copy` 和 `/api/ad/copy` 设置不同策略。
     * </p>
     */
    @Resource
    private AdCopyController adCopyController;

    /**
     * <h3>生成营销策划（翻译 + 本地化广告文案）</h3>
     *
     * <p>
     * 对应前端「广告营销策划」面板的 <b>"生成营销策划"</b> 按钮。
     * 接收用户的商品信息与目标市场参数，生成适合目标市场的本地化广告文案。
     * </p>
     *
     * <h4>处理流程（委托链）</h4>
     * <ol>
     *   <li>接收 {@link TranslateReqVO} 请求体</li>
     *   <li>直接委托 {@link AdCopyController#translate(TranslateReqVO)}</li>
     *   <li>被委托方内部执行：Prompt 模板变量替换 → 计费预扣 → AI 翻译调用 → 计费确认 → 返回结果</li>
     * </ol>
     *
     * <h4>调用链路</h4>
     * <pre>
     * AppAdCopyController.translate(reqVO)
     *   → AdCopyController.translate(reqVO)
     *     → PromptService.resolvePrompt("copy", vars)        // 从 DB/fallback 获取 Prompt 模板
     *     → BillingService.preConsume(userId, cost)          // 计费预扣（5 积分/次）
     *     → ModelOrchestrationService.translate(...)          // AI 模型编排翻译
     *     → BillingService.confirmConsume(tx.getId())        // 计费确认
     *     → 返回 TranslateRespVO
     * </pre>
     *
     * @param reqVO 翻译请求（商品标题、描述、卖点、目标市场/语言、源语言）
     * @return 翻译结果（翻译后标题 + 本地化文案）
     * @see AdCopyController#translate(TranslateReqVO) 实际执行翻译业务逻辑的管理端 Controller
     */
    @PostMapping("/translate")
    @Operation(summary = "翻译广告文案")
    public CommonResult<TranslateRespVO> translate(@Valid @RequestBody TranslateReqVO reqVO) {
        return adCopyController.translate(reqVO);
    }
}
