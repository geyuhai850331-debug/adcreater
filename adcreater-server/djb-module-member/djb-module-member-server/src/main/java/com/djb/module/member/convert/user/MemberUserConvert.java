package com.djb.module.member.convert.user;

import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.member.api.user.dto.MemberUserRespDTO;
import com.djb.module.member.controller.admin.user.vo.MemberUserRespVO;
import com.djb.module.member.controller.admin.user.vo.MemberUserUpdateReqVO;
import com.djb.module.member.controller.app.user.vo.AppMemberUserInfoRespVO;
import com.djb.module.member.convert.address.AddressConvert;
import com.djb.module.member.dal.dataobject.group.MemberGroupDO;
import com.djb.module.member.dal.dataobject.level.MemberLevelDO;
import com.djb.module.member.dal.dataobject.tag.MemberTagDO;
import com.djb.module.member.dal.dataobject.user.MemberUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static com.djb.framework.common.util.collection.CollectionUtils.convertList;
import static com.djb.framework.common.util.collection.CollectionUtils.convertMap;

@Mapper(uses = {AddressConvert.class})
public interface MemberUserConvert {

    MemberUserConvert INSTANCE = Mappers.getMapper(MemberUserConvert.class);

    default AppMemberUserInfoRespVO convert(MemberUserDO bean) {
        return BeanUtils.toBean(bean, AppMemberUserInfoRespVO.class);
    }


    default AppMemberUserInfoRespVO convert(MemberUserDO bean, MemberLevelDO level) {
        AppMemberUserInfoRespVO respVO = BeanUtils.toBean(bean, AppMemberUserInfoRespVO.class);
        if (level != null) {
            respVO.setLevel(BeanUtils.toBean(level, AppMemberUserInfoRespVO.Level.class));
        }
        return respVO;
    }

    default MemberUserRespDTO convert2(MemberUserDO bean) {
        return BeanUtils.toBean(bean, MemberUserRespDTO.class);
    }

    default List<MemberUserRespDTO> convertList2(List<MemberUserDO> list) {
        return BeanUtils.toBean(list, MemberUserRespDTO.class);
    }

    default MemberUserDO convert(MemberUserUpdateReqVO bean) {
        return BeanUtils.toBean(bean, MemberUserDO.class);
    }

    default PageResult<MemberUserRespVO> convertPage(PageResult<MemberUserDO> page) {
        return BeanUtils.toBean(page, MemberUserRespVO.class);
    }

    default MemberUserRespVO convert03(MemberUserDO bean) {
        return BeanUtils.toBean(bean, MemberUserRespVO.class);
    }

    default PageResult<MemberUserRespVO> convertPage(PageResult<MemberUserDO> pageResult,
                                                     List<MemberTagDO> tags,
                                                     List<MemberLevelDO> levels,
                                                     List<MemberGroupDO> groups) {
        PageResult<MemberUserRespVO> result = convertPage(pageResult);
        // 处理关联数据
        Map<Long, String> tagMap = convertMap(tags, MemberTagDO::getId, MemberTagDO::getName);
        Map<Long, String> levelMap = convertMap(levels, MemberLevelDO::getId, MemberLevelDO::getName);
        Map<Long, String> groupMap = convertMap(groups, MemberGroupDO::getId, MemberGroupDO::getName);
        // 填充关联数据
        result.getList().forEach(user -> {
            user.setTagNames(convertList(user.getTagIds(), tagMap::get));
            user.setLevelName(levelMap.get(user.getLevelId()));
            user.setGroupName(groupMap.get(user.getGroupId()));
        });
        return result;
    }

}
