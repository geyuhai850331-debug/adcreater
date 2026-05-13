# AdCreater Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a full-stack cross-border e-commerce ad creation platform with Electron+Vue3 desktop client, Vue3 admin web, and yudao-cloud Spring Boot backend.

**Architecture:** Monorepo with yudao-cloud backend (5 new Maven modules: module-ad, module-delivery, module-template, module-ai, module-billing), Electron+Vue3 desktop client, and Vue3 admin web. AI models via third-party APIs through Adapter pattern. Assets stored locally on client.

**Tech Stack:** Spring Boot (yudao-cloud), MyBatis-Plus, MySQL, Redis, Vue3+Element Plus+Vite, Electron

---

## Phase 1: Backend Infrastructure

### Task 1: Project Scaffold & Database Init

**Files:**
- Create: `adcreater-server/pom.xml`
- Create: `adcreater-server/adcreater-server-common/pom.xml`
- Create: `adcreater-server/module-ai/pom.xml`
- Create: `adcreater-server/module-billing/pom.xml`
- Create: `adcreater-server/module-ad/pom.xml`
- Create: `adcreater-server/module-delivery/pom.xml`
- Create: `adcreater-server/module-template/pom.xml`
- Create: `adcreater-server/sql/init-schema.sql`

- [ ] **Step 1: Create root pom.xml with yudao-cloud parent and 5 new modules**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>cn.iocoder.yudao</groupId>
        <artifactId>yudao</artifactId>
        <version>${revision}</version>
    </parent>
    <artifactId>adcreater-server</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>adcreater-server-common</module>
        <module>module-ai</module>
        <module>module-billing</module>
        <module>module-ad</module>
        <module>module-delivery</module>
        <module>module-template</module>
    </modules>
</project>
```

- [ ] **Step 2: Create SQL schema file with all 9 tables**

```sql
-- adcreater-server/sql/init-schema.sql

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
```

- [ ] **Step 3: Create module pom.xml files for each of the 5 modules**

Each module pom.xml depends on `adcreater-server-common` and the relevant yudao framework starters.

```xml
<!-- module-ai/pom.xml example -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>cn.iocoder.yudao</groupId>
        <artifactId>adcreater-server</artifactId>
        <version>${revision}</version>
    </parent>
    <artifactId>adcreater-module-ai</artifactId>
    <dependencies>
        <dependency>
            <groupId>cn.iocoder.yudao</groupId>
            <artifactId>adcreater-server-common</artifactId>
            <version>${revision}</version>
        </dependency>
        <dependency>
            <groupId>cn.iocoder.yudao</groupId>
            <artifactId>yudao-spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.iocoder.yudao</groupId>
            <artifactId>yudao-spring-boot-starter-mybatis</artifactId>
        </dependency>
    </dependencies>
</project>
```

Repeat for module-billing, module-ad, module-delivery, module-template with same pattern.

- [ ] **Step 4: Run schema init against local MySQL**

Run: `mysql -u root -p adcreater < adcreater-server/sql/init-schema.sql`
Expected: All 9 tables created successfully.

- [ ] **Step 5: Verify Maven build compiles**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 2: Shared Common Layer

**Files:**
- Create: `adcreater-server/adcreater-server-common/src/main/java/cn/iocoder/yudao/adcreater/common/enums/TaskTypeEnum.java`
- Create: `adcreater-server/adcreater-server-common/src/main/java/cn/iocoder/yudao/adcreater/common/enums/TaskStatusEnum.java`
- Create: `adcreater-server/adcreater-server-common/src/main/java/cn/iocoder/yudao/adcreater/common/enums/TemplateCategoryEnum.java`
- Create: `adcreater-server/adcreater-server-common/src/main/java/cn/iocoder/yudao/adcreater/common/enums/PromptCategoryEnum.java`
- Create: `adcreater-server/adcreater-server-common/src/main/java/cn/iocoder/yudao/adcreater/common/exception/AiCallException.java`
- Create: `adcreater-server/adcreater-server-common/src/main/java/cn/iocoder/yudao/adcreater/common/exception/InsufficientPointsException.java`

- [ ] **Step 1: Write TaskTypeEnum**

```java
package cn.iocoder.yudao.adcreater.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {
    COPY("copy", "文案翻译"),
    IMAGE("image", "图片生成"),
    VIDEO("video", "视频生成"),
    DIGITAL_HUMAN("digital_human", "数字人"),
    PLATFORM_EXPORT("platform_export", "平台导出");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 2: Write TaskStatusEnum**

```java
package cn.iocoder.yudao.adcreater.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    DONE("done", "已完成"),
    FAILED("failed", "失败");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 3: Write PromptCategoryEnum**

```java
package cn.iocoder.yudao.adcreater.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromptCategoryEnum {
    COPY("copy", "文案"),
    IMAGE("image", "图片"),
    VIDEO("video", "视频"),
    DIGITAL_HUMAN("digital_human", "数字人");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 4: Write InsufficientPointsException**

```java
package cn.iocoder.yudao.adcreater.common.exception;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;

public class InsufficientPointsException extends ServiceException {
    public InsufficientPointsException(int required, int available) {
        super(GlobalErrorCodeConstants.BAD_REQUEST.getCode(),
              String.format("点数不足: 需要 %d, 当前余额 %d", required, available));
    }
}
```

- [ ] **Step 5: Write AiCallException**

```java
package cn.iocoder.yudao.adcreater.common.exception;

import cn.iocoder.yudao.framework.common.exception.ServiceException;

public class AiCallException extends ServiceException {
    public AiCallException(String modelName, String detail) {
        super(500, String.format("AI模型调用失败 [%s]: %s", modelName, detail));
    }
}
```

- [ ] **Step 6: Verify Maven compiles**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Phase 2: AI Module (module-ai)

### Task 3: AiModelAdapter Interface & DO/VO

**Files:**
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/adapter/AiModelAdapter.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/adapter/AiRequest.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/adapter/AiResult.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/dal/dataobject/AiModelConfigDO.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/dal/dataobject/PromptTemplateDO.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/dal/mapper/AiModelConfigMapper.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/dal/mapper/PromptTemplateMapper.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/controller/admin/vo/AiModelConfigSaveReqVO.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/controller/admin/vo/AiModelConfigRespVO.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/controller/admin/vo/PromptTemplateSaveReqVO.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/controller/admin/vo/PromptTemplateRespVO.java`

- [ ] **Step 1: Write AiModelAdapter interface**

```java
package cn.iocoder.yudao.adcreater.module.ai.adapter;

public interface AiModelAdapter {
    /**
     * 调用 AI 模型
     * @param request 统一请求对象 (prompt + 参数)
     * @param config  模型配置 (apiKey, endpoint 等)
     * @return 统一结果
     */
    AiResult call(AiRequest request, AiModelConfigDO config);

    /**
     * 验证模型配置是否可用 (连接测试)
     */
    boolean validateConfig(AiModelConfigDO config);

    /**
     * 预估本次调用消耗的点数
     */
    int estimateCost(AiRequest request);
}
```

- [ ] **Step 2: Write AiRequest (unified request object)**

```java
package cn.iocoder.yudao.adcreater.module.ai.adapter;

import lombok.Data;
import java.util.Map;

@Data
public class AiRequest {
    private String prompt;
    private String negativePrompt;
    private Integer width;
    private Integer height;
    private String style;
    private Integer duration;  // video duration in seconds
    private String model;       // specific model override
    private Map<String, Object> extraParams;  // adapter-specific params
}
```

- [ ] **Step 3: Write AiResult (unified response)**

```java
package cn.iocoder.yudao.adcreater.module.ai.adapter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiResult {
    private boolean success;
    private String url;           // 生成的资源临时 URL
    private String revisedPrompt;  // 模型修改后的 prompt
    private Integer inputTokens;
    private Integer outputTokens;
    private String errorMessage;
}
```

- [ ] **Step 4: Write AiModelConfigDO**

```java
package cn.iocoder.yudao.adcreater.module.ai.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model_config")
public class AiModelConfigDO extends BaseDO {
    @TableId
    private Long id;
    private String modelName;
    private String adapterClass;
    private String apiKey;
    private String endpointUrl;
    private Boolean isEnabled;
    private Integer priority;
    private Integer sortOrder;
    private String extraConfig;  // JSON string
}
```

- [ ] **Step 5: Write PromptTemplateDO**

```java
package cn.iocoder.yudao.adcreater.module.ai.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prompt_template")
public class PromptTemplateDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private Long modelConfigId;
    private String templateContent;
    private String variables;   // JSON string: {"product_name":"","style":"modern minimalist"}
    private String category;
    private Boolean isEnabled;
}
```

- [ ] **Step 6: Write MyBatis-Plus Mappers**

```java
package cn.iocoder.yudao.adcreater.module.ai.dal.mapper;

import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.AiModelConfigDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiModelConfigMapper extends BaseMapperX<AiModelConfigDO> {
}
```

```java
package cn.iocoder.yudao.adcreater.module.ai.dal.mapper;

import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.PromptTemplateDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptTemplateMapper extends BaseMapperX<PromptTemplateDO> {
    default PromptTemplateDO selectByCategoryAndEnabled(String category) {
        return selectOne(
            new LambdaQueryWrapper<PromptTemplateDO>()
                .eq(PromptTemplateDO::getCategory, category)
                .eq(PromptTemplateDO::getIsEnabled, true)
                .last("LIMIT 1")
        );
    }
}
```

- [ ] **Step 7: Write VO classes for admin CRUD**

```java
package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AiModelConfigSaveReqVO {
    private Long id;
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    @NotBlank(message = "适配器类名不能为空")
    private String adapterClass;
    @NotBlank(message = "API Key 不能为空")
    private String apiKey;
    private String endpointUrl;
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
    private Integer priority;
    private Integer sortOrder;
    private String extraConfig;
}
```

```java
package cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo;

import lombok.Data;

@Data
public class AiModelConfigRespVO {
    private Long id;
    private String modelName;
    private String adapterClass;
    private String apiKey;       // 脱敏展示: sk-****xxxx
    private String endpointUrl;
    private Boolean isEnabled;
    private Integer priority;
    private Integer sortOrder;
    private String extraConfig;
}
```

- [ ] **Step 8: Verify Maven compiles**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 4: AiModelConfig Admin CRUD API

**Files:**
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/service/AiModelConfigService.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/service/AiModelConfigServiceImpl.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/controller/admin/AiModelConfigController.java`

- [ ] **Step 1: Write AiModelConfigService interface**

```java
package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

public interface AiModelConfigService {
    Long create(AiModelConfigSaveReqVO reqVO);
    void update(AiModelConfigSaveReqVO reqVO);
    void delete(Long id);
    AiModelConfigRespVO get(Long id);
    PageResult<AiModelConfigRespVO> getPage(AiModelConfigPageReqVO pageReqVO);
    boolean testConnection(Long id);
}
```

- [ ] **Step 2: Write AiModelConfigServiceImpl**

```java
package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.adapter.AiModelAdapter;
import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.AiModelConfigDO;
import cn.iocoder.yudao.adcreater.module.ai.dal.mapper.AiModelConfigMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.AesUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Map;

@Service
public class AiModelConfigServiceImpl implements AiModelConfigService {

    @Resource
    private AiModelConfigMapper mapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public Long create(AiModelConfigSaveReqVO reqVO) {
        AiModelConfigDO entity = BeanUtils.toBean(reqVO, AiModelConfigDO.class);
        entity.setApiKey(AesUtils.encrypt(reqVO.getApiKey()));
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(AiModelConfigSaveReqVO reqVO) {
        AiModelConfigDO entity = BeanUtils.toBean(reqVO, AiModelConfigDO.class);
        if (reqVO.getApiKey() != null && !reqVO.getApiKey().contains("****")) {
            entity.setApiKey(AesUtils.encrypt(reqVO.getApiKey()));
        }
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public AiModelConfigRespVO get(Long id) {
        AiModelConfigDO entity = mapper.selectById(id);
        AiModelConfigRespVO resp = BeanUtils.toBean(entity, AiModelConfigRespVO.class);
        resp.setApiKey(maskApiKey(entity.getApiKey()));
        return resp;
    }

    @Override
    public PageResult<AiModelConfigRespVO> getPage(AiModelConfigPageReqVO pageReqVO) {
        PageResult<AiModelConfigDO> pageResult = mapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, AiModelConfigRespVO.class);
    }

    @Override
    public boolean testConnection(Long id) {
        AiModelConfigDO config = mapper.selectById(id);
        Map<String, AiModelAdapter> adapters = applicationContext.getBeansOfType(AiModelAdapter.class);
        AiModelAdapter adapter = adapters.values().stream()
            .filter(a -> a.getClass().getName().equals(config.getAdapterClass()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Adapter not found: " + config.getAdapterClass()));
        return adapter.validateConfig(config);
    }

    private String maskApiKey(String encryptedKey) {
        String decrypted = AesUtils.decrypt(encryptedKey);
        if (decrypted.length() <= 8) return "****";
        return decrypted.substring(0, 3) + "****" + decrypted.substring(decrypted.length() - 4);
    }
}
```

- [ ] **Step 3: Write AiModelConfigController (admin)**

```java
package cn.iocoder.yudao.adcreater.module.ai.controller.admin;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.service.AiModelConfigService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin-api/ai/model-config")
public class AiModelConfigController {

    @Resource
    private AiModelConfigService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('ai:model-config:create')")
    public CommonResult<Long> create(@Valid @RequestBody AiModelConfigSaveReqVO reqVO) {
        return CommonResult.success(service.create(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('ai:model-config:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody AiModelConfigSaveReqVO reqVO) {
        service.update(reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('ai:model-config:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        service.delete(id);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('ai:model-config:query')")
    public CommonResult<AiModelConfigRespVO> get(@RequestParam("id") Long id) {
        return CommonResult.success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('ai:model-config:query')")
    public CommonResult<PageResult<AiModelConfigRespVO>> getPage(@Valid AiModelConfigPageReqVO pageReqVO) {
        return CommonResult.success(service.getPage(pageReqVO));
    }

    @PostMapping("/test-connection")
    @PreAuthorize("@ss.hasPermission('ai:model-config:update')")
    public CommonResult<Boolean> testConnection(@RequestParam("id") Long id) {
        return CommonResult.success(service.testConnection(id));
    }
}
```

- [ ] **Step 4: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 5: PromptTemplate Admin CRUD API

**Files:**
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/service/PromptService.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/service/PromptServiceImpl.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/controller/admin/PromptTemplateController.java`

- [ ] **Step 1: Write PromptService interface**

```java
package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.util.Map;

public interface PromptService {
    Long create(PromptTemplateSaveReqVO reqVO);
    void update(PromptTemplateSaveReqVO reqVO);
    void delete(Long id);
    PromptTemplateRespVO get(Long id);
    PageResult<PromptTemplateRespVO> getPage(PromptTemplatePageReqVO pageReqVO);
    /** 根据分类获取启用的 Prompt 模板，并 merge 变量 */
    String resolvePrompt(String category, Map<String, String> variables);
}
```

- [ ] **Step 2: Write PromptServiceImpl**

```java
package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.PromptTemplateDO;
import cn.iocoder.yudao.adcreater.module.ai.dal.mapper.PromptTemplateMapper;
import cn.iocoder.yudao.adcreater.common.enums.PromptCategoryEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptServiceImpl implements PromptService {

    @Resource
    private PromptTemplateMapper mapper;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)(?:\\|([^}]*))?\\}\\}");

    @Override
    public Long create(PromptTemplateSaveReqVO reqVO) {
        PromptTemplateDO entity = BeanUtils.toBean(reqVO, PromptTemplateDO.class);
        entity.setVariables(extractVariables(reqVO.getTemplateContent()));
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(PromptTemplateSaveReqVO reqVO) {
        PromptTemplateDO entity = BeanUtils.toBean(reqVO, PromptTemplateDO.class);
        entity.setVariables(extractVariables(reqVO.getTemplateContent()));
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PromptTemplateRespVO get(Long id) {
        return BeanUtils.toBean(mapper.selectById(id), PromptTemplateRespVO.class);
    }

    @Override
    public PageResult<PromptTemplateRespVO> getPage(PromptTemplatePageReqVO pageReqVO) {
        PageResult<PromptTemplateDO> pageResult = mapper.selectPage(pageReqVO,
            new LambdaQueryWrapper<PromptTemplateDO>()
                .eq(pageReqVO.getCategory() != null,
                    PromptTemplateDO::getCategory, pageReqVO.getCategory()));
        return BeanUtils.toBean(pageResult, PromptTemplateRespVO.class);
    }

    @Override
    public String resolvePrompt(String category, Map<String, String> variables) {
        PromptTemplateDO template = mapper.selectByCategoryAndEnabled(category);
        if (template == null) {
            return buildDefaultPrompt(category, variables);
        }
        String content = template.getTemplateContent();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultValue = matcher.group(2);
            String value = variables.getOrDefault(varName, defaultValue != null ? defaultValue : "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String extractVariables(String templateContent) {
        // Parse {{variable}} from template, build JSON array
        // Returns: ["product_name", "style", "background"]
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        java.util.Set<String> vars = new java.util.LinkedHashSet<>();
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return com.alibaba.fastjson.JSON.toJSONString(vars);
    }

    private String buildDefaultPrompt(String category, Map<String, String> variables) {
        // Fallback when no template configured
        if ("image".equals(category)) {
            return String.format(
                "Professional e-commerce product photo of %s. Style: %s. Background: clean white studio. High resolution, commercial quality.",
                variables.getOrDefault("product_name", "product"),
                variables.getOrDefault("style", "modern minimalist"));
        }
        // Similar for copy, video, digital_human categories
        return String.format("Generate %s content for: %s", category,
            variables.getOrDefault("product_name", "product"));
    }
}
```

- [ ] **Step 3: Write PromptTemplateController**

```java
package cn.iocoder.yudao.adcreater.module.ai.controller.admin;

import cn.iocoder.yudao.adcreater.module.ai.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin-api/ai/prompt-template")
public class PromptTemplateController {

    @Resource
    private PromptService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('ai:prompt:create')")
    public CommonResult<Long> create(@Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        return CommonResult.success(service.create(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('ai:prompt:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        service.update(reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('ai:prompt:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        service.delete(id);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('ai:prompt:query')")
    public CommonResult<PromptTemplateRespVO> get(@RequestParam("id") Long id) {
        return CommonResult.success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('ai:prompt:query')")
    public CommonResult<PageResult<PromptTemplateRespVO>> getPage(@Valid PromptTemplatePageReqVO pageReqVO) {
        return CommonResult.success(service.getPage(pageReqVO));
    }
}
```

- [ ] **Step 4: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 6: ModelOrchestrationService + Adapter Implementations

**Files:**
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/service/ModelOrchestrationService.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/service/ModelOrchestrationServiceImpl.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/adapter/OpenAIAdapter.java`
- Create: `adcreater-server/module-ai/src/main/java/cn/iocoder/yudao/adcreater/module/ai/adapter/StabilityAdapter.java`

- [ ] **Step 1: Write ModelOrchestrationService interface**

```java
package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.adapter.*;
import java.util.function.Consumer;

public interface ModelOrchestrationService {
    /** 生成图片 (SSE 进度回调) */
    AiResult generateImage(AiRequest request, Consumer<String> progressCallback);

    /** 生成视频 */
    AiResult generateVideo(AiRequest request, Consumer<String> progressCallback);

    /** 文案翻译+本地化 */
    AiResult translate(String sourceText, String sourceLang, String targetLang, String targetMarket);

    /** 数字人生成 */
    AiResult generateDigitalHuman(AiRequest request, Consumer<String> progressCallback);
}
```

- [ ] **Step 2: Write ModelOrchestrationServiceImpl (core orchestration logic)**

```java
package cn.iocoder.yudao.adcreater.module.ai.service;

import cn.iocoder.yudao.adcreater.module.ai.adapter.*;
import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.AiModelConfigDO;
import cn.iocoder.yudao.adcreater.module.ai.dal.mapper.AiModelConfigMapper;
import cn.iocoder.yudao.adcreater.common.exception.AiCallException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;

@Service
public class ModelOrchestrationServiceImpl implements ModelOrchestrationService {

    @Resource
    private AiModelConfigMapper configMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public AiResult generateImage(AiRequest request, Consumer<String> progressCallback) {
        return executeWithFallback("image", request, progressCallback);
    }

    @Override
    public AiResult generateVideo(AiRequest request, Consumer<String> progressCallback) {
        return executeWithFallback("video", request, progressCallback);
    }

    @Override
    public AiResult translate(String sourceText, String sourceLang, String targetLang, String targetMarket) {
        AiRequest request = new AiRequest();
        request.setPrompt(sourceText);
        request.getExtraParams().put("sourceLang", sourceLang);
        request.getExtraParams().put("targetLang", targetLang);
        request.getExtraParams().put("targetMarket", targetMarket);
        return executeWithFallback("copy", request, null);
    }

    @Override
    public AiResult generateDigitalHuman(AiRequest request, Consumer<String> progressCallback) {
        return executeWithFallback("digital_human", request, progressCallback);
    }

    private AiResult executeWithFallback(String taskType, AiRequest request,
                                          Consumer<String> progressCallback) {
        // 1. 按优先级获取该类型的所有已启用模型
        List<AiModelConfigDO> configs = configMapper.selectList(
            new LambdaQueryWrapper<AiModelConfigDO>()
                .eq(AiModelConfigDO::getIsEnabled, true)
                .orderByAsc(AiModelConfigDO::getPriority));

        if (configs.isEmpty()) {
            return AiResult.builder().success(false)
                .errorMessage("No enabled model found for task type: " + taskType).build();
        }

        // 2. 按优先级依次尝试
        Exception lastException = null;
        for (AiModelConfigDO config : configs) {
            try {
                AiModelAdapter adapter = getAdapter(config.getAdapterClass());
                if (progressCallback != null) {
                    progressCallback.accept("正在调用 " + config.getModelName() + "...");
                }
                AiResult result = adapter.call(request, config);
                if (result.isSuccess()) {
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
                if (progressCallback != null) {
                    progressCallback.accept(config.getModelName() + " 调用失败，尝试备用模型...");
                }
            }
        }

        // 3. 全部失败
        String msg = lastException != null ? lastException.getMessage() : "All models failed";
        return AiResult.builder().success(false).errorMessage(msg).build();
    }

    private AiModelAdapter getAdapter(String adapterClassName) {
        Map<String, AiModelAdapter> adapters = applicationContext.getBeansOfType(AiModelAdapter.class);
        return adapters.values().stream()
            .filter(a -> a.getClass().getName().equals(adapterClassName))
            .findFirst()
            .orElseThrow(() -> new AiCallException("unknown",
                "Adapter not found: " + adapterClassName));
    }
}
```

- [ ] **Step 3: Write OpenAIAdapter (DALL-E + GPT-4o)**

```java
package cn.iocoder.yudao.adcreater.module.ai.adapter;

import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.AiModelConfigDO;
import cn.iocoder.yudao.framework.common.util.AesUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Component
public class OpenAIAdapter implements AiModelAdapter {

    private final WebClient webClient = WebClient.create();

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        String apiKey = AesUtils.decrypt(config.getApiKey());
        String endpoint = config.getEndpointUrl() + "/v1/images/generations";

        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : "dall-e-3",
            "prompt", request.getPrompt(),
            "n", 1,
            "size", request.getWidth() + "x" + request.getHeight(),
            "quality", "hd"
        );

        Map<String, Object> response = webClient.post()
            .uri(endpoint)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        String url = (String) data.get(0).get("url");

        return AiResult.builder()
            .success(true)
            .url(url)
            .revisedPrompt((String) data.get(0).get("revised_prompt"))
            .build();
    }

    @Override
    public boolean validateConfig(AiModelConfigDO config) {
        try {
            String apiKey = AesUtils.decrypt(config.getApiKey());
            Map<String, Object> response = webClient.get()
                .uri(config.getEndpointUrl() + "/v1/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            return response != null && response.containsKey("data");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int estimateCost(AiRequest request) {
        return 10; // DALL-E 3 基础消耗 10 点
    }
}
```

- [ ] **Step 4: Write StabilityAdapter**

```java
package cn.iocoder.yudao.adcreater.module.ai.adapter;

import cn.iocoder.yudao.adcreater.module.ai.dal.dataobject.AiModelConfigDO;
import cn.iocoder.yudao.framework.common.util.AesUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class StabilityAdapter implements AiModelAdapter {

    private final WebClient webClient = WebClient.create();

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        String apiKey = AesUtils.decrypt(config.getApiKey());

        Map<String, Object> body = Map.of(
            "prompt", request.getPrompt(),
            "negative_prompt", request.getNegativePrompt() != null ? request.getNegativePrompt() : "",
            "output_format", "jpeg"
        );

        Map<String, Object> response = webClient.post()
            .uri("https://api.stability.ai/v2beta/stable-image/generate/sd3")
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        String base64Image = (String) response.get("image");
        return AiResult.builder()
            .success(true)
            .url("data:image/jpeg;base64," + base64Image)  // Base64 data URL
            .build();
    }

    @Override
    public boolean validateConfig(AiModelConfigDO config) {
        try {
            String apiKey = AesUtils.decrypt(config.getApiKey());
            Map<String, Object> response = webClient.get()
                .uri("https://api.stability.ai/v1/user/account")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            return response != null && response.containsKey("email");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int estimateCost(AiRequest request) {
        return 5; // SD 基础消耗 5 点
    }
}
```

- [ ] **Step 5: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Phase 3: Billing Module

### Task 7: Billing Entities & Mappers

**Files:**
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/dal/dataobject/UserPointsAccountDO.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/dal/dataobject/PointsTransactionDO.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/dal/dataobject/UsageRecordDO.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/dal/mapper/UserPointsAccountMapper.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/dal/mapper/PointsTransactionMapper.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/dal/mapper/UsageRecordMapper.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/controller/admin/vo/*.java`

- [ ] **Step 1: Write DO classes**

```java
package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_points_account")
public class UserPointsAccountDO {
    @TableId
    private Long id;
    private Long userId;
    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
}
```

```java
package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("points_transaction")
public class PointsTransactionDO {
    @TableId
    private Long id;
    private Long userId;
    private String type;     // earn/consume/refund
    private Integer amount;
    private Integer balanceAfter;
    private String status;   // pending/confirmed/rolled_back
    private Long bizId;
    private String remark;
    private LocalDateTime createdAt;
}
```

```java
package cn.iocoder.yudao.adcreater.module.billing.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("usage_record")
public class UsageRecordDO {
    @TableId
    private Long id;
    private Long userId;
    private String taskType;
    private String modelUsed;
    private Integer pointsConsumed;
    private Integer inputTokens;
    private Integer outputTokens;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Write Mappers**

```java
package cn.iocoder.yudao.adcreater.module.billing.dal.mapper;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.UserPointsAccountDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserPointsAccountMapper extends BaseMapperX<UserPointsAccountDO> {

    @Select("SELECT * FROM user_points_account WHERE user_id = #{userId} FOR UPDATE")
    UserPointsAccountDO selectByUserIdForUpdate(@Param("userId") Long userId);

    @Update("UPDATE user_points_account SET balance = balance - #{amount}, " +
            "total_spent = total_spent + #{amount} WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductPoints(@Param("userId") Long userId, @Param("amount") int amount);

    @Update("UPDATE user_points_account SET balance = balance + #{amount}, " +
            "total_earned = total_earned + #{amount} WHERE user_id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("amount") int amount);
}
```

```java
package cn.iocoder.yudao.adcreater.module.billing.dal.mapper;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PointsTransactionMapper extends BaseMapperX<PointsTransactionDO> {
}
```

```java
package cn.iocoder.yudao.adcreater.module.billing.dal.mapper;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.UsageRecordDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsageRecordMapper extends BaseMapperX<UsageRecordDO> {
}
```

- [ ] **Step 3: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 8: BillingService (Core Points Logic)

**Files:**
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/service/BillingService.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/service/BillingServiceImpl.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/service/UsageService.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/service/UsageServiceImpl.java`

- [ ] **Step 1: Write BillingService interface**

```java
package cn.iocoder.yudao.adcreater.module.billing.service;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;

public interface BillingService {
    /** 管理员充值 */
    void recharge(Long userId, int amount, String remark);

    /** 预扣点数 (返回 transactionId) */
    PointsTransactionDO preConsume(Long userId, int amount, Long bizId);

    /** 确认扣减 */
    void confirmConsume(Long transactionId);

    /** 回滚扣减 */
    void rollbackConsume(Long transactionId);

    /** 查询余额 */
    int getBalance(Long userId);

    /** 确保用户有账户 (首次使用时自动创建) */
    void ensureAccount(Long userId);
}
```

- [ ] **Step 2: Write BillingServiceImpl**

```java
package cn.iocoder.yudao.adcreater.module.billing.service;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.*;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.*;
import cn.iocoder.yudao.adcreater.common.exception.InsufficientPointsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class BillingServiceImpl implements BillingService {

    @Resource
    private UserPointsAccountMapper accountMapper;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @Override
    @Transactional
    public void recharge(Long userId, int amount, String remark) {
        ensureAccount(userId);
        accountMapper.addPoints(userId, amount);
        UserPointsAccountDO account = accountMapper.selectByUserIdForUpdate(userId);

        PointsTransactionDO tx = new PointsTransactionDO();
        tx.setUserId(userId);
        tx.setType("earn");
        tx.setAmount(amount);
        tx.setBalanceAfter(account.getBalance());
        tx.setStatus("confirmed");
        tx.setRemark(remark);
        tx.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(tx);
    }

    @Override
    @Transactional
    public PointsTransactionDO preConsume(Long userId, int amount, Long bizId) {
        ensureAccount(userId);

        // 悲观锁查询，防止并发超扣
        UserPointsAccountDO account = accountMapper.selectByUserIdForUpdate(userId);
        if (account.getBalance() < amount) {
            throw new InsufficientPointsException(amount, account.getBalance());
        }

        // 扣减
        int updated = accountMapper.deductPoints(userId, amount);
        if (updated == 0) {
            throw new InsufficientPointsException(amount, 0);
        }

        // 记录流水
        PointsTransactionDO tx = new PointsTransactionDO();
        tx.setUserId(userId);
        tx.setType("consume");
        tx.setAmount(amount);
        tx.setBalanceAfter(account.getBalance() - amount);
        tx.setStatus("pending");
        tx.setBizId(bizId);
        tx.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(tx);

        return tx;
    }

    @Override
    @Transactional
    public void confirmConsume(Long transactionId) {
        PointsTransactionDO tx = transactionMapper.selectById(transactionId);
        if (tx == null || !"pending".equals(tx.getStatus())) return;
        tx.setStatus("confirmed");
        transactionMapper.updateById(tx);
    }

    @Override
    @Transactional
    public void rollbackConsume(Long transactionId) {
        PointsTransactionDO tx = transactionMapper.selectById(transactionId);
        if (tx == null || !"pending".equals(tx.getStatus())) return;

        // 退款
        accountMapper.addPoints(tx.getUserId(), tx.getAmount());

        tx.setStatus("rolled_back");
        transactionMapper.updateById(tx);
    }

    @Override
    public int getBalance(Long userId) {
        ensureAccount(userId);
        UserPointsAccountDO account = accountMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPointsAccountDO>()
                .eq(UserPointsAccountDO::getUserId, userId));
        return account != null ? account.getBalance() : 0;
    }

    @Override
    public void ensureAccount(Long userId) {
        UserPointsAccountDO existing = accountMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPointsAccountDO>()
                .eq(UserPointsAccountDO::getUserId, userId));
        if (existing == null) {
            UserPointsAccountDO account = new UserPointsAccountDO();
            account.setUserId(userId);
            account.setBalance(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
            accountMapper.insert(account);
        }
    }
}
```

- [ ] **Step 3: Write UsageService**

```java
package cn.iocoder.yudao.adcreater.module.billing.service;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.UsageRecordDO;

public interface UsageService {
    void record(Long userId, String taskType, String modelUsed,
                int pointsConsumed, int inputTokens, int outputTokens);
}

@Service
public class UsageServiceImpl implements UsageService {
    @Resource
    private UsageRecordMapper mapper;

    @Override
    public void record(Long userId, String taskType, String modelUsed,
                       int pointsConsumed, int inputTokens, int outputTokens) {
        UsageRecordDO record = new UsageRecordDO();
        record.setUserId(userId);
        record.setTaskType(taskType);
        record.setModelUsed(modelUsed);
        record.setPointsConsumed(pointsConsumed);
        record.setInputTokens(inputTokens);
        record.setOutputTokens(outputTokens);
        record.setCreatedAt(java.time.LocalDateTime.now());
        mapper.insert(record);
    }
}
```

- [ ] **Step 4: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 9: Billing Admin API

**Files:**
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/controller/admin/BillingController.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/controller/admin/vo/RechargeReqVO.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/controller/admin/vo/TransactionRespVO.java`
- Create: `adcreater-server/module-billing/src/main/java/cn/iocoder/yudao/adcreater/module/billing/controller/app/BillingAppController.java`

- [ ] **Step 1: Write admin BillingController (充值管理)**

```java
package cn.iocoder.yudao.adcreater.module.billing.controller.admin;

import cn.iocoder.yudao.adcreater.module.billing.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.PointsTransactionMapper;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin-api/billing")
public class BillingController {

    @Resource
    private BillingService billingService;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @PostMapping("/recharge")
    @PreAuthorize("@ss.hasPermission('billing:recharge')")
    public CommonResult<Boolean> recharge(@Valid @RequestBody RechargeReqVO reqVO) {
        billingService.recharge(reqVO.getUserId(), reqVO.getAmount(), reqVO.getRemark());
        return CommonResult.success(true);
    }

    @GetMapping("/transaction/page")
    @PreAuthorize("@ss.hasPermission('billing:query')")
    public CommonResult<PageResult<PointsTransactionDO>> getTransactionPage(
            @Valid PointsTransactionPageReqVO pageReqVO) {
        return CommonResult.success(transactionMapper.selectPage(pageReqVO));
    }
}
```

- [ ] **Step 2: Write app BillingAppController (用户端余额查询+流水)**

```java
package cn.iocoder.yudao.adcreater.module.billing.controller.app;

import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.dal.mapper.PointsTransactionMapper;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingAppController {

    @Resource
    private BillingService billingService;

    @Resource
    private PointsTransactionMapper transactionMapper;

    @GetMapping("/balance")
    public CommonResult<Map<String, Integer>> getBalance() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        int balance = billingService.getBalance(userId);
        return CommonResult.success(Map.of("balance", balance));
    }

    @GetMapping("/transaction/page")
    public CommonResult<PageResult<PointsTransactionDO>> getTransactionPage(
            @Valid PointsTransactionPageReqVO pageReqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        return CommonResult.success(transactionMapper.selectPage(pageReqVO,
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PointsTransactionDO>()
                .eq(PointsTransactionDO::getUserId, userId)));
    }
}
```

- [ ] **Step 3: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Phase 4: Template Module

### Task 10: Template Entities & TemplateService

**Files:**
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/dal/dataobject/TemplateDO.java`
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/dal/dataobject/TemplateVersionDO.java`
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/dal/mapper/TemplateMapper.java`
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/dal/mapper/TemplateVersionMapper.java`
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/service/TemplateService.java`
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/service/TemplateServiceImpl.java`
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/controller/admin/TemplateController.java`
- Create: `adcreater-server/module-template/src/main/java/cn/iocoder/yudao/adcreater/module/template/controller/app/TemplateClientController.java`

- [ ] **Step 1: Write TemplateDO**

```java
package cn.iocoder.yudao.adcreater.module.template.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("template")
public class TemplateDO {
    @TableId
    private Long id;
    private String name;
    private String description;
    private String category;     // image/video
    private String thumbnailUrl;
    private String status;       // draft/published
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Write TemplateVersionDO**

```java
package cn.iocoder.yudao.adcreater.module.template.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("template_version")
public class TemplateVersionDO {
    @TableId
    private Long id;
    private Long templateId;
    private Integer version;
    private String fileUrl;
    private String changelog;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: Write TemplateService (管理端 CRUD + 客户端同步)**

```java
package cn.iocoder.yudao.adcreater.module.template.service;

import cn.iocoder.yudao.adcreater.module.template.controller.admin.vo.*;
import cn.iocoder.yudao.adcreater.module.template.controller.app.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.util.Map;

public interface TemplateService {
    // Admin CRUD
    Long createTemplate(TemplateSaveReqVO reqVO);
    void updateTemplate(TemplateSaveReqVO reqVO);
    void deleteTemplate(Long id);
    TemplateRespVO getTemplate(Long id);
    PageResult<TemplateRespVO> getTemplatePage(TemplatePageReqVO pageReqVO);

    // Version management
    Long publishVersion(Long templateId, String fileUrl, String changelog);
    List<TemplateVersionRespVO> getVersions(Long templateId);

    // Client sync
    SyncResultVO syncTemplates(SyncRequestVO reqVO);
    String getDownloadUrl(Long templateVersionId);
}
```

- [ ] **Step 4: Write TemplateServiceImpl - sync logic**

```java
@Service
public class TemplateServiceImpl implements TemplateService {
    // ... admin CRUD (standard, omitted for brevity) ...

    @Override
    public SyncResultVO syncTemplates(SyncRequestVO reqVO) {
        // reqVO.localVersions: [{templateId: 1, version: 3}, ...]
        List<SyncRequestVO.LocalVersion> localVersions = reqVO.getLocalVersions();
        Map<Long, Integer> localVersionMap = localVersions.stream()
            .collect(Collectors.toMap(
                SyncRequestVO.LocalVersion::getTemplateId,
                SyncRequestVO.LocalVersion::getVersion));

        // 查询所有已发布模板的最新版本
        List<TemplateVersionDO> latestVersions = templateMapper.selectLatestVersions();

        List<SyncResultVO.UpdatedTemplate> updated = new ArrayList<>();
        for (TemplateVersionDO serverVersion : latestVersions) {
            Integer localVer = localVersionMap.get(serverVersion.getTemplateId());
            if (localVer == null || serverVersion.getVersion() > localVer) {
                TemplateDO template = templateMapper.selectById(serverVersion.getTemplateId());
                SyncResultVO.UpdatedTemplate ut = new SyncResultVO.UpdatedTemplate();
                ut.setTemplateId(template.getId());
                ut.setVersion(serverVersion.getVersion());
                ut.setFileUrl(serverVersion.getFileUrl());
                ut.setChangelog(serverVersion.getChangelog());
                updated.add(ut);
            }
        }

        // 检测已删除的模板 (服务端已删但客户端有的)
        List<Long> serverTemplateIds = latestVersions.stream()
            .map(TemplateVersionDO::getTemplateId).collect(Collectors.toList());
        List<Long> deleted = localVersionMap.keySet().stream()
            .filter(id -> !serverTemplateIds.contains(id))
            .collect(Collectors.toList());

        SyncResultVO result = new SyncResultVO();
        result.setUpdated(updated);
        result.setDeleted(deleted);
        return result;
    }
}
```

- [ ] **Step 5: Write TemplateClientController (客户端同步 API)**

```java
package cn.iocoder.yudao.adcreater.module.template.controller.app;

import cn.iocoder.yudao.adcreater.module.template.service.TemplateService;
import cn.iocoder.yudao.adcreater.module.template.controller.app.vo.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/template/client")
public class TemplateClientController {

    @Resource
    private TemplateService templateService;

    @PostMapping("/sync")
    public CommonResult<SyncResultVO> sync(@Valid @RequestBody SyncRequestVO reqVO) {
        return CommonResult.success(templateService.syncTemplates(reqVO));
    }

    @GetMapping("/download/{versionId}")
    public CommonResult<String> getDownloadUrl(@PathVariable Long versionId) {
        return CommonResult.success(templateService.getDownloadUrl(versionId));
    }
}
```

- [ ] **Step 6: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Phase 5: Ad Module

### Task 11: AdCopyController (文案翻译+本地化)

**Files:**
- Create: `adcreater-server/module-ad/src/main/java/cn/iocoder/yudao/adcreater/module/ad/controller/AdCopyController.java`
- Create: `adcreater-server/module-ad/src/main/java/cn/iocoder/yudao/adcreater/module/ad/controller/vo/TranslateReqVO.java`
- Create: `adcreater-server/module-ad/src/main/java/cn/iocoder/yudao/adcreater/module/ad/controller/vo/TranslateRespVO.java`

- [ ] **Step 1: Write TranslateReqVO**

```java
package cn.iocoder.yudao.adcreater.module.ad.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TranslateReqVO {
    @NotBlank
    private String productTitle;
    private String productDescription;
    @NotEmpty
    private List<String> sellingPoints;
    @NotBlank
    private String sourceLang;   // zh-CN
    @NotBlank
    private String targetLang;   // en-US
    @NotBlank
    private String targetMarket; // US / DE / JP / SA
}
```

- [ ] **Step 2: Write AdCopyController**

```java
package cn.iocoder.yudao.adcreater.module.ad.controller;

import cn.iocoder.yudao.adcreater.module.ad.controller.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.service.ModelOrchestrationService;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/ad/copy")
public class AdCopyController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @PostMapping("/translate")
    public CommonResult<TranslateRespVO> translate(@Valid @RequestBody TranslateReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();

        // 构建变量
        Map<String, String> vars = Map.of(
            "product_name", reqVO.getProductTitle(),
            "description", reqVO.getProductDescription() != null ? reqVO.getProductDescription() : "",
            "selling_points", String.join(", ", reqVO.getSellingPoints()),
            "target_market", reqVO.getTargetMarket(),
            "target_lang", reqVO.getTargetLang()
        );

        // 解析 Prompt
        String prompt = promptService.resolvePrompt("copy", vars);

        // 预扣点数
        int cost = 5; // 翻译消耗 5 点
        billingService.preConsume(userId, cost, null);

        try {
            // 调用 AI
            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(prompt);
            aiReq.getExtraParams().put("sourceLang", reqVO.getSourceLang());
            aiReq.getExtraParams().put("targetLang", reqVO.getTargetLang());
            AiResult result = orchestrationService.translate(
                reqVO.getProductTitle(), reqVO.getSourceLang(),
                reqVO.getTargetLang(), reqVO.getTargetMarket());

            if (!result.isSuccess()) {
                throw new RuntimeException(result.getErrorMessage());
            }

            // TODO: Parse result into structured response
            TranslateRespVO resp = new TranslateRespVO();
            resp.setTranslatedTitle(result.getUrl()); // Placeholder — real impl parses AI response
            return CommonResult.success(resp);
        } catch (Exception e) {
            // 点数回滚在外层 AOP 或这里处理
            throw e;
        }
    }
}
```

---

### Task 12: AdCreativeController (图片+视频生成 with SSE)

**Files:**
- Create: `adcreater-server/module-ad/src/main/java/cn/iocoder/yudao/adcreater/module/ad/controller/AdCreativeController.java`
- Create: `adcreater-server/module-ad/src/main/java/cn/iocoder/yudao/adcreater/module/ad/controller/vo/ImageGenReqVO.java`
- Create: `adcreater-server/module-ad/src/main/java/cn/iocoder/yudao/adcreater/module/ad/controller/vo/BatchImageGenReqVO.java`
- Create: `adcreater-server/module-ad/src/main/java/cn/iocoder/yudao/adcreater/module/ad/controller/vo/VideoGenReqVO.java`

- [ ] **Step 1: Write AdCreativeController with SSE support**

```java
package cn.iocoder.yudao.adcreater.module.ad.controller;

import cn.iocoder.yudao.adcreater.module.ad.controller.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiRequest;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiResult;
import cn.iocoder.yudao.adcreater.module.ai.service.ModelOrchestrationService;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.adcreater.module.billing.service.UsageService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/ad/creative")
public class AdCreativeController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @Resource
    private UsageService usageService;

    @PostMapping(value = "/image/text-to-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter textToImage(@Valid @RequestBody ImageGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5 min timeout

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                // 1. 解析 Prompt
                Map<String, String> vars = new HashMap<>();
                vars.put("product_name", reqVO.getProductName());
                vars.put("style", reqVO.getStyle() != null ? reqVO.getStyle() : "modern minimalist");
                vars.put("background", reqVO.getBackground() != null ? reqVO.getBackground() : "clean studio white");
                vars.put("target_market", reqVO.getTargetMarket());
                vars.put("selling_points", reqVO.getSellingPoints());
                vars.put("width", String.valueOf(reqVO.getWidth()));
                vars.put("height", String.valueOf(reqVO.getHeight()));
                String prompt = promptService.resolvePrompt("image", vars);

                // 2. 预扣点数
                int cost = 10;
                tx = billingService.preConsume(userId, cost, null);

                // 3. 构建请求
                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setWidth(reqVO.getWidth());
                aiReq.setHeight(reqVO.getHeight());
                aiReq.setStyle(reqVO.getStyle());

                // 4. 调用模型 (SSE 推送进度)
                sendSse(emitter, "progress", Map.of("progress", 10, "status", "processing"));

                AiResult result = orchestrationService.generateImage(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("progress", 50, "message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    // 5. 确认扣减
                    billingService.confirmConsume(tx.getId());
                    usageService.record(userId, "image", result.getRevisedPrompt() != null ? "dall-e-3" : "stable-diffusion",
                            cost, result.getInputTokens(), result.getOutputTokens());

                    sendSse(emitter, "done", Map.of("imageUrl", result.getUrl()));
                }
                emitter.complete();
            } catch (Exception e) {
                if (tx != null) billingService.rollbackConsume(tx.getId());
                sendSse(emitter, "error", Map.of("message", e.getMessage()));
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/image/batch")
    public CommonResult<List<Map<String, String>>> batchGenerate(@Valid @RequestBody BatchImageGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        List<Map<String, String>> results = new ArrayList<>();

        for (BatchImageGenReqVO.SizeSpec size : reqVO.getSizes()) {
            // For each size, generate image
            Map<String, String> vars = Map.of(
                "product_name", reqVO.getProductName(),
                "width", String.valueOf(size.getWidth()),
                "height", String.valueOf(size.getHeight()),
                "platform", size.getPlatform()
            );
            String prompt = promptService.resolvePrompt("image", vars);

            AiRequest aiReq = new AiRequest();
            aiReq.setPrompt(prompt);
            aiReq.setWidth(size.getWidth());
            aiReq.setHeight(size.getHeight());

            int cost = 10;
            PointsTransactionDO tx = billingService.preConsume(userId, cost, null);
            try {
                AiResult result = orchestrationService.generateImage(aiReq, null);
                if (result.isSuccess()) {
                    billingService.confirmConsume(tx.getId());
                    results.add(Map.of("platform", size.getPlatform(), "url", result.getUrl()));
                } else {
                    billingService.rollbackConsume(tx.getId());
                    results.add(Map.of("platform", size.getPlatform(), "error", result.getErrorMessage()));
                }
            } catch (Exception e) {
                billingService.rollbackConsume(tx.getId());
            }
        }

        return CommonResult.success(results);
    }

    @PostMapping(value = "/video", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateVideo(@Valid @RequestBody VideoGenReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L); // 10 min timeout

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                int cost = 50; // 视频消耗 50 点
                tx = billingService.preConsume(userId, cost, null);

                Map<String, String> vars = Map.of(
                    "product_name", reqVO.getProductName(),
                    "duration", String.valueOf(reqVO.getDuration()),
                    "style", reqVO.getStyle() != null ? reqVO.getStyle() : "professional"
                );
                String prompt = promptService.resolvePrompt("video", vars);

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setDuration(reqVO.getDuration());
                aiReq.setStyle(reqVO.getStyle());

                AiResult result = orchestrationService.generateVideo(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("progress", 0, "message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
                    sendSse(emitter, "done", Map.of("videoUrl", result.getUrl()));
                }
                emitter.complete();
            } catch (Exception e) {
                if (tx != null) billingService.rollbackConsume(tx.getId());
                sendSse(emitter, "error", Map.of("message", e.getMessage()));
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private void sendSse(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event()
                .name(event)
                .data(data, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    @Resource
    private java.util.concurrent.ExecutorService executorService;
}
```

- [ ] **Step 2: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Phase 6: Delivery Module

### Task 13: DeliveryController (数字人 + 平台投放)

**Files:**
- Create: `adcreater-server/module-delivery/src/main/java/cn/iocoder/yudao/adcreater/module/delivery/controller/DeliveryController.java`
- Create: `adcreater-server/module-delivery/src/main/java/cn/iocoder/yudao/adcreater/module/delivery/controller/vo/DigitalHumanReqVO.java`
- Create: `adcreater-server/module-delivery/src/main/java/cn/iocoder/yudao/adcreater/module/delivery/controller/vo/PlatformExportReqVO.java`

- [ ] **Step 1: Write DigitalHumanReqVO**

```java
package cn.iocoder.yudao.adcreater.module.delivery.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DigitalHumanReqVO {
    @NotBlank
    private String avatarId;      // 数字人形象 ID
    @NotBlank
    private String script;        // 口播脚本
    private String voiceId;       // 音色 ID (可选)
    @NotNull
    private Integer duration;     // 时长 (秒)
    private String backgroundUrl; // 背景图/视频 URL
}
```

- [ ] **Step 2: Write PlatformExportReqVO**

```java
package cn.iocoder.yudao.adcreater.module.delivery.controller.vo;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class PlatformExportReqVO {
    @NotEmpty
    private List<String> platforms;     // ["amazon", "facebook", "shopee", "tiktok"]
    private String baseImageUrl;        // 基础素材图片 URL
    private String copyText;            // 广告文案 (用于叠加到图片)
}
```

- [ ] **Step 3: Write DeliveryController**

```java
package cn.iocoder.yudao.adcreater.module.delivery.controller;

import cn.iocoder.yudao.adcreater.module.delivery.controller.vo.*;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiRequest;
import cn.iocoder.yudao.adcreater.module.ai.adapter.AiResult;
import cn.iocoder.yudao.adcreater.module.ai.service.ModelOrchestrationService;
import cn.iocoder.yudao.adcreater.module.ai.service.PromptService;
import cn.iocoder.yudao.adcreater.module.billing.dal.dataobject.PointsTransactionDO;
import cn.iocoder.yudao.adcreater.module.billing.service.BillingService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    @Resource
    private ExecutorService executorService;

    /** 平台尺寸预设 */
    private static final Map<String, int[][]> PLATFORM_SIZES = Map.of(
        "amazon", new int[][]{{1500, 1500}, {1200, 628}, {800, 800}},
        "facebook", new int[][]{{1200, 628}, {1080, 1080}, {1200, 444}},
        "shopee", new int[][]{{800, 800}, {1200, 1200}},
        "tiktok", new int[][]{{1080, 1920}, {720, 1280}},
        "google", new int[][]{{1200, 628}, {1200, 1200}, {970, 250}}
    );

    @PostMapping(value = "/digital-human", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateDigitalHuman(@Valid @RequestBody DigitalHumanReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);

        executorService.submit(() -> {
            PointsTransactionDO tx = null;
            try {
                int cost = 100;
                tx = billingService.preConsume(userId, cost, null);

                String prompt = promptService.resolvePrompt("digital_human", Map.of(
                    "script", reqVO.getScript(),
                    "avatar", reqVO.getAvatarId(),
                    "duration", String.valueOf(reqVO.getDuration())
                ));

                AiRequest aiReq = new AiRequest();
                aiReq.setPrompt(prompt);
                aiReq.setDuration(reqVO.getDuration());

                AiResult result = orchestrationService.generateDigitalHuman(aiReq, progressMsg -> {
                    sendSse(emitter, "progress", Map.of("message", progressMsg));
                });

                if (!result.isSuccess()) {
                    billingService.rollbackConsume(tx.getId());
                    sendSse(emitter, "error", Map.of("message", result.getErrorMessage()));
                } else {
                    billingService.confirmConsume(tx.getId());
                    sendSse(emitter, "done", Map.of("videoUrl", result.getUrl()));
                }
                emitter.complete();
            } catch (Exception e) {
                if (tx != null) billingService.rollbackConsume(tx.getId());
                sendSse(emitter, "error", Map.of("message", e.getMessage()));
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/platform/export")
    public CommonResult<List<Map<String, Object>>> platformExport(
            @Valid @RequestBody PlatformExportReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        List<Map<String, Object>> results = new ArrayList<>();

        for (String platform : reqVO.getPlatforms()) {
            int[][] sizes = PLATFORM_SIZES.getOrDefault(platform,
                new int[][]{{1200, 628}});

            List<Map<String, Object>> platformResults = new ArrayList<>();
            for (int[] size : sizes) {
                Map<String, Object> sizeResult = new HashMap<>();
                sizeResult.put("width", size[0]);
                sizeResult.put("height", size[1]);
                // Actual resize/adaptation logic calls image processing API
                sizeResult.put("status", "ready");
                platformResults.add(sizeResult);
            }

            Map<String, Object> platformResult = new HashMap<>();
            platformResult.put("platform", platform);
            platformResult.put("sizes", platformResults);
            results.add(platformResult);
        }

        return CommonResult.success(results);
    }

    private void sendSse(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event)
                .data(data, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
```

- [ ] **Step 2: Run Maven compile to verify**

Run: `cd adcreater-server && mvn compile -DskipTests`
Expected: BUILD SUCCESS

---

## Phase 7: Admin Frontend (Vue3 Web)

### Task 14: Admin Project Scaffold

**Files:**
- Create: `admin-frontend/package.json`
- Create: `admin-frontend/vite.config.ts`
- Create: `admin-frontend/src/main.ts`
- Create: `admin-frontend/src/App.vue`
- Create: `admin-frontend/src/router/index.ts`

- [ ] **Step 1: Initialize Vue3 + Vite + Element Plus project**

```bash
cd admin-frontend
npm create vite@latest . -- --template vue-ts
npm install element-plus vue-router pinia axios @element-plus/icons-vue
npm install -D @types/node sass
```

- [ ] **Step 2: Configure router with all admin pages**

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('@/pages/DashboardPage.vue'), meta: { title: '仪表盘' } },
      { path: 'users', component: () => import('@/pages/UserListPage.vue'), meta: { title: '用户管理' } },
      { path: 'billing', component: () => import('@/pages/BillingPage.vue'), meta: { title: '点数管理' } },
      { path: 'ai/models', component: () => import('@/pages/ModelConfigPage.vue'), meta: { title: '模型配置' } },
      { path: 'ai/prompts', component: () => import('@/pages/PromptConfigPage.vue'), meta: { title: 'Prompt 配置' } },
      { path: 'templates', component: () => import('@/pages/TemplateManagePage.vue'), meta: { title: '模板管理' } },
    ]
  },
  { path: '/login', component: () => import('@/pages/LoginPage.vue'), meta: { title: '登录' } },
]

export default createRouter({ history: createWebHistory(), routes })
```

- [ ] **Step 3: Setup API client with axios interceptor**

```typescript
// src/api/client.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

const client = axios.create({ baseURL: '/admin-api' })

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  res => res.data,
  err => {
    ElMessage.error(err.response?.data?.message || '请求失败')
    return Promise.reject(err)
  }
)

export default client
```

- [ ] **Step 4: Verify dev server starts**

Run: `cd admin-frontend && npm run dev`
Expected: Vite dev server on localhost:5173

---

### Task 15-19: Admin Business Pages

Due to the extensive nature of admin pages, each page follows the same pattern:
1. Define TypeScript types matching backend VO
2. Write API service functions
3. Build page with Element Plus Table + Form + Dialog

**Task 15**: UserListPage (user list + recharge dialog)
**Task 16**: BillingPage (transaction history + usage stats)
**Task 17**: ModelConfigPage (model list + edit form with API key masking)
**Task 18**: PromptConfigPage (prompt list + template editor with variable highlighting)
**Task 19**: TemplateManagePage (template list + upload + version history)

Each page follows the pattern:

```vue
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ pageTitle }}</span>
          <el-button type="primary" @click="handleCreate">新增</el-button>
        </div>
      </template>
      <!-- Search bar -->
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
      <!-- Data table -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <!-- columns -->
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- Pagination -->
      <el-pagination v-model:current-page="pageNo" :total="total"
        @current-change="fetchData" layout="total, prev, pager, next" />
    </el-card>
    <!-- Edit dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <!-- form fields -->
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPage, create, update, remove } from '@/api/xxx'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNo = ref(1)
const dialogVisible = ref(false)
// ... standard CRUD logic
</script>
```

---

## Phase 8: Electron Frontend

### Task 20: Electron Project Scaffold

**Files:**
- Create: `desktop/package.json`
- Create: `desktop/electron/main.ts`
- Create: `desktop/electron/preload.ts`
- Create: `desktop/src/main.ts`
- Create: `desktop/src/App.vue`
- Create: `desktop/src/router/index.ts`

- [ ] **Step 1: Initialize Electron + Vue3 + Vite project**

Use `electron-vite` or manual setup with Vite + electron-builder.

```bash
cd desktop
npm init -y
npm install vue vue-router pinia axios element-plus @element-plus/icons-vue
npm install -D electron electron-builder vite @vitejs/plugin-vue typescript
```

- [ ] **Step 2: Write Electron main process**

```typescript
// electron/main.ts
import { app, BrowserWindow, ipcMain } from 'electron'
import path from 'path'
import fs from 'fs'

const ASSETS_DIR = path.join(app.getPath('userData'), 'AdCreater')
const TEMPLATES_DIR = path.join(ASSETS_DIR, 'templates')
const IMAGES_DIR = path.join(ASSETS_DIR, 'assets', 'images')
const VIDEOS_DIR = path.join(ASSETS_DIR, 'assets', 'videos')

function ensureDirs() {
  ;[TEMPLATES_DIR, IMAGES_DIR, VIDEOS_DIR].forEach(dir => {
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true })
  })
}

let mainWindow: BrowserWindow | null = null

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280, height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    }
  })
  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
  } else {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }
}

app.whenReady().then(() => { ensureDirs(); createWindow() })
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit() })

// IPC handlers for local file operations
ipcMain.handle('fs:save-file', async (_event, { subDir, filename, dataUrl }) => {
  const base64 = dataUrl.split(',')[1]
  const buffer = Buffer.from(base64, 'base64')
  const dir = subDir === 'videos' ? VIDEOS_DIR : IMAGES_DIR
  const monthDir = path.join(dir, new Date().toISOString().slice(0, 7))
  if (!fs.existsSync(monthDir)) fs.mkdirSync(monthDir, { recursive: true })
  const filePath = path.join(monthDir, filename)
  fs.writeFileSync(filePath, buffer)
  return filePath
})

ipcMain.handle('fs:read-dir', async (_event, subDir: string) => {
  const dir = subDir === 'videos' ? VIDEOS_DIR : IMAGES_DIR
  if (!fs.existsSync(dir)) return []
  return fs.readdirSync(dir, { withFileTypes: true })
    .filter(d => d.isDirectory())
    .map(d => d.name)
})

ipcMain.handle('fs:list-files', async (_event, { subDir, month }: { subDir: string, month: string }) => {
  const base = subDir === 'videos' ? VIDEOS_DIR : IMAGES_DIR
  const dir = path.join(base, month)
  if (!fs.existsSync(dir)) return []
  return fs.readdirSync(dir).map(f => ({
    name: f,
    path: path.join(dir, f),
    size: fs.statSync(path.join(dir, f)).size,
    mtime: fs.statSync(path.join(dir, f)).mtime,
  }))
})

ipcMain.handle('fs:delete-file', async (_event, filePath: string) => {
  if (fs.existsSync(filePath)) fs.unlinkSync(filePath)
})

ipcMain.handle('fs:get-template-dir', () => TEMPLATES_DIR)
ipcMain.handle('fs:get-assets-dir', () => ASSETS_DIR)
```

- [ ] **Step 3: Write preload script**

```typescript
// electron/preload.ts
import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electronAPI', {
  saveFile: (opts: { subDir: string, filename: string, dataUrl: string }) =>
    ipcRenderer.invoke('fs:save-file', opts),
  readDir: (subDir: string) => ipcRenderer.invoke('fs:read-dir', subDir),
  listFiles: (opts: { subDir: string, month: string }) =>
    ipcRenderer.invoke('fs:list-files', opts),
  deleteFile: (filePath: string) => ipcRenderer.invoke('fs:delete-file', filePath),
  getTemplateDir: () => ipcRenderer.invoke('fs:get-template-dir'),
  getAssetsDir: () => ipcRenderer.invoke('fs:get-assets-dir'),
})
```

- [ ] **Step 4: Configure Vue router for desktop pages**

```typescript
// src/router/index.ts
import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/pages/LoginPage.vue') },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', component: () => import('@/pages/HomePage.vue'), meta: { title: '首页' } },
      { path: 'ad/create', component: () => import('@/pages/AdCreatePage.vue'), meta: { title: '广告制作' } },
      { path: 'ad/preview', component: () => import('@/pages/AdPreviewPage.vue'), meta: { title: '广告预览' } },
      { path: 'delivery', component: () => import('@/pages/DeliveryPage.vue'), meta: { title: '广告投放' } },
      { path: 'templates', component: () => import('@/pages/TemplatesPage.vue'), meta: { title: '模板管理' } },
      { path: 'resources', component: () => import('@/pages/ResourcesPage.vue'), meta: { title: '资源管理' } },
      { path: 'billing', component: () => import('@/pages/BillingPage.vue'), meta: { title: '点数充值' } },
    ]
  }
]

export default createRouter({ history: createWebHashHistory(), routes })
```

- [ ] **Step 5: Verify Electron app launches**

Run: `cd desktop && npm run dev`
Expected: Electron window opens with Vue app

---

### Task 21-26: Electron Business Pages

**Task 21**: LoginPage (OAuth2 password grant login → store token → redirect)
**Task 22**: MainLayout (sidebar nav + header with balance display + router-view)
**Task 23**: HomePage (quick actions + recent projects + usage summary cards)
**Task 24**: AdCreatePage (4-step wizard: copy → image → video → preview)
**Task 25**: DeliveryPage (digital human panel + platform export with size preview)
**Task 26**: TemplatesPage + ResourcesPage + BillingPage

Each page calls backend API via axios client (similar to admin frontend pattern) and interacts with Electron IPC for local file operations.

---

## Phase 9: Integration

### Task 27: Backend Integration Tests

- Test each API endpoint with Spring Boot Test + Testcontainers (MySQL)
- Verify SSE endpoints with WebTestClient
- Test billing: pre-consume → success → confirm / failure → rollback flow
- Test template sync: server new version → client reports old → receives update

### Task 28: E2E Smoke Test

- Electron: Login → Create image ad → view result → save locally
- Admin: Login → Create user → recharge points → configure model → create template
- Verify generated files exist on local filesystem

---

## Execution Order

```
Phase 1: Task 1-2   (Infrastructure)
   ↓
Phase 2: Task 3-6   (module-ai)
   ↓
Phase 3: Task 7-9   (module-billing)    ← can parallel with Phase 4
Phase 4: Task 10     (module-template)   ← can parallel with Phase 3
   ↓
Phase 5: Task 11-12  (module-ad)        ← depends on Phase 2+3
Phase 6: Task 13     (module-delivery)   ← depends on Phase 2+3
   ↓
Phase 7: Task 14-19  (Admin Frontend)   ← depends on all backend APIs
Phase 8: Task 20-26  (Electron Frontend) ← depends on all backend APIs
   ↓
Phase 9: Task 27-28  (Integration)
```
