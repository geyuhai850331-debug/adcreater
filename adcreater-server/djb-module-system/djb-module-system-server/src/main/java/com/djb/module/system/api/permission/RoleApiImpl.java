package com.djb.module.system.api.permission;

import com.djb.framework.common.pojo.CommonResult;
import com.djb.module.system.service.permission.RoleService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static com.djb.framework.common.pojo.CommonResult.success;

@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class RoleApiImpl implements RoleApi {

    @Resource
    private RoleService roleService;

    @Override
    public CommonResult<Boolean> validRoleList(Collection<Long> ids) {
        roleService.validateRoleList(ids);
        return success(true);
    }
}
