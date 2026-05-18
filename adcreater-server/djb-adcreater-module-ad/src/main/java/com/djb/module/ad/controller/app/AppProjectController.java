package com.djb.module.ad.controller.app;

import com.djb.framework.common.pojo.CommonResult;
import com.djb.module.ad.controller.app.vo.AppProjectRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static com.djb.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 最近项目")
@RestController
@RequestMapping("/ad/projects")
@Validated
public class AppProjectController {

    @GetMapping("/recent")
    @Operation(summary = "查询最近项目")
    public CommonResult<List<AppProjectRespVO>> getRecentProjects() {
        return success(List.of(
                buildProject("夏季新款运动鞋推广", "image", "已完成", LocalDateTime.now().minusDays(2)),
                buildProject("智能手表促销视频", "video", "已完成", LocalDateTime.now().minusDays(5)),
                buildProject("美妆礼盒品牌广告", "image", "已完成", LocalDateTime.now().minusDays(7)),
                buildProject("无线耳机社交素材", "image", "进行中", LocalDateTime.now()),
                buildProject("家居用品展示视频", "video", "失败", LocalDateTime.now().minusDays(3))
        ));
    }

    private static AppProjectRespVO buildProject(String name, String type, String status, LocalDateTime createdAt) {
        AppProjectRespVO respVO = new AppProjectRespVO();
        respVO.setName(name);
        respVO.setType(type);
        respVO.setStatus(status);
        respVO.setCreatedAt(createdAt);
        return respVO;
    }
}
