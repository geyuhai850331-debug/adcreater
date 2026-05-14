package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.adapter.AiModelAdapter;
import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.AiModelConfigDO;
import cn.iocoder.yudao.adcreater.module.ai.dal.mapper.AiModelConfigMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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
        PageResult<AiModelConfigDO> pageResult = mapper.selectPage(pageReqVO);
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
