package com.djb.module.system.dal.mysql.mail;

import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.mybatis.core.mapper.BaseMapperX;
import com.djb.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.djb.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import com.djb.module.system.dal.dataobject.mail.MailAccountDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailAccountMapper extends BaseMapperX<MailAccountDO> {

    default PageResult<MailAccountDO> selectPage(MailAccountPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<MailAccountDO>()
                .likeIfPresent(MailAccountDO::getMail, pageReqVO.getMail())
                .likeIfPresent(MailAccountDO::getUsername , pageReqVO.getUsername())
                .orderByDesc(MailAccountDO::getId));
    }

}
