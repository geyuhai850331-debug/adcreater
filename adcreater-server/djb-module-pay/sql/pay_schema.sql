-- ========================== TABLE: pay_order ==========================
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付订单编号，主键',
    `app_id` BIGINT NOT NULL COMMENT '应用编号',
    `channel_id` BIGINT NOT NULL COMMENT '支付渠道编号',
    `merchant_order_id` VARCHAR(64) NOT NULL COMMENT '商户订单编号',
    `subject` VARCHAR(128) NOT NULL COMMENT '商品标题',
    `body` VARCHAR(255) COMMENT '商品描述',
    `price` INT NOT NULL COMMENT '支付金额，单位：分',
    `channel_fee_rate` DOUBLE COMMENT '渠道手续费，单位：百分比',
    `channel_fee_price` INT COMMENT '渠道手续金额，单位：分',
    `status` INT NOT NULL COMMENT '订单状态（枚举）',
    `user_ip` VARCHAR(45) COMMENT '用户 IP',
    `expire_time` DATETIME COMMENT '订单失效时间',
    `user_id` BIGINT COMMENT '用户编号',
    `user_type` INT COMMENT '用户类型',
    `notify_status` INT COMMENT '通知状态',
    `success_time` DATETIME COMMENT '支付成功时间',
    `extension_id` BIGINT COMMENT '扩展单编号',
    `no` VARCHAR(128) COMMENT '支付成功的外部订单号',
    `channel_user_id` VARCHAR(128) COMMENT '渠道用户编号',
    `channel_order_no` VARCHAR(128) COMMENT '渠道订单号',
    `refund_status` INT COMMENT '退款状态',
    `refund_price` INT COMMENT '退款总金额，单位：分',
    `notify_url` VARCHAR(255) COMMENT '支付结果回调地址',
    `creator` VARCHAR(64) COMMENT '创建者',
    `updater` VARCHAR(64) COMMENT '更新者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_time` DATETIME COMMENT '最后更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单 DO';

-- ========================== TABLE: pay_order_extension ==========================
DROP TABLE IF EXISTS `pay_order_extension`;
CREATE TABLE `pay_order_extension` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单扩展编号，主键',
    `order_id` BIGINT NOT NULL COMMENT '订单编号，关联 pay_order.id',
    `status` INT NOT NULL COMMENT '状态',
    `no` VARCHAR(128) COMMENT '第三方流水号',
    `channel_extras` JSON COMMENT '渠道扩展参数，JSON 格式',
    `creator` VARCHAR(64) COMMENT '创建者',
    `updater` VARCHAR(64) COMMENT '更新者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_time` DATETIME COMMENT '最后更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单扩展 DO';

-- ========================== TABLE: pay_refund ==========================
DROP TABLE IF EXISTS `pay_refund`;
CREATE TABLE `pay_refund` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付退款单主键',
    `order_id` BIGINT NOT NULL COMMENT '支付订单编号',
    `order_no` VARCHAR(128) COMMENT '支付订单编号',
    `user_id` BIGINT COMMENT '用户编号',
    `user_type` INT COMMENT '用户类型',
    `app_id` BIGINT NOT NULL COMMENT '应用编号',
    `channel_id` BIGINT NOT NULL COMMENT '支付渠道编号',
    `merchant_order_id` VARCHAR(64) COMMENT '商户订单编号',
    `merchant_refund_id` VARCHAR(64) NOT NULL COMMENT '商户退款单编号',
    `status` INT NOT NULL COMMENT '退款状态',
    `pay_price` INT COMMENT '支付金额，单位：分',
    `refund_price` INT COMMENT '退款金额，单位：分',
    `price` INT NOT NULL COMMENT '退款金额，单位：分',
    `reason` VARCHAR(255) COMMENT '退款原因',
    `notify_url` VARCHAR(255) COMMENT '退款结果回调地址',
    `success_time` DATETIME COMMENT '退款成功时间',
    `user_ip` VARCHAR(45) COMMENT '用户 IP',
    `channel_refund_no` VARCHAR(128) COMMENT '渠道退款单号',
    `channel_error_code` VARCHAR(64) COMMENT '调用渠道的错误码',
    `channel_error_msg` VARCHAR(255) COMMENT '调用渠道报错时，错误信息',
    `channel_notify_data` TEXT COMMENT '支付渠道的同步/异步通知的内容',
    `creator` VARCHAR(64) COMMENT '创建者',
    `updater` VARCHAR(64) COMMENT '更新者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_time` DATETIME COMMENT '最后更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付退款单 DO';

-- ========================== TABLE: pay_notify_task ==========================
DROP TABLE IF EXISTS `pay_notify_task`;
CREATE TABLE `pay_notify_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知任务编号',
    `type` INT NOT NULL COMMENT '通知类型（订单、退款等）',
    `data_id` BIGINT NOT NULL COMMENT '关联的数据编号',
    `status` INT NOT NULL COMMENT '通知状态',
    `next_time` DATETIME COMMENT '下次通知时间',
    `notify_url` VARCHAR(255) COMMENT '回调地址',
    `notify_times` INT DEFAULT 0 COMMENT '已通知次数',
    `max_notify_times` INT DEFAULT 0 COMMENT '最大通知次数',
    `last_notify_result` VARCHAR(255) COMMENT '最后一次通知结果',
    `creator` VARCHAR(64) COMMENT '创建者',
    `updater` VARCHAR(64) COMMENT '更新者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_time` DATETIME COMMENT '最后更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付通知任务 DO';

-- ========================== TABLE: pay_notify_log ==========================
DROP TABLE IF EXISTS `pay_notify_log`;
CREATE TABLE `pay_notify_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知日志编号',
    `task_id` BIGINT NOT NULL COMMENT '通知任务编号，关联 pay_notify_task.id',
    `data_id` BIGINT NOT NULL COMMENT '关联的数据编号',
    `status` INT NOT NULL COMMENT '通知状态',
    `response` VARCHAR(255) COMMENT '通知响应内容',
    `notify_time` DATETIME COMMENT '通知时间',
    `creator` VARCHAR(64) COMMENT '创建者',
    `updater` VARCHAR(64) COMMENT '更新者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_time` DATETIME COMMENT '最后更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付通知日志 DO';

-- ========================== TABLE: pay_transfer ==========================
DROP TABLE IF EXISTS `pay_transfer`;
CREATE TABLE `pay_transfer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '转账单编号',
    `app_id` BIGINT NOT NULL COMMENT '应用编号',
    `channel_id` BIGINT NOT NULL COMMENT '支付渠道编号',
    `merchant_transfer_id` VARCHAR(64) NOT NULL COMMENT '商户转账单编号',
    `price` INT NOT NULL COMMENT '转账金额，单位：分',
    `status` INT NOT NULL COMMENT '转账状态',
    `user_id` BIGINT NOT NULL COMMENT '转账人编号',
    `user_name` VARCHAR(128) COMMENT '转账人名称',
    `user_openid` VARCHAR(128) COMMENT '转账人 OpenID',
    `channel_extras` JSON COMMENT '渠道扩展参数，JSON 格式',
    `remark` VARCHAR(255) COMMENT '转账备注',
    `success_time` DATETIME COMMENT '转账成功时间',
    `channel_transfer_no` VARCHAR(128) COMMENT '渠道转账单号',
    `notify_url` VARCHAR(255) COMMENT '转账结果回调地址',
    `creator` VARCHAR(64) COMMENT '创建者',
    `updater` VARCHAR(64) COMMENT '更新者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_time` DATETIME COMMENT '最后更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付转账单 DO';

-- ========================== TABLE: pay_wallet ==========================
DROP TABLE IF EXISTS `pay_wallet`;
CREATE TABLE `pay_wallet` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '钱包账户编号',
    `user_id` BIGINT NOT NULL COMMENT '用户编号',
    `user_type`  VARCHAR(64) COMMENT '用户类型',
    `balance` INT NOT NULL COMMENT '钱包余额，单位：分',
    `freeze_balance` INT DEFAULT 0 COMMENT '冻结余额，单位：分',
    `freeze_price` INT DEFAULT 0 COMMENT '冻结金额，单位分',
    `total_expense` INT DEFAULT 0 COMMENT '累计支出，单位分',
    `total_recharge` INT DEFAULT 0 COMMENT '累计充值，单位分',
    `creator` VARCHAR(64) COMMENT '创建者',
    `updater` VARCHAR(64) COMMENT '更新者',
    `create_time` DATETIME COMMENT '创建时间',
    `update_time` DATETIME COMMENT '最后更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包账户 DO';
