-- Add the missing model_name column for legacy ad_prompt_template tables.
-- Run this once on existing environments that were initialized before
-- model_name was added to PromptTemplateDO.

ALTER TABLE `ad_prompt_template`
    ADD COLUMN `model_name` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '模型名称' AFTER `name`;
