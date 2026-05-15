package com.djb.module.ad.config;

import com.djb.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.djb.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.djb.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.djb.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.djb.framework.common.biz.system.dict.DictDataCommonApi;
import com.djb.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.djb.framework.common.biz.system.logger.OperateLogCommonApi;
import com.djb.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.djb.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.djb.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.djb.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.djb.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.djb.framework.common.biz.system.permission.PermissionCommonApi;
import com.djb.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.djb.framework.common.biz.system.tenant.TenantCommonApi;
import com.djb.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * Mock 系统 RPC API — 模型管理/系统模块未上线时的临时桩。
 * 覆盖全部 7 个 CommonApi 接口，后续系统模块完整启动后自动失效（@ConditionalOnMissingBean）。
 *
 * @author adcreater
 */
@Configuration
@Slf4j
public class MockSystemApiConfig {

    // ==================== PermissionCommonApi ====================

    @Bean
    @ConditionalOnMissingBean(PermissionCommonApi.class)
    public PermissionCommonApi mockPermissionApi() {
        log.warn(">>> Mock PermissionCommonApi — 权限校验全放行 <<<");
        return new PermissionCommonApi() {
            @Override
            public CommonResult<Boolean> hasAnyPermissions(Long userId, String... permissions) {
                return success(true);
            }
            @Override
            public CommonResult<Boolean> hasAnyRoles(Long userId, String... roles) {
                return success(true);
            }
            @Override
            public CommonResult<DeptDataPermissionRespDTO> getDeptDataPermission(Long userId) {
                DeptDataPermissionRespDTO dto = new DeptDataPermissionRespDTO();
                dto.setAll(true);
                dto.setDeptIds(Collections.emptySet());
                return success(dto);
            }
        };
    }

    // ==================== OAuth2TokenCommonApi ====================

    @Bean
    @ConditionalOnMissingBean(OAuth2TokenCommonApi.class)
    public OAuth2TokenCommonApi mockOAuth2TokenApi() {
        log.warn(">>> Mock OAuth2TokenCommonApi — Token 校验全放行 <<<");
        return new OAuth2TokenCommonApi() {
            @Override
            public CommonResult<OAuth2AccessTokenRespDTO> createAccessToken(OAuth2AccessTokenCreateReqDTO reqDTO) {
                OAuth2AccessTokenRespDTO resp = new OAuth2AccessTokenRespDTO();
                resp.setAccessToken("mock-access-token");
                resp.setRefreshToken("mock-refresh-token");
                resp.setUserId(reqDTO.getUserId());
                resp.setUserType(reqDTO.getUserType());
                resp.setExpiresTime(LocalDateTime.now().plusHours(2));
                return success(resp);
            }
            @Override
            public CommonResult<OAuth2AccessTokenCheckRespDTO> checkAccessToken(String accessToken) {
                OAuth2AccessTokenCheckRespDTO resp = new OAuth2AccessTokenCheckRespDTO();
                resp.setUserId(1L);
                resp.setUserType(1);
                resp.setTenantId(1L);
                resp.setScopes(Collections.emptyList());
                resp.setExpiresTime(LocalDateTime.now().plusHours(2));
                return success(resp);
            }
            @Override
            public CommonResult<OAuth2AccessTokenRespDTO> removeAccessToken(String accessToken) {
                return success(new OAuth2AccessTokenRespDTO());
            }
            @Override
            public CommonResult<OAuth2AccessTokenRespDTO> refreshAccessToken(String refreshToken, String clientId) {
                OAuth2AccessTokenRespDTO resp = new OAuth2AccessTokenRespDTO();
                resp.setAccessToken("mock-access-token-refreshed");
                resp.setRefreshToken("mock-refresh-token");
                resp.setUserId(1L);
                resp.setUserType(1);
                resp.setExpiresTime(LocalDateTime.now().plusHours(2));
                return success(resp);
            }
        };
    }

    // ==================== ApiErrorLogCommonApi ====================

    @Bean
    @ConditionalOnMissingBean(ApiErrorLogCommonApi.class)
    public ApiErrorLogCommonApi mockApiErrorLogApi() {
        log.warn(">>> Mock ApiErrorLogCommonApi — 错误日志静默丢弃 <<<");
        return createDTO -> success(true);
    }

    // ==================== ApiAccessLogCommonApi ====================

    @Bean
    @ConditionalOnMissingBean(ApiAccessLogCommonApi.class)
    public ApiAccessLogCommonApi mockApiAccessLogApi() {
        log.warn(">>> Mock ApiAccessLogCommonApi — 访问日志静默丢弃 <<<");
        return createDTO -> success(true);
    }

    // ==================== TenantCommonApi ====================

    @Bean
    @ConditionalOnMissingBean(TenantCommonApi.class)
    public TenantCommonApi mockTenantApi() {
        log.warn(">>> Mock TenantCommonApi — 租户校验全放行 <<<");
        return new TenantCommonApi() {
            @Override
            public CommonResult<List<Long>> getTenantIdList() {
                return success(List.of(1L));
            }
            @Override
            public CommonResult<Boolean> validTenant(Long id) {
                return success(true);
            }
        };
    }

    // ==================== DictDataCommonApi ====================

    @Bean
    @ConditionalOnMissingBean(DictDataCommonApi.class)
    public DictDataCommonApi mockDictDataApi() {
        log.warn(">>> Mock DictDataCommonApi — 字典数据返回空列表 <<<");
        return dictType -> success(Collections.emptyList());
    }

    // ==================== OperateLogCommonApi ====================

    @Bean
    @ConditionalOnMissingBean(OperateLogCommonApi.class)
    public OperateLogCommonApi mockOperateLogApi() {
        log.warn(">>> Mock OperateLogCommonApi — 操作日志静默丢弃 <<<");
        return createReqDTO -> success(true);
    }
}
