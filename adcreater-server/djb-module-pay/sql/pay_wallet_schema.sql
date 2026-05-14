-- ========== 会员钱包表 ==========
DROP TABLE IF EXISTS `pay_wallet`;
CREATE TABLE `pay_wallet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '钱包账户编号',
  `user_id` BIGINT NOT NULL COMMENT '用户编号',
  `user_type` INT COMMENT '用户类型',
  `balance` INT NOT NULL COMMENT '钱包余额，单位：分',
  `freeze_price` INT DEFAULT 0 COMMENT '冻结金额，单位分',
  `total_expense` INT DEFAULT 0 COMMENT '累计支出，单位分',
  `total_recharge` INT DEFAULT 0 COMMENT '累计充值，单位分',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '最后更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包账户 DO';

-- ========== 会员钱包充值表 ==========
DROP TABLE IF EXISTS `pay_wallet_recharge`;
CREATE TABLE `pay_wallet_recharge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '充值编号',
  `wallet_id` BIGINT NOT NULL COMMENT '钱包编号',
  `total_price` INT NOT NULL COMMENT '实际到账金额',
  `pay_price` INT NOT NULL COMMENT '实际支付金额',
  `bonus_price` INT DEFAULT 0 COMMENT '赠送金额',
  `package_id` BIGINT DEFAULT NULL COMMENT '充值套餐编号',
  `pay_status` TINYINT(1) DEFAULT 0 COMMENT '是否已支付',
  `pay_order_id` BIGINT DEFAULT NULL COMMENT '支付订单编号',
  `pay_channel_code` VARCHAR(64) DEFAULT NULL COMMENT '支付渠道',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `pay_refund_id` BIGINT DEFAULT NULL COMMENT '支付退款单编号',
  `refund_total_price` INT DEFAULT 0 COMMENT '退款金额（含赠送）',
  `refund_pay_price` INT DEFAULT 0 COMMENT '退款支付金额',
  `refund_bonus_price` INT DEFAULT 0 COMMENT '退款赠送金额',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `refund_status` INT DEFAULT NULL COMMENT '退款状态',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '最后更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包充值 DO';

-- ========== 会员钱包流水表 ==========
DROP TABLE IF EXISTS `pay_wallet_transaction`;
CREATE TABLE `pay_wallet_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水编号',
  `no` VARCHAR(64) NOT NULL COMMENT '流水号',
  `wallet_id` BIGINT NOT NULL COMMENT '钱包编号',
  `biz_type` INT NOT NULL COMMENT '业务类型',
  `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务编号',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '流水说明',
  `price` INT NOT NULL COMMENT '交易金额',
  `balance` INT NOT NULL COMMENT '交易后余额',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '最后更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包流水 DO';

-- ========== 会员钱包充值套餐表 ==========
DROP TABLE IF EXISTS `pay_wallet_recharge_package`;
CREATE TABLE `pay_wallet_recharge_package` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐编号',
  `name` VARCHAR(64) NOT NULL COMMENT '套餐名',
  `pay_price` INT NOT NULL COMMENT '支付金额',
  `bonus_price` INT DEFAULT 0 COMMENT '赠送金额',
  `status` INT NOT NULL COMMENT '状态',
  `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '最后更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包充值套餐 DO';

