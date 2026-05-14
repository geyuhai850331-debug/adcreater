-- ---------------------------
-- Table structure for member_sign_in_record
-- ---------------------------
DROP TABLE IF EXISTS `member_sign_in_record`;
CREATE TABLE `member_sign_in_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '签到用户',
  `day` INT COMMENT '第几天签到',
  `point` INT COMMENT '签到的积分',
  `experience` INT COMMENT '签到的经验',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ---------------------------
-- Table structure for member_tag
-- ---------------------------
DROP TABLE IF EXISTS `member_tag`;
CREATE TABLE `member_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` VARCHAR(255) COMMENT '标签名称',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员标签表';

-- ---------------------------
-- Table structure for member_address
-- ---------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '用户编号',
  `name` VARCHAR(255) COMMENT '收件人名称',
  `mobile` VARCHAR(50) COMMENT '手机号',
  `area_id` BIGINT COMMENT '地区编号',
  `detail_address` VARCHAR(512) COMMENT '收件详细地址',
  `default_status` TINYINT(1) DEFAULT 0 COMMENT '是否默认，true-默认地址',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收件地址表';

-- ---------------------------
-- Table structure for member_user
-- ---------------------------
DROP TABLE IF EXISTS `member_user`;
CREATE TABLE `member_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `mobile` VARCHAR(32) COMMENT '手机',
  `password` VARCHAR(255) COMMENT '加密后的密码',
  `status` INT COMMENT '账号状态',
  `register_ip` VARCHAR(45) COMMENT '注册IP',
  `register_terminal` INT COMMENT '注册终端',
  `login_ip` VARCHAR(45) COMMENT '最后登录IP',
  `login_date` DATETIME COMMENT '最后登录时间',
  `nickname` VARCHAR(64) COMMENT '用户昵称',
  `avatar` VARCHAR(255) COMMENT '用户头像',
  `name` VARCHAR(64) COMMENT '真实名字',
  `sex` INT COMMENT '性别',
  `birthday` DATETIME COMMENT '出生日期',
  `area_id` INT COMMENT '所在地区',
  `mark` VARCHAR(255) COMMENT '用户备注',
  `point` INT COMMENT '积分',
  `tag_ids` TEXT COMMENT '会员标签列表，逗号分隔，List<Long>',
  `level_id` BIGINT COMMENT '会员级别编号',
  `experience` INT COMMENT '会员经验',
  `group_id` BIGINT COMMENT '用户分组编号',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员用户表';

-- ---------------------------
-- Table structure for member_level_record
-- ---------------------------
DROP TABLE IF EXISTS `member_level_record`;
CREATE TABLE `member_level_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '用户编号',
  `level_id` BIGINT COMMENT '等级编号',
  `level` INT COMMENT '会员等级',
  `discount_percent` INT COMMENT '享受折扣',
  `experience` INT COMMENT '升级经验',
  `user_experience` INT COMMENT '会员此时的经验',
  `remark` VARCHAR(255) COMMENT '备注',
  `description` VARCHAR(255) COMMENT '描述',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级记录表';

-- ---------------------------
-- Table structure for member_config
-- ---------------------------
DROP TABLE IF EXISTS `member_config`;
CREATE TABLE `member_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `point_trade_deduct_enable` TINYINT(1) DEFAULT 0 COMMENT '积分抵扣开关',
  `point_trade_deduct_unit_price` INT COMMENT '积分抵扣，单位：分',
  `point_trade_deduct_max_price` INT COMMENT '积分抵扣最大值',
  `point_trade_give_point` INT COMMENT '1元送多少分',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员配置表';

-- ---------------------------
-- Table structure for member_level
-- ---------------------------
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` VARCHAR(64) COMMENT '等级名称',
  `level` INT COMMENT '等级',
  `experience` INT COMMENT '升级经验',
  `discount_percent` INT COMMENT '享受折扣',
  `icon` VARCHAR(128) COMMENT '等级图标',
  `background_url` VARCHAR(255) COMMENT '等级背景图',
  `status` INT COMMENT '状态',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- ---------------------------
-- Table structure for member_experience_record
-- ---------------------------
DROP TABLE IF EXISTS `member_experience_record`;
CREATE TABLE `member_experience_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '用户编号',
  `biz_type` INT COMMENT '业务类型',
  `biz_id` VARCHAR(64) COMMENT '业务编号',
  `title` VARCHAR(255) COMMENT '标题',
  `description` VARCHAR(255) COMMENT '描述',
  `experience` INT COMMENT '经验',
  `total_experience` INT COMMENT '变更后的经验',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员经验记录表';

-- ---------------------------
-- Table structure for member_sign_in_config
-- ---------------------------
DROP TABLE IF EXISTS `member_sign_in_config`;
CREATE TABLE `member_sign_in_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则自增主键',
  `day` INT COMMENT '签到第 x 天',
  `point` INT COMMENT '奖励积分',
  `experience` INT COMMENT '奖励经验',
  `status` INT COMMENT '状态',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到规则表';

-- ---------------------------
-- Table structure for member_point_record
-- ---------------------------
DROP TABLE IF EXISTS `member_point_record`;
CREATE TABLE `member_point_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` BIGINT COMMENT '用户编号',
  `biz_id` VARCHAR(128) COMMENT '业务编码',
  `biz_type` INT COMMENT '业务类型',
  `title` VARCHAR(255) COMMENT '积分标题',
  `description` VARCHAR(255) COMMENT '积分描述',
  `point` INT COMMENT '变动积分，正数为获得，负数为消耗',
  `total_point` INT COMMENT '变动后的积分',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分记录表';

-- ---------------------------
-- Table structure for member_group
-- ---------------------------
DROP TABLE IF EXISTS `member_group`;
CREATE TABLE `member_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` VARCHAR(64) COMMENT '名称',
  `remark` VARCHAR(255) COMMENT '备注',
  `status` INT COMMENT '状态',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `tenant_id` BIGINT COMMENT '租户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户分组表';

-- ---------------------------
-- Table structure for member_sign_in_record
-- ---------------------------
DROP TABLE IF EXISTS `member_sign_in_record`;
CREATE TABLE `member_sign_in_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '签到用户',
  `day` INT COMMENT '第几天签到',
  `point` INT COMMENT '签到的积分',
  `experience` INT COMMENT '签到的经验',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ---------------------------
-- Table structure for member_tag
-- ---------------------------
DROP TABLE IF EXISTS `member_tag`;
CREATE TABLE `member_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` VARCHAR(255) COMMENT '标签名称',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员标签表';

-- ---------------------------
-- Table structure for member_address
-- ---------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '用户编号',
  `name` VARCHAR(255) COMMENT '收件人名称',
  `mobile` VARCHAR(50) COMMENT '手机号',
  `area_id` BIGINT COMMENT '地区编号',
  `detail_address` VARCHAR(512) COMMENT '收件详细地址',
  `default_status` TINYINT(1) DEFAULT 0 COMMENT '是否默认，true-默认地址',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收件地址表';

-- ---------------------------
-- Table structure for member_user
-- ---------------------------
DROP TABLE IF EXISTS `member_user`;
CREATE TABLE `member_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `mobile` VARCHAR(32) COMMENT '手机',
  `password` VARCHAR(255) COMMENT '加密后的密码',
  `status` INT COMMENT '账号状态',
  `register_ip` VARCHAR(45) COMMENT '注册IP',
  `register_terminal` INT COMMENT '注册终端',
  `login_ip` VARCHAR(45) COMMENT '最后登录IP',
  `login_date` DATETIME COMMENT '最后登录时间',
  `nickname` VARCHAR(64) COMMENT '用户昵称',
  `avatar` VARCHAR(255) COMMENT '用户头像',
  `name` VARCHAR(64) COMMENT '真实名字',
  `sex` INT COMMENT '性别',
  `birthday` DATETIME COMMENT '出生日期',
  `area_id` INT COMMENT '所在地区',
  `mark` VARCHAR(255) COMMENT '用户备注',
  `point` INT COMMENT '积分',
  `tag_ids` TEXT COMMENT '会员标签列表，逗号分隔，List<Long>',
  `level_id` BIGINT COMMENT '会员级别编号',
  `experience` INT COMMENT '会员经验',
  `group_id` BIGINT COMMENT '用户分组编号',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
    `creator` BIGINT COMMENT '创建人',
    `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员用户表';

-- ---------------------------
-- Table structure for member_level_record
-- ---------------------------
DROP TABLE IF EXISTS `member_level_record`;
CREATE TABLE `member_level_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '用户编号',
  `level_id` BIGINT COMMENT '等级编号',
  `level` INT COMMENT '会员等级',
  `discount_percent` INT COMMENT '享受折扣',
  `experience` INT COMMENT '升级经验',
  `user_experience` INT COMMENT '会员此时的经验',
  `remark` VARCHAR(255) COMMENT '备注',
  `description` VARCHAR(255) COMMENT '描述',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级记录表';

-- ---------------------------
-- Table structure for member_config
-- ---------------------------
DROP TABLE IF EXISTS `member_config`;
CREATE TABLE `member_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `point_trade_deduct_enable` TINYINT(1) DEFAULT 0 COMMENT '积分抵扣开关',
  `point_trade_deduct_unit_price` INT COMMENT '积分抵扣，单位：分',
  `point_trade_deduct_max_price` INT COMMENT '积分抵扣最大值',
  `point_trade_give_point` INT COMMENT '1元送多少分',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',


  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员配置表';

-- ---------------------------
-- Table structure for member_level
-- ---------------------------
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` VARCHAR(64) COMMENT '等级名称',
  `level` INT COMMENT '等级',
  `experience` INT COMMENT '升级经验',
  `discount_percent` INT COMMENT '享受折扣',
  `icon` VARCHAR(128) COMMENT '等级图标',
  `background_url` VARCHAR(255) COMMENT '等级背景图',
  `status` INT COMMENT '状态',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- ---------------------------
-- Table structure for member_experience_record
-- ---------------------------
DROP TABLE IF EXISTS `member_experience_record`;
CREATE TABLE `member_experience_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` BIGINT COMMENT '用户编号',
  `biz_type` INT COMMENT '业务类型',
  `biz_id` VARCHAR(64) COMMENT '业务编号',
  `title` VARCHAR(255) COMMENT '标题',
  `description` VARCHAR(255) COMMENT '描述',
  `experience` INT COMMENT '经验',
  `total_experience` INT COMMENT '变更后的经验',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员经验记录表';

-- ---------------------------
-- Table structure for member_sign_in_config
-- ---------------------------
DROP TABLE IF EXISTS `member_sign_in_config`;
CREATE TABLE `member_sign_in_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则自增主键',
  `day` INT COMMENT '签到第 x 天',
  `point` INT COMMENT '奖励积分',
  `experience` INT COMMENT '奖励经验',
  `status` INT COMMENT '状态',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到规则表';

-- ---------------------------
-- Table structure for member_point_record
-- ---------------------------
DROP TABLE IF EXISTS `member_point_record`;
CREATE TABLE `member_point_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` BIGINT COMMENT '用户编号',
  `biz_id` VARCHAR(128) COMMENT '业务编码',
  `biz_type` INT COMMENT '业务类型',
  `title` VARCHAR(255) COMMENT '积分标题',
  `description` VARCHAR(255) COMMENT '积分描述',
  `point` INT COMMENT '变动积分，正数为获得，负数为消耗',
  `total_point` INT COMMENT '变动后的积分',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分记录表';

-- ---------------------------
-- Table structure for member_group
-- ---------------------------
DROP TABLE IF EXISTS `member_group`;
CREATE TABLE `member_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` VARCHAR(64) COMMENT '名称',
  `remark` VARCHAR(255) COMMENT '备注',
  `status` INT COMMENT '状态',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME COMMENT '更新时间',
  `creator` BIGINT COMMENT '创建人',
  `updater` BIGINT COMMENT '更新人',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
  `tenant_id` BIGINT COMMENT '租户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户分组表';

