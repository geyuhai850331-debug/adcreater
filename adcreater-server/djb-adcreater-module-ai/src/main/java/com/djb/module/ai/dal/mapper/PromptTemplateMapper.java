package com.djb.module.ai.dal.mapper;

import com.djb.module.ai.dal.dataobject.PromptTemplateDO;
import com.djb.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptTemplateMapper extends BaseMapperX<PromptTemplateDO> {
    default PromptTemplateDO selectByCategoryAndEnabled(String category) {
        return selectOne(
            new LambdaQueryWrapper<PromptTemplateDO>()
                .eq(PromptTemplateDO::getCategory, category)
                .eq(PromptTemplateDO::getIsEnabled, true)
                .last("LIMIT 1")
        );
    }
}
