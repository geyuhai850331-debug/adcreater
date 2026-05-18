package com.djb.module.ai.service;

import com.djb.module.ai.controller.admin.vo.*;
import com.djb.framework.common.pojo.PageResult;
import java.util.Map;

/**
 * Prompt 模板服务接口 —— 管理提示词模板的 CRUD 与运行时解析。
 *
 * <h3>两层职责</h3>
 * <ol>
 *   <li><b>管理后台 CRUD</b> —— 提供模板的增删改查分页，供管理页面维护 prompt 模板库</li>
 *   <li><b>运行时解析</b> —— 根据 {@code category} 查找已启用模板，做变量替换后返回完整 prompt</li>
 * </ol>
 *
 * <h3>核心方法：resolvePrompt</h3>
 * 这是整个 AI 模块 prompt 生成的统一入口。调用链路：
 * <pre>
 * Controller → PromptService.resolvePrompt(category, vars)
 *   → DB 查 PromptTemplateDO（按 category + enabled）
 *      ├── 命中 → 正则替换 {{varName}} / {{varName|default}} → 返回
 *      └── 未命中 → buildDefaultPrompt(category, vars) 硬编码兜底
 * </pre>
 *
 * <h3>变量语法</h3>
 * 模板中使用 {@code {{变量名}}} 或 {@code {{变量名|默认值}}} 声明变量占位符，
 * 运行时由 {@code variables} Map 中的值替换。例如：
 * <pre>
 * 模板: "为 {{product_name}} 生成 {{target_market|美国}} 市场的广告文案"
 * vars:  {product_name: "筋膜枪"}
 * 结果: "为 筋膜枪 生成 美国 市场的广告文案"
 * </pre>
 *
 * <h3>兜底策略</h3>
 * 当数据库不可用或未配置模板时，不抛异常，改用 {@code buildDefaultPrompt} 返回
 * 各 category 对应的硬编码 prompt。这是"模型管理上线前"的临时策略，
 * 确保系统在配置缺失时仍能降级运行。
 *
 * @see PromptServiceImpl
 */
public interface PromptService {

    // ========================================
    // 管理后台 CRUD
    // ========================================

    /** 创建模板，自动从 templateContent 提取变量名列表存入 variables 字段 */
    Long create(PromptTemplateSaveReqVO reqVO);

    /** 更新模板，同步刷新 variables 字段 */
    void update(PromptTemplateSaveReqVO reqVO);

    /** 更新模板启用/禁用状态（为管理端 StatusReqVO 统一入口） */
    void updateStatus(Long id, Boolean isEnabled);

    /** 删除模板 */
    void delete(Long id);

    /** 按 ID 查询单条模板 */
    PromptTemplateRespVO get(Long id);

    /** 分页查询模板列表，支持按 category / name 筛选 */
    PageResult<PromptTemplateRespVO> getPage(PromptTemplatePageReqVO pageReqVO);

    // ========================================
    // 运行时 Prompt 解析
    // ========================================

    /**
     * 根据分类名查找已启用模板，填充变量后返回可直接发给 AI 的 prompt 文本。
     *
     * <h4>查找逻辑</h4>
     * <ol>
     *   <li>从 DB 查 category 匹配且 enabled=true 的模板</li>
     *   <li>DB 异常 → log.warn + 降级到硬编码 prompt</li>
     *   <li>未找到模板 → 降级到硬编码 prompt</li>
     *   <li>找到模板 → 正则替换 {{var}} → 返回</li>
     * </ol>
     *
     * <h4>已知 category 值</h4>
     * <table border="1">
     *   <tr><th>category</th><th>用途</th><th>调用方</th></tr>
     *   <tr><td>marketing</td><td>全量营销分析（输出 4 字段 JSON）</td><td>{@code MarketingController#analyze}</td></tr>
     *   <tr><td>marketing_copy</td><td>单独重新生成示例文案</td><td>{@code MarketingController#regenerateCopy}</td></tr>
     *   <tr><td>image</td><td>商品图生成 prompt</td><td>图片生成相关接口</td></tr>
     *   <tr><td>copy</td><td>通用翻译/本地化</td><td>翻译相关接口</td></tr>
     * </table>
     *
     * @param category  prompt 使用场景分类（对应 DB 模板的 category 字段）
     * @param variables 运行时变量 Map，key 为模板中 {{varName}} 的 varName
     * @return 变量替换后的完整 prompt 文本，可直接传入 AI 模型
     */
    String resolvePrompt(String category, Map<String, String> variables);
}
