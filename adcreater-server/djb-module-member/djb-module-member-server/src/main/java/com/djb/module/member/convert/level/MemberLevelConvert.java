package com.djb.module.member.convert.level;

import com.djb.module.member.api.level.dto.MemberLevelRespDTO;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.member.controller.admin.level.vo.level.MemberLevelCreateReqVO;
import com.djb.module.member.controller.admin.level.vo.level.MemberLevelRespVO;
import com.djb.module.member.controller.admin.level.vo.level.MemberLevelSimpleRespVO;
import com.djb.module.member.controller.admin.level.vo.level.MemberLevelUpdateReqVO;
import com.djb.module.member.controller.app.level.vo.level.AppMemberLevelRespVO;
import com.djb.module.member.dal.dataobject.level.MemberLevelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员等级 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberLevelConvert {

    MemberLevelConvert INSTANCE = Mappers.getMapper(MemberLevelConvert.class);

    default MemberLevelDO convert(MemberLevelCreateReqVO bean) {
        return BeanUtils.toBean(bean, MemberLevelDO.class);
    }

    default MemberLevelDO convert(MemberLevelUpdateReqVO bean) {
        return BeanUtils.toBean(bean, MemberLevelDO.class);
    }

    default MemberLevelRespVO convert(MemberLevelDO bean) {
        return BeanUtils.toBean(bean, MemberLevelRespVO.class);
    }

    default List<MemberLevelRespVO> convertList(List<MemberLevelDO> list) {
        return BeanUtils.toBean(list, MemberLevelRespVO.class);
    }

    default List<MemberLevelSimpleRespVO> convertSimpleList(List<MemberLevelDO> list) {
        return BeanUtils.toBean(list, MemberLevelSimpleRespVO.class);
    }

    default List<AppMemberLevelRespVO> convertList02(List<MemberLevelDO> list) {
        return BeanUtils.toBean(list, AppMemberLevelRespVO.class);
    }

    default MemberLevelRespDTO convert02(MemberLevelDO bean) {
        return BeanUtils.toBean(bean, MemberLevelRespDTO.class);
    }

}
