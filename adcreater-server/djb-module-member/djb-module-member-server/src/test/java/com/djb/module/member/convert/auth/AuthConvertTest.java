package com.djb.module.member.convert.auth;

import com.djb.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.djb.framework.test.core.ut.BaseMockitoUnitTest;
import com.djb.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthConvertTest extends BaseMockitoUnitTest {

    @Test
    void convert_shouldMapOAuthTokenFields() {
        OAuth2AccessTokenRespDTO dto = new OAuth2AccessTokenRespDTO();
        dto.setUserId(1L);
        dto.setAccessToken("access-token");
        dto.setRefreshToken("refresh-token");
        dto.setExpiresTime(LocalDateTime.of(2026, 5, 18, 13, 0));

        AppAuthLoginRespVO respVO = AuthConvert.INSTANCE.convert(dto, "openid-1");

        assertEquals(1L, respVO.getUserId());
        assertEquals("access-token", respVO.getAccessToken());
        assertEquals("refresh-token", respVO.getRefreshToken());
        assertEquals(LocalDateTime.of(2026, 5, 18, 13, 0), respVO.getExpiresTime());
        assertEquals("openid-1", respVO.getOpenid());
    }
}
