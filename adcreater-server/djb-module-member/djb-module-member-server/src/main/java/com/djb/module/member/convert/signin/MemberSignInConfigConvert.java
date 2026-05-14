package com.djb.module.member.convert.signin;

import com.djb.module.member.controller.admin.signin.vo.config.MemberSignInConfigCreateReqVO;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.member.controller.admin.signin.vo.config.MemberSignInConfigRespVO;
import com.djb.module.member.controller.admin.signin.vo.config.MemberSignInConfigUpdateReqVO;
import com.djb.module.member.controller.app.signin.vo.config.AppMemberSignInConfigRespVO;
import com.djb.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 签到规则 Convert
 *
 * @author QingX
 */
@Mapper
public interface MemberSignInConfigConvert {

    MemberSignInConfigConvert INSTANCE = Mappers.getMapper(MemberSignInConfigConvert.class);

    default MemberSignInConfigDO convert(MemberSignInConfigCreateReqVO bean) {
        return BeanUtils.toBean(bean, MemberSignInConfigDO.class);
    }

    default MemberSignInConfigDO convert(MemberSignInConfigUpdateReqVO bean) {
        return BeanUtils.toBean(bean, MemberSignInConfigDO.class);
    }

    default MemberSignInConfigRespVO convert(MemberSignInConfigDO bean) {
        return BeanUtils.toBean(bean, MemberSignInConfigRespVO.class);
    }

    default List<MemberSignInConfigRespVO> convertList(List<MemberSignInConfigDO> list) {
        return BeanUtils.toBean(list, MemberSignInConfigRespVO.class);
    }

    default List<AppMemberSignInConfigRespVO> convertList02(List<MemberSignInConfigDO> list) {
        return BeanUtils.toBean(list, AppMemberSignInConfigRespVO.class);
    }

}
