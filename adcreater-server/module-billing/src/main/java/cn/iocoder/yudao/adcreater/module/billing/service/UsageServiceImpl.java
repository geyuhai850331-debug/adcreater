package cn.iocoder.yudao.adcreater.module.billing.service;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.UsageRecordDO;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.UsageRecordMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
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
        record.setCreatedAt(LocalDateTime.now());
        mapper.insert(record);
    }
}
