package com.djb.module.ai.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.djb.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Prompt 模板 DO
 *
 * @author adcreater
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_prompt_template")
public class PromptTemplateDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 模板名称 */
    private String name;

    /** 模型名称 */
    private String modelName;

    /** 模板内容 (含 {{variable}} 占位符) */
    private String templateContent;

    /** 系统提示 */
    private String systemPrompt;

    /** 占位符变量列表 JSON */
    private String variables;

    /** 分类: copy/image/video/video_storyboard/video_keyframe/video_keyframe_image/video_keyframe_grid/digital_human */
    private String category;

    /** 是否启用 */
    private Boolean isEnabled;
}
