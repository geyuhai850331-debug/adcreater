package com.djb.module.ai.service;

import com.djb.module.ai.adapter.AiModelAdapter;
import com.djb.module.ai.adapter.AiModelAdapterResolver;
import com.djb.module.ai.controller.admin.vo.*;
import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import com.djb.module.ai.dal.mapper.AiModelConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djb.framework.common.exception.util.ServiceExceptionUtil;
import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.util.object.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AI 模型配置服务实现
 *
 * @author adcreater
 */
@Service
@Validated
@Slf4j
public class AiModelConfigServiceImpl implements AiModelConfigService {

    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[ -/]*[@-~]");
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");

    @Resource
    private AiModelConfigMapper mapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public Long create(AiModelConfigSaveReqVO reqVO) {
        AiModelConfigDO entity = BeanUtils.toBean(reqVO, AiModelConfigDO.class);
        entity.setApiKey(Base64.getEncoder().encodeToString(reqVO.getApiKey().getBytes()));
        entity.setExtraConfig(normalizeExtraConfig(reqVO.getExtraConfig()));
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(AiModelConfigSaveReqVO reqVO) {
        AiModelConfigDO entity = BeanUtils.toBean(reqVO, AiModelConfigDO.class);
        if (reqVO.getApiKey() != null && !reqVO.getApiKey().contains("****")) {
            entity.setApiKey(Base64.getEncoder().encodeToString(reqVO.getApiKey().getBytes()));
        }
        entity.setExtraConfig(normalizeExtraConfig(reqVO.getExtraConfig()));
        mapper.updateById(entity);
    }

    @Override
    public void updateStatus(Long id, Boolean isEnabled) {
        AiModelConfigDO entity = new AiModelConfigDO();
        entity.setId(id);
        entity.setIsEnabled(isEnabled);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public AiModelConfigRespVO get(Long id) {
        AiModelConfigDO entity = mapper.selectById(id);
        AiModelConfigRespVO resp = BeanUtils.toBean(entity, AiModelConfigRespVO.class);
        resp.setApiKey(maskApiKey(entity.getApiKey()));
        return resp;
    }

    @Override
    public PageResult<AiModelConfigRespVO> getPage(AiModelConfigPageReqVO pageReqVO) {
        LambdaQueryWrapper<AiModelConfigDO> wrapper = new LambdaQueryWrapper<AiModelConfigDO>()
                .like(pageReqVO.getModelName() != null && !pageReqVO.getModelName().isEmpty(),
                        AiModelConfigDO::getModelName, pageReqVO.getModelName())
                .eq(pageReqVO.getIsEnabled() != null, AiModelConfigDO::getIsEnabled, pageReqVO.getIsEnabled())
                .orderByDesc(AiModelConfigDO::getId);
        PageResult<AiModelConfigDO> pageResult = mapper.selectPage(pageReqVO, wrapper);
        PageResult<AiModelConfigRespVO> result = new PageResult<>();
        result.setList(BeanUtils.toBean(pageResult.getList(), AiModelConfigRespVO.class));
        result.setTotal(pageResult.getTotal());
        return result;
    }

    @Override
    public boolean testConnection(Long id) {
        AiModelConfigDO config = mapper.selectById(id);
        if (config == null) {
            throw ServiceExceptionUtil.invalidParamException("模型配置不存在");
        }
        return validateConnection(buildTestConfig(config));
    }

    @Override
    public boolean testConnection(AiModelConfigTestReqVO reqVO) {
        AiModelConfigDO config = buildTestConfig(reqVO);
        return validateConnection(config);
    }

    private boolean validateConnection(AiModelConfigDO config) {
        AiModelAdapter adapter = resolveAdapter(config.getAdapterClass());
        try {
            boolean connected = adapter.validateConfig(config);
            if (!connected) {
                throw ServiceExceptionUtil.invalidParamException("连接测试失败，请检查接口地址、API Key 和适配器配置");
            }
            return true;
        } catch (Exception e) {
            log.warn("[testConnection][模型 {} 测试失败]", config.getModelName(), e);
            throw ServiceExceptionUtil.invalidParamException("连接测试失败：{}", extractErrorMessage(e));
        }
    }

    private String maskApiKey(String encodedKey) {
        try {
            String decoded = new String(Base64.getDecoder().decode(encodedKey));
            if (decoded.length() <= 8) return "****";
            return decoded.substring(0, 3) + "****" + decoded.substring(decoded.length() - 4);
        } catch (Exception e) {
            return "****";
        }
    }

    private String normalizeExtraConfig(String extraConfig) {
        if (extraConfig == null || extraConfig.isBlank()) {
            return null;
        }
        return extraConfig;
    }

    private AiModelConfigDO buildTestConfig(AiModelConfigDO source) {
        AiModelConfigDO config = new AiModelConfigDO();
        config.setId(source.getId());
        config.setModelName(source.getModelName());
        config.setAdapterClass(source.getAdapterClass());
        config.setApiKey(source.getApiKey());
        config.setEndpointUrl(source.getEndpointUrl());
        config.setIsEnabled(source.getIsEnabled());
        config.setPriority(source.getPriority());
        config.setSortOrder(source.getSortOrder());
        config.setExtraConfig(normalizeExtraConfig(source.getExtraConfig()));
        return config;
    }

    private AiModelConfigDO buildTestConfig(AiModelConfigTestReqVO reqVO) {
        AiModelConfigDO config = reqVO.getId() != null
                ? mapper.selectById(reqVO.getId())
                : new AiModelConfigDO();
        if (reqVO.getId() != null && config == null) {
            throw ServiceExceptionUtil.invalidParamException("模型配置不存在");
        }
        String normalizedModelName = normalizeInput(reqVO.getModelName());
        if (normalizedModelName != null && !normalizedModelName.isBlank()) {
            config.setModelName(normalizedModelName);
        }
        String normalizedAdapterClass = normalizeInput(reqVO.getAdapterClass());
        if (normalizedAdapterClass != null && !normalizedAdapterClass.isBlank()) {
            config.setAdapterClass(normalizedAdapterClass);
        }
        if (reqVO.getEndpointUrl() != null) {
            config.setEndpointUrl(normalizeInput(reqVO.getEndpointUrl()));
        }
        if (reqVO.getIsEnabled() != null) {
            config.setIsEnabled(reqVO.getIsEnabled());
        }
        if (reqVO.getPriority() != null) {
            config.setPriority(reqVO.getPriority());
        }
        if (reqVO.getSortOrder() != null) {
            config.setSortOrder(reqVO.getSortOrder());
        }
        if (reqVO.getApiKey() != null && !reqVO.getApiKey().isBlank()) {
            config.setApiKey(Base64.getEncoder().encodeToString(reqVO.getApiKey().getBytes(StandardCharsets.UTF_8)));
        }
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw ServiceExceptionUtil.invalidParamException("API Key 不能为空");
        }
        config.setExtraConfig(normalizeExtraConfig(reqVO.getExtraConfig()));
        return config;
    }

    private AiModelAdapter resolveAdapter(String adapterClass) {
        Map<String, AiModelAdapter> adapters = applicationContext.getBeansOfType(AiModelAdapter.class);
        return AiModelAdapterResolver.resolve(adapters, adapterClass)
                .orElseThrow(() -> ServiceExceptionUtil.invalidParamException("找不到适配器：{}", adapterClass));
    }

    private String extractErrorMessage(Exception e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return (message == null || message.isBlank()) ? e.getClass().getSimpleName() : message;
    }

    private String normalizeInput(String value) {
        if (value == null) {
            return null;
        }
        String normalized = ANSI_ESCAPE_PATTERN.matcher(value).replaceAll("");
        normalized = CONTROL_CHAR_PATTERN.matcher(normalized).replaceAll("");
        return normalized.trim();
    }
}
