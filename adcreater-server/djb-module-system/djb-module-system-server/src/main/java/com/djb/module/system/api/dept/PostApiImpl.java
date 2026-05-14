package com.djb.module.system.api.dept;

import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.common.util.object.BeanUtils;
import com.djb.module.system.dal.dataobject.dept.PostDO;
import com.djb.module.system.service.dept.PostService;
import com.djb.module.system.api.dept.dto.PostRespDTO;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static com.djb.framework.common.pojo.CommonResult.success;

@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class PostApiImpl implements PostApi {

    @Resource
    private PostService postService;

    @Override
    public CommonResult<Boolean> validPostList(Collection<Long> ids) {
        postService.validatePostList(ids);
        return success(true);
    }

    @Override
    public CommonResult<List<PostRespDTO>> getPostList(Collection<Long> ids) {
        List<PostDO> list = postService.getPostList(ids);
        return success(BeanUtils.toBean(list, PostRespDTO.class));
    }

}
