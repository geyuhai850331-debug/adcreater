package com.djb.module.member.service.auth;

import com.djb.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.djb.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.tenant.core.context.TenantContextHolder;
import com.djb.framework.test.core.ut.BaseMockitoUnitTest;
import com.djb.module.member.controller.app.auth.vo.AppAuthRegisterReqVO;
import com.djb.module.member.dal.dataobject.user.MemberUserDO;
import com.djb.module.member.service.user.MemberUserService;
import com.djb.module.system.api.logger.LoginLogApi;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MemberAuthServiceImplTest extends BaseMockitoUnitTest {

    @Mock
    private MemberUserService userService;
    @Mock
    private OAuth2TokenCommonApi oauth2TokenApi;
    @Mock
    private LoginLogApi loginLogApi;

    @InjectMocks
    private MemberAuthServiceImpl authService;

    @Test
    void register_shouldCreateTokenUnderUserTenant() {
        TenantContextHolder.clear();
        AppAuthRegisterReqVO reqVO = AppAuthRegisterReqVO.builder()
                .mobile("15601691300")
                .password("123456")
                .build();
        MemberUserDO user = MemberUserDO.builder()
                .id(1L)
                .mobile(reqVO.getMobile())
                .password("encoded")
                .build();
        user.setTenantId(1L);
        when(userService.registerUser(any(), any(), any(), any())).thenReturn(user);
        when(loginLogApi.createLoginLog(any())).thenReturn(CommonResult.success(true));
        when(oauth2TokenApi.createAccessToken(any())).thenAnswer(invocation -> {
            assertEquals(1L, TenantContextHolder.getTenantId());
            return CommonResult.success(new OAuth2AccessTokenRespDTO());
        });

        authService.register(reqVO);

        assertNull(TenantContextHolder.getTenantId());
    }
}
