package com.djb.module.member.service.config;

import com.djb.framework.common.util.collection.CollectionUtils;
import com.djb.module.member.controller.admin.config.vo.MemberConfigSaveReqVO;
import com.djb.module.member.convert.config.MemberConfigConvert;
import com.djb.module.member.dal.dataobject.config.MemberConfigDO;
import com.djb.module.member.dal.mysql.config.MemberConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 会员配置 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberConfigServiceImpl implements MemberConfigService {

    @Resource
    private MemberConfigMapper memberConfigMapper;

    @Override
    public void saveConfig(MemberConfigSaveReqVO saveReqVO) {
        // 存在，则进行更新
        MemberConfigDO dbConfig = getConfig();
        if (dbConfig != null) {
            MemberConfigDO updateObj = MemberConfigConvert.INSTANCE.convert(saveReqVO);
            updateObj.setId(dbConfig.getId());
            memberConfigMapper.updateById(updateObj);
            return;
        }
        // 不存在，则进行插入
        memberConfigMapper.insert(MemberConfigConvert.INSTANCE.convert(saveReqVO));
    }

    @Override
    public MemberConfigDO getConfig() {
        List<MemberConfigDO> list = memberConfigMapper.selectList();
        return CollectionUtils.getFirst(list);
    }

}
