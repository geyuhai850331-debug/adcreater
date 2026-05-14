package com.djb.module.billing.dal.mapper;

import com.djb.module.billing.dal.dataobject.UsageRecordDO;
import com.djb.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsageRecordMapper extends BaseMapperX<UsageRecordDO> {
}
