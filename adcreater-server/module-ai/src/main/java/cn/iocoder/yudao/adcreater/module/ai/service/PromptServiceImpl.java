package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.PromptTemplateDO;
import cn.iocoder.yudao.adcreater.module.ai.dal.mapper.PromptTemplateMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PromptTemplateRespVO get(Long id) {
        return BeanUtils.toBean(mapper.selectById(id), PromptTemplateRespVO.class);
    }

    @Override
    public PageResult<PromptTemplateRespVO> getPage(PromptTemplatePageReqVO pageReqVO) {
        LambdaQueryWrapper<PromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
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
        return String.format("Generate %s content for: %s. Style: %s.",
            category,
            productName,
            variables.getOrDefault("style", "professional"));
    }
}
