package com.djb.module.ai.service;

import com.djb.module.ai.adapter.AiModelAdapter;
import com.djb.module.ai.controller.admin.vo.*;
import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import com.djb.module.ai.dal.mapper.AiModelConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.util.object.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Base64;
import java.util.Map;

/**
 * AI 模型配置服务实现
 *
 * @author adcreater
 */
@Service
@Validated
@Slf4j
public class AiModelConfigServiceImpl implements AiModelConfigService {

    @Resource
    private AiModelConfigMapper mapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public Long create(AiModelConfigSaveReqVO reqVO) {
        AiModelConfigDO entity = BeanUtils.toBean(reqVO, AiModelConfigDO.class);
        entity.setApiKey(Base64.getEncoder().encodeToString(reqVO.getApiKey().getBytes()));
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(AiModelConfigSaveReqVO reqVO) {
        AiModelConfigDO entity = BeanUtils.toBean(reqVO, AiModelConfigDO.class);
        if (reqVO.getApiKey() != null && !reqVO.getApiKey().contains("****")) {
            entity.setApiKey(Base64.getEncoder().encodeToString(reqVO.getApiKey().getBytes()));
        }
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
        Map<String, AiModelAdapter> adapters = applicationContext.getBeansOfType(AiModelAdapter.class);
        AiModelAdapter adapter = adapters.values().stream()
            .filter(a -> a.getClass().getName().equals(config.getAdapterClass()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Adapter not found: " + config.getAdapterClass()));
        return adapter.validateConfig(config);
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
}
