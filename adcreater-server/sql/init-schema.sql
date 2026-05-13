-- adcreater-server/sql/init-schema.sql
-- AdCreater 全部业务表

CREATE TABLE IF NOT EXISTS `ad_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `type` VARCHAR(32) NOT NULL COMMENT 'copy/image/video',
    `input_params` JSON COMMENT '请求参数',
    `result` JSON COMMENT '生成结果',
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/done/failed',
    `model_used` VARCHAR(64),
    `points_cost` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告制作任务';

CREATE TABLE IF NOT EXISTS `delivery_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `type` VARCHAR(32) NOT NULL COMMENT 'digital_human/platform',
    `platform` VARCHAR(32) COMMENT '目标平台',
    `input_params` JSON,
    `result` JSON,
    `status` VARCHAR(32) NOT NULL DEFAULT 'pending',
    `points_cost` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投放任务';

CREATE TABLE IF NOT EXISTS `template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL,
    `description` VARCHAR(512),
    `category` VARCHAR(32) NOT NULL COMMENT 'image/video',
    `thumbnail_url` VARCHAR(512),
    `status` VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT 'draft/published',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告模板';

CREATE TABLE IF NOT EXISTS `template_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `template_id` BIGINT NOT NULL,
    `version` INT NOT NULL DEFAULT 1,
    `file_url` VARCHAR(512) NOT NULL,
    `changelog` VARCHAR(512),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板版本';

CREATE TABLE IF NOT EXISTS `ai_model_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `model_name` VARCHAR(64) NOT NULL,
    `adapter_class` VARCHAR(255) NOT NULL COMMENT '全限定类名',
    `api_key` VARCHAR(512) NOT NULL COMMENT 'AES加密',
    `endpoint_url` VARCHAR(255),
    `is_enabled` TINYINT DEFAULT 1,
    `priority` INT DEFAULT 0,
    `sort_order` INT DEFAULT 0,
    `extra_config` JSON COMMENT '扩展配置(计费单价等)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置';

CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `model_config_id` BIGINT,
    `template_content` TEXT NOT NULL,
    `variables` JSON COMMENT '占位符列表及默认值',
    `category` VARCHAR(32) NOT NULL COMMENT 'copy/image/video/digital_human',
    `is_enabled` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板';

CREATE TABLE IF NOT EXISTS `user_points_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE,
    `balance` INT DEFAULT 0,
    `total_earned` INT DEFAULT 0,
    `total_spent` INT DEFAULT 0,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户点数账户';

CREATE TABLE IF NOT EXISTS `points_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `type` VARCHAR(16) NOT NULL COMMENT 'earn/consume/refund',
    `amount` INT NOT NULL,
    `balance_after` INT NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/confirmed/rolled_back',
    `biz_id` BIGINT COMMENT '关联任务ID',
    `remark` VARCHAR(255),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id_type` (`user_id`, `type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点数流水';

CREATE TABLE IF NOT EXISTS `usage_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `task_type` VARCHAR(32) NOT NULL,
    `model_used` VARCHAR(64),
    `points_consumed` INT DEFAULT 0,
    `input_tokens` INT DEFAULT 0,
    `output_tokens` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id_created` (`user_id`, `created_at`),
    INDEX `idx_model_used` (`model_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用量记录';
