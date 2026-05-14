package com.djb.module.system.service.logger;

import com.djb.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.djb.framework.common.pojo.PageResult;
import com.djb.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import com.djb.module.system.dal.dataobject.logger.OperateLogDO;
import com.djb.module.system.api.logger.dto.OperateLogPageReqDTO;

/**
 * 操作日志 Service 接口
 *
 * @author djbadmin
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 创建请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);

    /**
     * 获得操作日志
     *
     * @param id 编号
     * @return 操作日志
     */
    OperateLogDO getOperateLog(Long id);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqDTO pageReqVO);

}
