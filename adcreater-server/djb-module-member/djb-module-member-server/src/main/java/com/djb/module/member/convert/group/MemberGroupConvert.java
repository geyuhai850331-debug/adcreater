package com.djb.module.member.convert.group;

import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.member.controller.admin.group.vo.MemberGroupCreateReqVO;
import com.djb.module.member.controller.admin.group.vo.MemberGroupRespVO;
import com.djb.module.member.controller.admin.group.vo.MemberGroupSimpleRespVO;
import com.djb.module.member.controller.admin.group.vo.MemberGroupUpdateReqVO;
import com.djb.module.member.dal.dataobject.group.MemberGroupDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户分组 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberGroupConvert {

    MemberGroupConvert INSTANCE = Mappers.getMapper(MemberGroupConvert.class);

    default MemberGroupDO convert(MemberGroupCreateReqVO bean) {
        return BeanUtils.toBean(bean, MemberGroupDO.class);
    }

    default MemberGroupDO convert(MemberGroupUpdateReqVO bean) {
        return BeanUtils.toBean(bean, MemberGroupDO.class);
    }

    default MemberGroupRespVO convert(MemberGroupDO bean) {
        return BeanUtils.toBean(bean, MemberGroupRespVO.class);
    }

    default List<MemberGroupRespVO> convertList(List<MemberGroupDO> list) {
        return BeanUtils.toBean(list, MemberGroupRespVO.class);
    }

    default PageResult<MemberGroupRespVO> convertPage(PageResult<MemberGroupDO> page) {
        return BeanUtils.toBean(page, MemberGroupRespVO.class);
    }

    default List<MemberGroupSimpleRespVO> convertSimpleList(List<MemberGroupDO> list) {
        return BeanUtils.toBean(list, MemberGroupSimpleRespVO.class);
    }
}
