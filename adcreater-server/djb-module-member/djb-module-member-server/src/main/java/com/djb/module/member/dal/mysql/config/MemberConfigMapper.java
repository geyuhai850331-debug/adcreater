package com.djb.module.member.dal.mysql.config;

import com.djb.framework.mybatis.core.mapper.BaseMapperX;
import com.djb.module.member.dal.dataobject.config.MemberConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分设置 Mapper
 *
 * @author QingX
 */
@Mapper
public interface MemberConfigMapper extends BaseMapperX<MemberConfigDO> {
}
