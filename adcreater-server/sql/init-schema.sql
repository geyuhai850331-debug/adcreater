-- ====================================================================
-- AdCreater 全量业务表 DDL (DJB 规范)
--
-- 规范要求:
--   - 表名: ad_ 前缀 (DJB 模块前缀约定)
--   - 引擎: InnoDB, 字符集: utf8mb4_unicode_ci
--   - 每表含: tenant_id, creator, create_time, updater, update_time, deleted
--   - 每列含中文 COMMENT
--   - id 统一 BIGINT AUTO_INCREMENT
-- ====================================================================

-- ── 广告制作任务 ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `type` VARCHAR(32) NOT NULL COMMENT '任务类型: copy/image/video',
    `input_params` JSON COMMENT '请求参数',
    `result` JSON COMMENT '生成结果',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/done/failed',
    `model_used` VARCHAR(64) COMMENT '使用的 AI 模型',
    `points_cost` INT DEFAULT 0 COMMENT '消耗点数',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='广告制作任务';

-- ── 投放任务 ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_delivery_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `type` VARCHAR(32) NOT NULL COMMENT '投放类型: digital_human/platform',
    `platform` VARCHAR(32) COMMENT '目标平台',
    `input_params` JSON COMMENT '请求参数',
    `result` JSON COMMENT '投放结果',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/done/failed',
    `points_cost` INT DEFAULT 0 COMMENT '消耗点数',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='投放任务';

-- ── 广告模板 ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(512) COMMENT '模板描述',
    `category` VARCHAR(32) NOT NULL COMMENT '分类: image/video',
    `thumbnail_url` VARCHAR(512) COMMENT '缩略图 URL',
    `status` VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/published',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci  COMMENT='广告模板';

-- ── 模板版本 ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_template_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `template_id` BIGINT NOT NULL COMMENT '关联模板 ID',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `file_url` VARCHAR(512) NOT NULL COMMENT '模板文件 URL',
    `changelog` VARCHAR(512) COMMENT '变更日志',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='模板版本';

-- ── AI 模型配置 ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_model_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `model_name` VARCHAR(64) NOT NULL COMMENT '模型名称',
    `adapter_class` VARCHAR(255) NOT NULL COMMENT '适配器全限定类名',
    `api_key` VARCHAR(512) NOT NULL COMMENT 'API Key (AES 加密存储)',
    `endpoint_url` VARCHAR(255) COMMENT 'API 端点 URL',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0=禁用, 1=启用',
    `priority` INT DEFAULT 0 COMMENT '优先级 (降级用, 越小越优先)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `extra_config` JSON COMMENT '扩展配置 JSON (计费单价等)',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='AI 模型配置';

-- ── Prompt 模板 ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `name` VARCHAR(64) NOT NULL COMMENT '模板名称',
    `model_config_id` BIGINT COMMENT '关联模型配置 ID',
    `template_content` TEXT NOT NULL COMMENT '模板内容 (含 {{variable}} 占位符)',
    `variables` JSON COMMENT '占位符列表及默认值',
    `category` VARCHAR(32) NOT NULL COMMENT '分类: copy/image/video/video_storyboard/video_keyframe/video_keyframe_image/video_keyframe_grid/digital_human',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0=禁用, 1=启用',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='Prompt 模板';

-- ── 用户点数账户 ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_user_points_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID (关联 system_users.id)',
    `balance` INT DEFAULT 0 COMMENT '当前余额',
    `total_earned` INT DEFAULT 0 COMMENT '累计充值',
    `total_spent` INT DEFAULT 0 COMMENT '累计消费',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='用户点数账户';

-- ── 点数流水 ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_points_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `type` VARCHAR(16) NOT NULL COMMENT '类型: earn/consume/refund',
    `amount` INT NOT NULL COMMENT '金额',
    `balance_after` INT NOT NULL COMMENT '操作后余额',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/confirmed/rolled_back',
    `biz_id` BIGINT COMMENT '关联业务 ID',
    `remark` VARCHAR(255) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_user_id_type` (`user_id`, `type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='点数流水 (不可变, 只 INSERT)';

-- ── 用量记录 ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `ad_usage_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `task_type` VARCHAR(32) NOT NULL COMMENT '任务类型',
    `model_used` VARCHAR(64) COMMENT '使用的 AI 模型',
    `points_consumed` INT DEFAULT 0 COMMENT '消耗点数',
    `input_tokens` INT DEFAULT 0 COMMENT '输入 Token 数',
    `output_tokens` INT DEFAULT 0 COMMENT '输出 Token 数',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_user_id_create_time` (`user_id`, `create_time`),
    INDEX `idx_model_used` (`model_used`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='用量记录';
