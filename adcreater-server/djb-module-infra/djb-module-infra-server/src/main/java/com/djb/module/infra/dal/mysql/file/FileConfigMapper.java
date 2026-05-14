package com.djb.module.infra.dal.mysql.file;

import com.djb.framework.common.pojo.PageResult;
import com.djb.framework.mybatis.core.mapper.BaseMapperX;
import com.djb.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.djb.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO;
import com.djb.module.infra.dal.dataobject.file.FileConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileConfigMapper extends BaseMapperX<FileConfigDO> {

    default PageResult<FileConfigDO> selectPage(FileConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileConfigDO>()
                .likeIfPresent(FileConfigDO::getName, reqVO.getName())
                .eqIfPresent(FileConfigDO::getStorage, reqVO.getStorage())
                .betweenIfPresent(FileConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileConfigDO::getId));
    }

    default FileConfigDO selectByMaster() {
        return selectOne(FileConfigDO::getMaster, true);
    }

}
