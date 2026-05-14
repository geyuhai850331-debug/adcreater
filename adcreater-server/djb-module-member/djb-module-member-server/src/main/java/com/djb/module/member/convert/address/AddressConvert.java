package com.djb.module.member.convert.address;

import com.djb.framework.ip.core.utils.AreaUtils;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.member.api.address.dto.MemberAddressRespDTO;
import com.djb.module.member.controller.admin.address.vo.AddressRespVO;
import com.djb.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import com.djb.module.member.controller.app.address.vo.AppAddressRespVO;
import com.djb.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import com.djb.module.member.dal.dataobject.address.MemberAddressDO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户收件地址 Convert
 *
 * @author djbadmin
 */
@Mapper
public interface AddressConvert {

    AddressConvert INSTANCE = Mappers.getMapper(AddressConvert.class);

    default MemberAddressDO convert(AppAddressCreateReqVO bean) {
        return BeanUtils.toBean(bean, MemberAddressDO.class);
    }

    default MemberAddressDO convert(AppAddressUpdateReqVO bean) {
        return BeanUtils.toBean(bean, MemberAddressDO.class);
    }

    default AppAddressRespVO convert(MemberAddressDO bean) {
        return BeanUtils.toBean(bean, AppAddressRespVO.class,
                respVO -> respVO.setAreaName(convertAreaIdToAreaName(bean.getAreaId().intValue())));
    }

    default List<AppAddressRespVO> convertList(List<MemberAddressDO> list) {
        return BeanUtils.toBean(list, AppAddressRespVO.class,
                respVO -> respVO.setAreaName(convertAreaIdToAreaName(respVO.getAreaId().intValue())));
    }

    default MemberAddressRespDTO convert02(MemberAddressDO bean) {
        return BeanUtils.toBean(bean, MemberAddressRespDTO.class);
    }

    @Named("convertAreaIdToAreaName")
    default String convertAreaIdToAreaName(Integer areaId) {
        return AreaUtils.format(areaId);
    }

    default List<AddressRespVO> convertList2(List<MemberAddressDO> list) {
        return BeanUtils.toBean(list, AddressRespVO.class);
    }

}
