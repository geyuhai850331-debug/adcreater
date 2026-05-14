package com.djb.module.template.dal.mapper;

import com.djb.module.template.dal.dataobject.TemplateVersionDO;
import com.djb.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TemplateVersionMapper extends BaseMapperX<TemplateVersionDO> {

    default TemplateVersionDO selectLatestByTemplateId(Long templateId) {
        return selectOne(new LambdaQueryWrapper<TemplateVersionDO>()
                .eq(TemplateVersionDO::getTemplateId, templateId)
                .orderByDesc(TemplateVersionDO::getVersion)
                .last("LIMIT 1"));
    }
}
