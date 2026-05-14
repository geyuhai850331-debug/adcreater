# AdCreater 系统设计文档

> 日期: 2026-05-13 | 状态: Draft

## 1. 项目概述

AdCreater 是一个面向**跨境电商**的全栈式广告创建平台，帮助中国商家将商品推广到全球海外市场。提供从广告文案翻译/本地化、图片生成、视频制作到多平台适配投放的一站式解决方案。

### 1.1 目标用户

- 跨境电商卖家（中小型为主）
- 面向全球多市场投放（欧美、东南亚、中东等）
- 需要高效批量制作多尺寸多语言广告素材

### 1.2 核心约束

- **素材隐私**: 生成的广告素材（图片/视频）全部存储在用户本地，不上传服务器
- **AI 模型**: 全部使用第三方 API（OpenAI、Stability AI、HeyGen、翻译 API），不自建模型
- **部署模式**: DJB Backend 单机版（单体部署，可扩展多租户 SaaS）
- **模板同步**: 管理端发布模板 → 客户端单向拉取同步，本地模板不上传

---

## 2. 技术栈

| 层 | 技术 | 说明 |
|---|---|---|
| 用户前端 | Electron + Vue3 + Vite | 桌面应用，本地资源管理 |
| 管理前端 | Vue3 + Element Plus + Vite | Web 管理后台，复用 yudao-ui-admin-vue3 |
| 后端框架 | DJB Backend (Spring Boot) | 复用认证/鉴权/用户/RBAC/多租户基础设施 |
| 数据库 | MySQL + Redis | 业务数据 + 缓存/分布式锁 |
| AI 模型 | 第三方 API | OpenAI, Stability AI, HeyGen, DeepL/Google Translate |
| 存储 | 本地文件系统 (Electron 端) | 素材不落服务器 |
| 通信 | HTTPS REST + SSE | OAuth2 Token 认证，长任务 SSE 推送进度 |

---

## 3. 系统架构

```
┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐
│  用户前端 (桌面)   │  │  管理前端 (Web)   │  │   第三方 AI API    │
│  Electron + Vue3  │  │  Vue3 + Ele Plus │  │                   │
│                  │  │                  │  │  OpenAI / Stable  │
│  广告制作/投放    │  │  用户/计费/配置   │  │  HeyGen / 翻译    │
│  模板/资源/充值   │  │  模板/模型/Prompt│  │                   │
└────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘
         │                     │                       │
         │   HTTPS / REST      │   HTTPS / REST        │  HTTPS
         └──────────┬──────────┘                       │
                    │                                  │
┌───────────────────┴──────────────────────────────────┴──────────┐
│                      DJB Backend (djb-framework)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  API 层      │  │  业务服务层   │  │  基础设施层               │  │
│  │             │  │             │  │  Spring Boot + Security  │  │
│  │ 用户 API     │  │ 广告/投放    │  │  MyBatis-Plus            │  │
│  │ 管理 API     │  │ AI 编排      │  │  MySQL + Redis           │  │
│  │ 模板 API     │  │ 计费/模板    │  │  OAuth2 + RBAC           │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. 后端模块设计

基于 DJB 多模块 Maven 架构，新增 5 个业务模块，运行于 djb-framework 基础设施之上：

```
adcreater-server/
├── adcreater-server-common/          # 共享: 枚举、异常、工具类
├── djb-framework/                    # DJB 框架层 (复用)
├── module-system/                    # DJB 系统模块 (复用)
│
├── module-ad/                        # ★ 广告制作模块
├── module-delivery/                  # ★ 广告投放模块
├── module-template/                  # ★ 模板管理模块
├── module-ai/                        # ★ AI 编排模块
└── module-billing/                   # ★ 计费模块
```

### 4.1 模块职责

| 模块 | 职责 | 关键类 |
|---|---|---|
| module-ad | 广告内容生产：文案翻译、图片生成、视频生成 | AdCopyService, AdCreativeService |
| module-delivery | 投放管理：数字人生成、多平台素材适配 | DigitalHumanService, PlatformDeliveryService |
| module-template | 模板 CRUD + 版本管理 + 客户端同步 | TemplateService (管理端+客户端双 Controller) |
| module-ai | 第三方 AI API 统一封装，Prompt 模板管理 | ModelOrchestrationService, PromptService, Adapter 层 |
| module-billing | 点数充值/扣减/流水，用量统计 | BillingService, UsageService |

### 4.2 模块依赖关系

```
module-ad ──────→ module-ai
module-delivery ─→ module-ai
module-ad ──────→ module-billing
module-delivery ─→ module-billing
module-template  (独立，无外部依赖)
module-ai ──────→ module-billing (扣减)
所有模块 ──────→ module-system (用户/RBAC)
```

---

## 5. AI 模型编排层 (核心)

### 5.1 Adapter 模式

```java
public interface AiModelAdapter {
    AiResult call(AiRequest request, AiModelConfigDO config);
    boolean validateConfig(AiModelConfigDO config);
    int estimateCost(AiRequest request);
}
```

Spring Bean 自动发现所有 Adapter 实现。新增模型只需实现接口 + 数据库加配置。

### 5.2 调用链 (6 步)

1. 查询 `ai_model_config` 表获取模型配置
2. 查询 `prompt_template` 表获取 Prompt 模板，`{{占位符}}` 替换为用户输入
3. `billingService.preConsume(userId, cost)` 预扣点数
4. `adapter.call(prompt, modelConfig)` 调用第三方 API
5. 成功 `confirmConsume` / 失败 `rollbackConsume`
6. `usageService.record()` 记录用量

### 5.3 降级策略

同类型多模型按 `ai_model_config.priority` 排序，主模型失败自动 fallback 到备选。

### 5.4 Prompt 模板

占位符语法 `{{variable|default}}`，运营在后台编辑即可调优，无需改代码或重启服务。

Prompt 模板按 category 分类：

| 分类 | code | 用途 |
|---|---|---|
| 文案 | `copy` | 广告文案翻译/本地化 |
| 图片 | `image` | 文生图 / 套图生成 |
| 视频 | `video` | 简易视频生成 |
| 视频-分镜 | `video_storyboard` | 分镜脚本策划 |
| 视频-关键帧 | `video_keyframe` | 关键帧描述生成 |
| 视频-关键帧图片 | `video_keyframe_image` | 单帧关键帧图片生成 |
| 视频-关键帧网格 | `video_keyframe_grid` | 4 宫格关键帧网格生成 |
| 数字人 | `digital_human` | 数字人口播视频 |

---

## 6. 数据库设计

### 6.1 新增表清单

| 表名 | 归属模块 | 说明 |
|---|---|---|
| `ad_task` | module-ad | 广告制作任务记录 |
| `ad_delivery_task` | module-delivery | 投放任务记录 |
| `ad_template` | module-template | 广告模板 |
| `ad_template_version` | module-template | 模板版本 (支持增量同步) |
| `ad_model_config` | module-ai | AI 模型配置 (API Key 加密存储) |
| `ad_prompt_template` | module-ai | Prompt 模板 |
| `ad_user_points_account` | module-billing | 用户点数账户 (1:1 system_users) |
| `ad_points_transaction` | module-billing | 点数流水 (只 INSERT) |
| `ad_usage_record` | module-billing | 用量记录 (按模型/用户/时间) |

### 6.2 核心表结构

```sql
-- 广告任务 (DJB 合规: tenant_id + 审计列 + ad_ 前缀 + 中文 COMMENT)
ad_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    type VARCHAR(32) NOT NULL COMMENT '任务类型: copy/image/video',
    input_params JSON COMMENT '请求参数',
    result JSON COMMENT '生成结果',
    status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
    model_used VARCHAR(64) COMMENT '使用的 AI 模型',
    points_cost INT DEFAULT 0 COMMENT '消耗点数',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
);

-- 模型配置 (API Key AES 加密存储)
ad_model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    model_name VARCHAR(64) NOT NULL COMMENT '模型名称',
    adapter_class VARCHAR(255) NOT NULL COMMENT '适配器全限定类名',
    api_key VARCHAR(512) NOT NULL COMMENT 'API Key (AES 加密)',
    endpoint_url VARCHAR(255) COMMENT 'API 端点 URL',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    priority INT DEFAULT 0 COMMENT '优先级 (降级用)',
    extra_config JSON COMMENT '扩展配置 (计费单价等)',
    creator/updater/create_time/update_time/deleted  -- 标准审计列
);

-- Prompt 模板 (含 {{variable|default}} 占位符)
ad_prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL COMMENT '模板名称',
    model_config_id BIGINT COMMENT '关联模型配置 ID',
    template_content TEXT NOT NULL COMMENT '模板内容',
    variables JSON COMMENT '占位符列表及默认值',
    category VARCHAR(32) NOT NULL COMMENT '分类',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    creator/updater/create_time/update_time/deleted  -- 标准审计列
);

-- 点数账户 (1:1 system_users)
ad_user_points_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户 ID',
    balance INT DEFAULT 0 COMMENT '当前余额',
    total_earned INT DEFAULT 0 COMMENT '累计充值',
    total_spent INT DEFAULT 0 COMMENT '累计消费',
    creator/updater/create_time/update_time/deleted  -- 标准审计列
);

-- 点数流水 (不可变, 只 INSERT)
ad_points_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    type VARCHAR(16) NOT NULL COMMENT '类型: earn/consume/refund',
    amount INT NOT NULL COMMENT '金额',
    balance_after INT NOT NULL COMMENT '操作后余额',
    status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT '状态',
    biz_id BIGINT COMMENT '关联业务 ID',
    remark VARCHAR(255) COMMENT '备注',
    creator/updater/create_time/update_time/deleted  -- 标准审计列
);
```

### 6.3 设计原则

- **DJB 多租户**: 每表含 `tenant_id` 列，默认为 0 (单租户)，后续可扩展 SaaS
- **DJB 审计列**: 每表含 `creator/create_time/updater/update_time/deleted`，由框架拦截器自动填充
- **DJB 表命名**: 统一 `ad_` 前缀，字符集 `utf8mb4_unicode_ci`，每列含中文 COMMENT
- **任务表 JSON 列**: 灵活适配不同任务类型的参数和结果
- **API Key 加密**: `ad_model_config.api_key` Base64 编码存储 (生产应改用 AES)
- **点数流水不可变**: `ad_points_transaction` 只 INSERT 不 UPDATE
- **模板版本化**: `ad_template` + `ad_template_version` 支持增量同步
- **并发控制**: 点数扣减使用 DB 行锁 (`SELECT FOR UPDATE`) 或 Redis 分布式锁

---

## 7. API 设计

### 7.1 用户端 API (`/api/ad/*`)

| 方法 | 路径 | 说明 | 进度推送 |
|---|---|---|---|
| POST | `/api/ad/copy/translate` | 广告文案翻译+本地化 | - |
| POST | `/api/ad/creative/image/text-to-image` | 文生图 | SSE |
| POST | `/api/ad/creative/image/batch` | 套图生成 (多尺寸) | SSE |
| POST | `/api/ad/creative/video` | 广告视频生成 (简易模式) | SSE |
| POST | `/api/ad/video/storyboard/generate` | 分镜策划：根据商品描述生成分镜脚本 | - |
| POST | `/api/ad/video/storyboard/regenerate` | 重新生成单个分镜的关键帧描述 | - |
| POST | `/api/ad/video/keyframe/generate` | 生成单个关键帧图片 | SSE |
| POST | `/api/ad/video/keyframe/grid` | 生成 4 宫格关键帧网格图 | SSE |
| POST | `/api/ad/video/generate` | 视频合成：分镜+关键帧+TTS+BGM 完整合成 | SSE |

### 7.2 投放 API (`/api/delivery/*`)

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/delivery/digital-human` | 数字人视频生成 (SSE) |
| POST | `/api/delivery/platform/export` | 多平台素材导出 |

### 7.3 模板 API (`/api/template/*`)

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/template/client/sync` | 客户端增量同步 (上报版本→返回差异) |
| GET | `/api/template/client/download/:id` | 下载模板文件 |

### 7.4 管理端 API (`/admin-api/ad/*`)

管理端 API 复用 yudao 标准 CRUD 模式：`/page`、`/create`、`/update`、`/delete-list`。

### 7.5 通用约定

- 响应格式: `{ "code": 0, "data": {...}, "message": "Success" }`（DJB `CommonResult`，使用 `success()` 静态导入）
- 认证: OAuth2 Bearer Token
- 进度推送: SSE `text/event-stream`，事件格式 `{ "progress": 30, "status": "processing" }`

---

## 8. 模板同步机制

单向同步 (Server → Client)：

1. Electron 启动时请求 `POST /api/template/client/sync`，上报本地版本清单
2. Server 对比版本差异，返回 `{ updated: [...], deleted: [...] }`
3. 客户端下载新增/更新的模板文件，写入本地模板目录
4. 更新本地 `manifest.json` 版本缓存

本地自定义模板版本号恒为 0，永不上传服务器。

---

## 9. 计费体系

### 9.1 状态机

```
发起 AI 请求
  → 检查余额 (余额不足返回 402)
  → 预扣点数 (INSERT transaction status=pending)
  → 调用 AI API
      → 成功: 确认扣减 (UPDATE status=confirmed)
      → 失败: 回滚 (UPDATE status=rolled_back, balance += cost)
  → 记录用量
```

### 9.2 计费规则

- 虚拟点数制，非真实货币
- 管理员手动充值
- 计费维度: 任务类型 × AI 模型，单价配置在 `ai_model_config.extra_config`
- 后续可扩展微信/支付宝支付

---

## 10. 用户前端 (Electron + Vue3)

### 10.1 路由结构

| 路由 | 页面 | 说明 |
|---|---|---|
| `/login` | LoginPage | 用户登录 |
| `/home` | HomePage | 工作台 (快速入口、最近项目、余额) |
| `/ad/create` | AdCreatePage | 广告制作主工作区 (4 步向导) |
| `/delivery` | DeliveryPage | 广告投放 (数字人 + 平台导出) |
| `/templates` | TemplatesPage | 模板浏览 + 同步状态 |
| `/resources` | ResourcesPage | 本地素材管理 |
| `/billing` | BillingPage | 点数余额 + 流水 |

### 10.2 广告制作工作流 (4 步向导, 视频为 3 子步骤)

```
步骤 1: 文案翻译 (AdCopyPanel)
  → 输入商品信息 → 选择目标市场 → 获取翻译+本地化文案

步骤 2: 图片生成 (AdImagePanel)
  → 选择模板/尺寸 → 构建 Prompt → SSE 生成 → 预览

步骤 3: 视频生成 (AdVideoPanel) — 3 个子步骤
  → 步骤 3-0: 分镜策划 (Storyboard Planning)
       输入商品描述/类目/目标平台/语言 → 选择视频模板
       → AI 生成分镜脚本 (每个场景 4 个关键帧)
  → 步骤 3-1: 关键帧生成 (Keyframe Generation)
       为每个场景生成 4 宫格关键帧网格图
       → 支持编辑/重新生成单个关键帧
  → 步骤 3-2: 视频合成 (Video Compositing)
       配置时长/TTS 语音/BGM 背景音乐
       → SSE 流式合成最终广告视频

步骤 4: 全屏预览 (AdPreviewPage)
  → 所有成品汇总展示 → 下载/导出
```

### 10.3 本地资源目录

```
~/AdCreater/
├── assets/images/2026-05/     # 生成的广告图片 (按月)
├── assets/videos/2026-05/     # 生成的广告视频
├── assets/exports/            # 平台投放导出文件
├── templates/                 # Server 同步的模板
│   └── manifest.json          # 本地版本清单
├── cache/                     # 缩略图缓存
└── data/history.db            # SQLite 制作历史缓存
```

### 10.4 设计原则

- **本地优先**: 素材/模板存本地，Electron Main Process 提供 fs IPC
- **步骤式引导**: 降低用户认知负荷
- **SSE 进度**: EventSource 接收实时进度
- **离线可用**: 模板和已有资源离线可浏览

---

## 11. 管理前端 (Vue3 Web)

复用 yudao-ui-admin-vue3 框架的 Layout、登录、权限体系，新增 5 个业务页面：

| 路由 | 页面 | 说明 |
|---|---|---|
| `/dashboard` | DashboardPage | 仪表盘 (调用统计、用户数、消耗) |
| `/user` | UserListPage | 用户管理 + 点数充值 |
| `/billing` | BillingPage | 充值流水 + 用量统计 |
| `/ai/models` | ModelConfigPage | 模型配置 (增删改、连接测试) |
| `/ai/prompts` | PromptConfigPage | Prompt 模板管理 (高亮占位符、预览) |
| `/templates` | TemplateManagePage | 模板上传/发布/版本管理 |

### 11.1 权限矩阵

| 角色 | 用户管理 | 充值 | 模型配置 | Prompt | 模板 | 仪表盘 |
|---|---|---|---|---|---|---|
| admin | 全部 | 全部 | 全部 | 全部 | 全部 | 全部 |
| operator | 查看 | 充值 | - | 全部 | 全部 | 查看 |
| viewer | 查看 | - | - | - | 查看 | 查看 |

### 11.2 安全注意事项

- Model 配置页 API Key 展示时脱敏（`sk-****xxxx`）
- 编辑/新增时 API Key 加密传输
- Prompt 模板编辑器高亮 `{{变量}}` 占位符并自动提取变量列表

---

## 12. 错误处理策略

| 场景 | HTTP 状态码 | 处理方式 |
|---|---|---|
| 未认证 | 401 | 跳转登录页 |
| 无权限 | 403 | 提示无权限 |
| 点数不足 | 402 | 前端弹窗引导充值 |
| 参数校验失败 | 400 | 返回字段级错误信息 |
| AI API 超时 | 500 | SSE 推送失败事件，点数回滚 |
| AI API 限流 | 429 | 指数退避重试，最多 3 次 |

统一错误响应格式：`{ "code": -1, "message": "错误描述", "data": null }`

## 13. 测试策略

| 层级 | 范围 | 工具 |
|---|---|---|
| 单元测试 | Service 层业务逻辑，Adapter 层 | JUnit 5 + Mockito |
| 集成测试 | Controller API 端点，数据库操作 | Spring Boot Test + Testcontainers |
| E2E | 关键用户流程 (登录→制作→下载) | Playwright (Electron 端) |
| 合约测试 | API 响应格式兼容性 | Spring Cloud Contract (可选) |

## 14. 技术决策记录

| 决策 | 选项 | 理由 |
|---|---|---|
| AI 调用模式 | 全部第三方 API | 用户选定，无需 GPU，按量付费 |
| 后端框架 | DJB Backend (Spring Boot) | 用户选定，复用用户/RBAC/多租户/代码生成 |
| 素材存储 | Electron 本地 | 用户隐私要求，不上传服务器 |
| 模板同步 | Server→Client 单向 | 本地模板不上传，管理端统一发布 |
| 长任务进度 | SSE | 简单可靠，无需 WebSocket 双向通信 |
| 模块化 | Monorepo 新增模块 | 复用 DJB (djb-framework) 基础设施，减少重复造轮子 |
| AI 模型扩展 | Adapter 模式 | 新模型只需实现接口+数据库配置，热插拔 |

---

## 15. DJB Backend Framework 合规适配

> 2026-05-14: 适配 DJB 后端框架 (djb-framework) 规范

### 15.1 适配背景

adcreater-server 原先基于 yudao-cloud (Spring Boot) 框架，现需在 DJB Backend (djb-backend) 工程下启动。DJB 框架有一套严格的模块分层规范和命名约定，需进行全面适配。

### 15.2 核心变更清单

| 层次 | 变更项 | 变更前 (yudao) | 变更后 (DJB) |
|---|---|---|---|
| **DO** | 基类 | `extends BaseDO` | `extends TenantBaseDO` (新增 `tenantId` 多租户支持) |
| **DO** | 时间字段 | `createdAt` / `updatedAt` | `createTime` / `updateTime` (继承自 TenantBaseDO) |
| **DO** | 表名 | 无前缀，如 `ai_model_config` | `ad_` 前缀，如 `ad_model_config` |
| **DO** | 注解 | `@TableName("ad_xxx")` | 同左, `@EqualsAndHashCode(callSuper = true)` |
| **SQL** | 审计列 | 缺 `tenant_id`, `creator`, `updater`, `deleted` | 每表均含全量审计列 |
| **SQL** | 列注释 | 部分列无中文 COMMENT | 每列含中文 COMMENT |
| **SQL** | 字符集 | `utf8mb4` | `utf8mb4_unicode_ci` |
| **SQL** | 索引 | 缺 `idx_tenant_id` | 每表含 `idx_tenant_id` |
| **Service** | 注解 | 缺 `@Validated`, `@Slf4j` | 每个 ServiceImpl 均含 |
| **Service** | 创建时间 | `entity.setCreatedAt(LocalDateTime.now())` | 由 TenantBaseDO 自动处理，删除手动赋值 |
| **Controller** | 注解 | 缺 `@Tag`, `@Operation`, `@Validated`, `@Slf4j` | 每个 Controller 均含 |
| **Controller** | 响应 | `CommonResult.success()` | `success()` (static import) |
| **VO** | 验证 | String 字段仅 `@NotBlank` | `@NotBlank` + `@Size(max = N)` |
| **VO** | 响应字段 | `createdAt` / `updatedAt` | `createTime` / `updateTime` |

### 15.3 受影响的文件统计

| 类别 | 修改文件数 | 说明 |
|---|---|---|
| DO 类 | 7 | AiModelConfigDO, PromptTemplateDO, UserPointsAccountDO, PointsTransactionDO, UsageRecordDO, TemplateDO, TemplateVersionDO |
| Service 类 | 5 | BillingServiceImpl, UsageServiceImpl, TemplateServiceImpl, AiModelConfigServiceImpl, PromptServiceImpl |
| Controller 类 | 10 | AdVideoController, AdCopyController, AdCreativeController, AiModelConfigController, PromptTemplateController, BillingController, BillingAppController, TemplateController, TemplateClientController, DeliveryController |
| VO 类 | 4 | AiModelConfigSaveReqVO, PromptTemplateSaveReqVO, TemplateSaveReqVO, TemplateRespVO, TemplateVersionRespVO |
| SQL DDL | 1 | init-schema.sql (9 张表完整重写) |
| Enum | 1 | PromptCategoryEnum (新增 4 个视频子类别) |

### 15.4 SQL 表命名对照

| 旧表名 | 新表名 (DJB) | 说明 |
|---|---|---|
| `ad_task` | `ad_task` | 保持不变（已有 ad_ 前缀） |
| `delivery_task` | `ad_delivery_task` | 添加 ad_ 前缀 |
| `template` | `ad_template` | 添加 ad_ 前缀 |
| `template_version` | `ad_template_version` | 添加 ad_ 前缀 |
| `ai_model_config` | `ad_model_config` | 添加 ad_ 前缀 |
| `prompt_template` | `ad_prompt_template` | 添加 ad_ 前缀 |
| `user_points_account` | `ad_user_points_account` | 添加 ad_ 前缀 |
| `points_transaction` | `ad_points_transaction` | 添加 ad_ 前缀 |
| `usage_record` | `ad_usage_record` | 添加 ad_ 前缀 |

### 15.5 关键注意事项

- **多租户**: TenantBaseDO 中的 `tenant_id` 默认为 0（单租户模式），后续可扩展为 SaaS 多租户
- **逻辑删除**: 通过 `deleted` 字段实现，MyBatis-Plus 自动拼接 `WHERE deleted = 0`
- **审计字段**: `creator`/`create_time`/`updater`/`update_time` 由 DJB 框架拦截器自动填充
- **不修改 pom.xml**: framework 依赖由 DJB 父 POM 统一管理，adcreater 模块 POM 无需显式声明

---

## 16. 风险和后续事项

- **并发点数扣减**: 需要 DB 行锁或 Redis 锁，避免超扣
- **视频生成超时**: 第三方 API 可能超时（>60s），需 SSE 保持连接 + 后台异步处理
- **大文件模板下载**: 视频模板可能较大，需断点续传支持
- **后续扩展**: 微信/支付宝支付、团队协作、多语言管理端
