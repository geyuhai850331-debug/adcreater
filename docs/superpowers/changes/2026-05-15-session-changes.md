# 2026-05-15 会话修改文档

> 本次会话完成"广告文案翻译"→"广告营销策划"全栈改造 + 3项修正 + 启动适配。

---

## 一、会话概览

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 广告营销策划后端实现（MarketingController + AI Adapter + Prompt） | 已完成 |
| Phase 2 | 广告营销策划前端实现（MarketingPanel + AdCreatePage + AdPreviewPanel + mock） | 已完成 |
| Phase 3 | 3项修正：删除中文广告词 / 文案重命名 / 可编辑+重新生成 | 已完成 |
| Phase 4 | 启动适配：模型编排硬编码 fallback + 系统 API Mock Bean | 已完成 |

---

## 二、后端修改（adcreater-server）

### 2.1 新建文件

#### 2.1.1 OpenAIChatAdapter.java（首次文本类 AI 适配器）

- **路径：** `djb-adcreater-module-ai/src/main/java/com/djb/module/ai/adapter/OpenAIChatAdapter.java`
- **功能：** 实现 `AiModelAdapter` 接口，调用 `/v1/chat/completions`（OpenAI 兼容协议），支持 gpt-4o-mini 模型
- **Key:** 此前系统只有图片生成适配器，Chat Adapter 填补了文本/对话类 AI 调用的空白
- **错误处理：** try/catch 包裹全文，null/empty guard 防护 choices 列表和 content 字段

#### 2.1.2 MarketingController.java（取代旧 AdCopyController）

- **路径：** `djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/MarketingController.java`
- **端点：**
  - `POST /api/ad/marketing/analyze` — 完整营销分析（5 积分），返回 risk_level / culture_notes / core_strategy / example_ad_copy
  - `POST /api/ad/marketing/regenerate-copy` — 仅重新生成文案（2 积分），接收编辑后的 culture_notes + core_strategy
- **AI 响应解析：** 3 层 fallback（Jackson 直接解析 → 正则提取 JSON 块 → 原始文本兜底）
- **计费：** `BillingService.preConsume()` → AI 调用 → `confirmConsume()` / `rollbackConsume()`

#### 2.1.3 MarketingAnalyzeReqVO.java

- **路径：** `djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/MarketingAnalyzeReqVO.java`
- **字段：** productName (@NotBlank), productDescription (@NotBlank), chineseAdCopy (可选), targetMarket (@NotBlank)

#### 2.1.4 MarketingAnalyzeRespVO.java

- **路径：** `djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/MarketingAnalyzeRespVO.java`
- **字段：** riskLevel, cultureNotes, coreStrategy, exampleAdCopy

#### 2.1.5 RegenerateCopyReqVO.java

- **路径：** `djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/RegenerateCopyReqVO.java`
- **字段：** productName, productDescription, chineseAdCopy, targetMarket, riskLevel, cultureNotes, coreStrategy

#### 2.1.6 MockSystemApiConfig.java（启动适配）

- **路径：** `djb-adcreater-module-ad/src/main/java/com/djb/module/ad/config/MockSystemApiConfig.java`
- **功能：** 系统模块未上线时的临时桩，提供 7 个 `CommonApi` Mock Bean：
  - `PermissionCommonApi` — 权限校验全放行
  - `OAuth2TokenCommonApi` — Token 校验全放行
  - `ApiErrorLogCommonApi` — 错误日志静默丢弃
  - `ApiAccessLogCommonApi` — 访问日志静默丢弃
  - `TenantCommonApi` — 租户校验全放行
  - `DictDataCommonApi` — 字典数据返回空列表
  - `OperateLogCommonApi` — 操作日志静默丢弃
- **生效条件：** `@ConditionalOnMissingBean` — 真实 Bean 存在时自动失效

### 2.2 修改文件

#### 2.2.1 PromptCategoryEnum.java

- **路径：** `djb-adcreater-common/src/main/java/com/djb/common/enums/PromptCategoryEnum.java`
- **变更：** 新增两个枚举值：
  - `MARKETING("marketing", "营销策划")`
  - `MARKETING_COPY("marketing_copy", "营销文案生成")`

#### 2.2.2 PromptServiceImpl.java

- **路径：** `djb-adcreater-module-ai/src/main/java/com/djb/module/ai/service/PromptServiceImpl.java`
- **变更：** `buildDefaultPrompt()` 新增两个 condition：
  - `"marketing"` → 跨境电商营销专家 prompt（中文，输出 JSON）
  - `"marketing_copy"` → 产品文案本地化专家 prompt（中文，输出纯文本）

#### 2.2.3 ModelOrchestrationServiceImpl.java

- **路径：** `djb-adcreater-module-ai/src/main/java/com/djb/module/ai/service/ModelOrchestrationServiceImpl.java`
- **变更：** 
  - 新增 `@Value` 注入 `spring.ai.openai.api-key` 和 `spring.ai.openai.base-url`
  - 新增 `executeWithFallbackConfig()` 方法：DB 无模型配置时，使用 application.yaml 中的 OpenAI 配置构造硬编码 fallback
  - 模型管理上线后删除此方法即可走 DB 查询路径

### 2.3 删除文件

| 文件 | 原因 |
|------|------|
| `AdCopyController.java` | 被 MarketingController 取代 |
| `TranslateReqVO.java` | 被 MarketingAnalyzeReqVO 取代 |
| `TranslateRespVO.java` | 被 MarketingAnalyzeRespVO 取代 |

---

## 三、前端修改（desktop）

### 3.1 新建文件

#### 3.1.1 MarketingPanel.vue

- **路径：** `desktop/src/pages/components/MarketingPanel.vue`
- **功能：** 广告营销策划 Step 1 组件
- **表单字段：** productName (必填), productDescription (必填), targetMarket (必填)
- **已移除：** 中文广告词字段（修正 #1）
- **分析结果卡片：**
  - 合规评估（el-tag 展示 risk_level）
  - 文化本土化建议（el-input textarea，**可编辑**）
  - 核心营销策略（el-input textarea，**可编辑**）
  - 示例文案（带复制按钮，"示例广告词" → "示例文案"，修正 #2）
- **重新生成按钮：** 基于编辑后的 cultureNotes + coreStrategy 调用 `/ad/marketing/regenerate-copy`（修正 #3）

### 3.2 修改文件

#### 3.2.1 AdCreatePage.vue

- **路径：** `desktop/src/pages/AdCreatePage.vue`
- **变更：**
  - 导入 MarketingPanel 替代旧 AdCopyPanel
  - Step 1 标题改为 `{ title: '策划', desc: '营销分析 & 文案生成' }`
  - data ref `adCopy` → `marketingData`

#### 3.2.2 AdPreviewPanel.vue

- **路径：** `desktop/src/pages/components/AdPreviewPanel.vue`
- **变更：**
  - "商品描述" tab → "营销分析" tab（4 张分析卡片）
  - 移除"中文广告词"引用
  - "示例广告词" → "示例文案"
  - 新增 `DocumentCopy` 图标导入和 `copyAdCopy()` 方法
  - 新增 `.analysis-cards` CSS 样式

#### 3.2.3 mock/index.ts

- **路径：** `desktop/mock/index.ts`
- **变更：**
  - 替换 `/api/ad/copy/translate` → `/api/ad/marketing/analyze`（返回 4 字段 JSON，6 个市场）
  - 新增 `/api/ad/marketing/regenerate-copy` handler

### 3.3 删除文件

| 文件 | 原因 |
|------|------|
| `AdCopyPanel.vue` | 被 MarketingPanel.vue 取代 |

---

## 四、文档文件

| 文件 | 说明 |
|------|------|
| `docs/superpowers/specs/2026-05-15-ad-marketing-strategy-design.md` | 设计规格文档 |
| `docs/superpowers/plans/2026-05-15-ad-marketing-strategy.md` | 实施计划文档（9 任务） |
| `docs/superpowers/changes/2026-05-15-session-changes.md` | 本文件 — 会话修改汇总 |

---

## 五、Git 提交记录

| Commit | 描述 |
|--------|------|
| `8cf773e` | feat: 广告营销策划后端 — MarketingController + OpenAIChatAdapter + Prompt |
| `(中间提交)` | feat: 广告营销策划前端 — MarketingPanel + AdCreatePage + AdPreviewPanel + mock |
| `61394e1` | fix: 营销策划面板3项修正 — 删除中文广告词/文案重命名/可编辑+重新生成 |
| `(未提交)` | 启动适配：ModelOrchestrationService 硬编码 fallback + MockSystemApiConfig |

---

## 六、启动说明

### 环境依赖
- MySQL: 192.168.10.218:3306 (djb-framework-master)
- Redis: 192.168.10.218:6379 (database 1)
- OpenAI 兼容 API: `spring.ai.openai.api-key` 配置在 application.yaml

### 启动命令

```bash
# 后端（端口 48080）
cd adcreater-server
mvn install -Dmaven.test.skip=true -Dproject.build.sourceEncoding=UTF-8
java -jar djb-server/target/djb-server.jar --spring.profiles.active=local

# 前端（端口 5173）
cd desktop
npx vite --host 0.0.0.0 --port 5173
```

### Mock 模式
- `djb.security.mock-enable: true` — 登录态 Mock 已启用
- 7 个 CommonApi Mock Bean 自动激活（`@ConditionalOnMissingBean`）
- Prompt 模板：数据库无数据时使用硬编码默认 prompt
- 模型编排：DB 无模型配置时使用 application.yaml 中的 OpenAI 配置
