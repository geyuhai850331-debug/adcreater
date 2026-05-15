ALTER TABLE `ad_model_config`
    ADD COLUMN `category` VARCHAR(32) NOT NULL DEFAULT 'copy' COMMENT '分类: copy/image/video/digital_human' AFTER `model_name`;
