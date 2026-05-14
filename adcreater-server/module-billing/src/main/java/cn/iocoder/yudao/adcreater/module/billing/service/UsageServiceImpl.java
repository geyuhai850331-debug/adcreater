package cn.iocoder.yudao.adcreater.module.billing.service;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.UsageRecordDO;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.UsageRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 用量记录服务实现
 *
 * @author adcreater
 */
@Service
@Validated
@Slf4j
public class UsageServiceImpl implements UsageService {

    @Resource
    private UsageRecordMapper mapper;

    @Override
    public void record(Long userId, String taskType, String modelUsed,
                       int pointsConsumed, int inputTokens, int outputTokens) {
        UsageRecordDO record = new UsageRecordDO();
        record.setUserId(userId);
        record.setTaskType(taskType);
        record.setModelUsed(modelUsed);
        record.setPointsConsumed(pointsConsumed);
        record.setInputTokens(inputTokens);
        record.setOutputTokens(outputTokens);
        // createTime auto-filled by DefaultDBFieldHandler
        mapper.insert(record);
    }
}
