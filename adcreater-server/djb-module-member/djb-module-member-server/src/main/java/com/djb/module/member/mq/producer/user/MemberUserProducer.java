package com.djb.module.member.mq.producer.user;

import com.djb.module.member.api.message.user.MemberUserCreateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 会员用户 Producer
 *
 * @author owen
 */
@Slf4j
@Component
public class MemberUserProducer {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 发送 {@link MemberUserCreateMessage} 消息
     *
     * @param userId 用户编号
     */
    public void sendUserCreateMessage(Long userId) {
        MemberUserCreateMessage message = new MemberUserCreateMessage();
        message.setUserId(userId);
        applicationContext.publishEvent(message);
    }

}
