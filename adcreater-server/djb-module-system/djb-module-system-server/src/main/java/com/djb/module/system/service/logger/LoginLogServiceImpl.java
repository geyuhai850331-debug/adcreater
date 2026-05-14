package com.djb.module.system.service.logger;

import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import com.djb.module.system.dal.dataobject.logger.LoginLogDO;
import com.djb.module.system.dal.mysql.logger.LoginLogMapper;
import com.djb.module.system.api.logger.dto.LoginLogCreateReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 登录日志 Service 实现
 */
@Service
@Validated
public class LoginLogServiceImpl implements LoginLogService {

    @Resource
    private LoginLogMapper loginLogMapper;

    @Override
    public LoginLogDO getLoginLog(Long id) {
        return loginLogMapper.selectById(id);
    }

    @Override
    public PageResult<LoginLogDO> getLoginLogPage(LoginLogPageReqVO pageReqVO) {
        return loginLogMapper.selectPage(pageReqVO);
    }

    @Override
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        LoginLogDO loginLog = BeanUtils.toBean(reqDTO, LoginLogDO.class);
        loginLogMapper.insert(loginLog);
    }

}
