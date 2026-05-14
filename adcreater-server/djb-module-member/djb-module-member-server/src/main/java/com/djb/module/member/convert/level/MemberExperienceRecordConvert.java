package com.djb.module.member.convert.level;

import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.member.controller.admin.level.vo.experience.MemberExperienceRecordRespVO;
import com.djb.module.member.controller.app.level.vo.experience.AppMemberExperienceRecordRespVO;
import com.djb.module.member.dal.dataobject.level.MemberExperienceRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员经验记录 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberExperienceRecordConvert {

    MemberExperienceRecordConvert INSTANCE = Mappers.getMapper(MemberExperienceRecordConvert.class);

    default MemberExperienceRecordRespVO convert(MemberExperienceRecordDO bean) {
        return BeanUtils.toBean(bean, MemberExperienceRecordRespVO.class);
    }

    default List<MemberExperienceRecordRespVO> convertList(List<MemberExperienceRecordDO> list) {
        return BeanUtils.toBean(list, MemberExperienceRecordRespVO.class);
    }

    default PageResult<MemberExperienceRecordRespVO> convertPage(PageResult<MemberExperienceRecordDO> page) {
        return BeanUtils.toBean(page, MemberExperienceRecordRespVO.class);
    }

    MemberExperienceRecordDO convert(Long userId, Integer experience, Integer totalExperience,
                                     String bizId, Integer bizType,
                                     String title, String description);

    default PageResult<AppMemberExperienceRecordRespVO> convertPage02(PageResult<MemberExperienceRecordDO> page) {
        return BeanUtils.toBean(page, AppMemberExperienceRecordRespVO.class);
    }

}
