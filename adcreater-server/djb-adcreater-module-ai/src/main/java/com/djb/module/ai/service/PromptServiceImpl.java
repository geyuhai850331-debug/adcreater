package com.djb.module.ai.service;

import com.djb.module.ai.controller.admin.vo.*;
import com.djb.module.ai.dal.dataobject.PromptTemplateDO;
import com.djb.module.ai.dal.mapper.PromptTemplateMapper;
import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.util.object.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Prompt 模板服务实现
 *
 * @author adcreater
 */
@Service
@Validated
@Slf4j
public class PromptServiceImpl implements PromptService {

    @Resource
    private PromptTemplateMapper mapper;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)(?:\\|([^}]*))?\\}\\}");

    @Override
    public Long create(PromptTemplateSaveReqVO reqVO) {
        PromptTemplateDO entity = BeanUtils.toBean(reqVO, PromptTemplateDO.class);
        entity.setVariables(extractVariables(reqVO.getTemplateContent()));
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(PromptTemplateSaveReqVO reqVO) {
        PromptTemplateDO entity = BeanUtils.toBean(reqVO, PromptTemplateDO.class);
        entity.setVariables(extractVariables(reqVO.getTemplateContent()));
        mapper.updateById(entity);
    }

    @Override
    public void updateStatus(Long id, Boolean isEnabled) {
        PromptTemplateDO entity = new PromptTemplateDO();
        entity.setId(id);
        entity.setIsEnabled(isEnabled);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PromptTemplateRespVO get(Long id) {
        return BeanUtils.toBean(mapper.selectById(id), PromptTemplateRespVO.class);
    }

    @Override
    public PageResult<PromptTemplateRespVO> getPage(PromptTemplatePageReqVO pageReqVO) {
        LambdaQueryWrapper<PromptTemplateDO> wrapper = new LambdaQueryWrapper<PromptTemplateDO>()
                .eq(pageReqVO.getIsEnabled() != null, PromptTemplateDO::getIsEnabled, pageReqVO.getIsEnabled())
                .orderByDesc(PromptTemplateDO::getId);
        if (pageReqVO.getCategory() != null) {
            wrapper.eq(PromptTemplateDO::getCategory, pageReqVO.getCategory());
        }
        if (pageReqVO.getName() != null) {
            wrapper.like(PromptTemplateDO::getName, pageReqVO.getName());
        }
        PageResult<PromptTemplateDO> pageResult = mapper.selectPage(pageReqVO, wrapper);
        PageResult<PromptTemplateRespVO> result = new PageResult<>();
        result.setList(BeanUtils.toBean(pageResult.getList(), PromptTemplateRespVO.class));
        result.setTotal(pageResult.getTotal());
        return result;
    }

    @Override
    public String resolvePrompt(String category, Map<String, String> variables) {
        PromptTemplateDO template = mapper.selectByCategoryAndEnabled(category);
        if (template == null) {
            return buildDefaultPrompt(category, variables);
        }
        String content = template.getTemplateContent();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultValue = matcher.group(2);
            String value = variables.getOrDefault(varName, defaultValue != null ? defaultValue : "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String extractVariables(String templateContent) {
        if (templateContent == null || templateContent.isBlank()) {
            return "[]";
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        Set<String> vars = new LinkedHashSet<>();
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars.stream().collect(Collectors.joining("\", \"", "[\"", "\"]"));
    }

    private String buildDefaultPrompt(String category, Map<String, String> variables) {
        String productName = variables.getOrDefault("product_name", "product");
        if ("image".equals(category)) {
            return String.format(
                "Professional e-commerce product photo of %s. Style: %s. Background: clean white studio. High resolution, commercial quality.",
                productName,
                variables.getOrDefault("style", "modern minimalist"));
        }
        if ("copy".equals(category)) {
            return String.format(
                "Translate and localize the following product information for the %s market in %s language. Original: %s. Selling points: %s.",
                variables.getOrDefault("target_market", "US"),
                variables.getOrDefault("target_lang", "English"),
                productName,
                variables.getOrDefault("selling_points", ""));
        }
        if ("marketing_copy".equals(category)) {
            return "你是一位专业的跨境电商产品文案本地化专家。\n\n" +
                "# 任务\n" +
                "根据用户提供的以下信息，**只生成最终的本地化产品文案**，不要输出任何其他内容。\n\n" +
                "# 输入信息\n" +
                "- 商品名称：" + productName + "\n" +
                "- 产品说明：" + variables.getOrDefault("product_description", "") + "\n" +
                "- 中文广告词：" + variables.getOrDefault("chinese_ad_copy", "") + "\n" +
                "- 目标市场：" + variables.getOrDefault("target_market", "USA") + "\n" +
                "- 合规风险等级：" + variables.getOrDefault("risk_level", "safe") + "\n" +
                "- 文化本土化建议：" + variables.getOrDefault("culture_notes", "") + "\n" +
                "- 核心营销策略：" + variables.getOrDefault("core_strategy", "") + "\n\n" +
                "# 输出要求\n" +
                "- 只输出一段纯文本产品文案（2-4句），用于商品页面展示。\n" +
                "- 严格基于原文进行本地化改写，保留所有核心语义。\n" +
                "- 不得添加输入中没有的卖点、功能、形容词。\n" +
                "- 允许调整语序、合并短句、使用当地惯用语、合规调整绝对化表达。\n" +
                "- 如有绝对化或敏感表达，改为合规说法但不改变核心意思。\n\n" +
                "# 开始执行\n" +
                "只输出最终产品文案，不要加引号、JSON 或解释。";
        }
        if ("marketing".equals(category)) {
            return "你是一位专业的跨境电商营销专家和内容策略师。\n\n" +
                "# 任务\n" +
                "分析提供的产品信息（图片描述、文字描述、中文广告推广词）、目标平台、目标市场，输出一个 JSON 对象。\n\n" +
                "# 输出 JSON 结构（键名固定，值中除示例广告词外均为分析结论）\n" +
                "{\n" +
                "  \"risk_level\": \"safe 或 warning\",\n" +
                "  \"culture_notes\": \"文化背景分析与本土化建议\",\n" +
                "  \"core_strategy\": \"核心营销策略描述\",\n" +
                "  \"example_ad_copy\": \"示例广告词（英文或目标市场语言），用户可直接使用或作为修改参考\"\n" +
                "}\n\n" +
                "# 分析要求\n" +
                "1. **合规性检查** → 输出 risk_level\n" +
                "   - 检查夸大功效、品牌侵权、违禁品类等。\n" +
                "   - safe = 无明显风险；warning = 存在需修改的内容。\n" +
                "2. **文化本土化** → 输出 culture_notes\n" +
                "   - 考虑幽默、禁忌、信任信号、计量单位、日期格式、季节性等。\n" +
                "3. **核心策略** → 输出 core_strategy\n" +
                "   - 选择最有效的单一营销心理触发点（痛点反差、价格锚点、从众心理、稀缺性、权威背书等）。\n" +
                "4. **示例广告词** → 输出 example_ad_copy\n" +
                "   - 基于原始情报（不可遗漏任何卖点）生成一段可直接展示的文案。\n" +
                "   - 必须符合平台政策和目标市场语言习惯，不是直接翻译中文。\n" +
                "   - 长度适中，适合产品页面展示。\n\n" +
                "# 输入信息\n" +
                "产品名称：" + productName + "\n" +
                "产品描述：" + variables.getOrDefault("product_description", "") + "\n" +
                "中文广告推广词：" + variables.getOrDefault("chinese_ad_copy", "") + "\n" +
                "目标市场：" + variables.getOrDefault("target_market", "USA") + "\n\n" +
                "# 重要约束\n" +
                "- 只输出 JSON，不要有任何额外文字。\n" +
                "- 所有值使用英文双引号，不要添加注释。\n" +
                "- 如果缺少某些信息，基于常识合理推断并注明（在 culture_notes 中说明）。";
        }
        return String.format("Generate %s content for: %s. Style: %s.",
            category,
            productName,
            variables.getOrDefault("style", "professional"));
    }

}
