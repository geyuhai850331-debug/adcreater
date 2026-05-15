# Design: 广告营销策划 (Ad Marketing Strategy)

**Date:** 2026-05-15
**Status:** Draft

## Overview

将 AdCreater 4 步广告制作向导的 Step 1 "广告文案翻译" 替换为 "广告营销策划"——基于 AI 大模型的跨境电商营销策略分析。用户输入产品信息和目标市场，AI 输出合规评估、文化本土化建议、核心营销策略和示例广告词。

## Scope

- **Desktop 前端：** `MarketingPanel.vue` 替代 `AdCopyPanel.vue`，表单 + 结构化结果卡片
- **后端：** 新端点 `POST /api/ad/marketing/analyze`，新 prompt 模板 category `marketing`
- **删除：** 旧 `AdCopyController`、旧 `TranslateReqVO/RespVO`
- **不涉及：** 数据库表结构变更、认证/鉴权变更、其他 3 步（图片/视频/预览）核心逻辑

---

## Backend Design

### API Endpoint

```
POST /api/ad/marketing/analyze
```

### Request — `MarketingAnalyzeReqVO.java`

```java
public class MarketingAnalyzeReqVO {
    @NotBlank
    private String productName;           // 产品名称
    @NotBlank
    private String productDescription;    // 产品描述（图片描述+文字描述合并）
    @NotBlank
    private String chineseAdCopy;         // 中文广告推广词
    @NotBlank
    private String targetMarket;          // 目标市场：USA, UK, Germany, Japan, SaudiArabia, Brazil
}
```

### Response — `MarketingAnalyzeRespVO.java`

```java
public class MarketingAnalyzeRespVO {
    private String riskLevel;         // "safe" | "warning"
    private String cultureNotes;      // 文化背景分析
    private String coreStrategy;      // 核心营销策略
    private String exampleAdCopy;     // 示例广告词（英文/目标市场语言）
}
```

### Controller — `MarketingController.java`

- 类注解：`@Tag(name = "用户端 - 广告营销策划")`
- 端点：`@PostMapping("/analyze")`
- 逻辑流：
  1. 构造 prompt variables map：`productName, productDescription, chineseAdCopy, targetMarket`
  2. `promptService.resolvePrompt("marketing", vars)` 获取完整 prompt
  3. `billingService.preConsume(userId, 5, bizId)` 预扣 5 点
  4. `orchestrationService.translate(result)` 调用 AI（注意：result 是 resolved prompt 文本）
  5. 解析 AI 返回的 JSON → `MarketingAnalyzeRespVO`
  6. `billingService.confirmConsume(txId)` 确认消费

### AI Prompt Template（默认值，可由管理员通过 Admin UI 覆盖）

存储在 `PromptServiceImpl.buildDefaultPrompt("marketing", vars)`。

内容：用户提供的营销专家提示词原文（见 Appendix A），通过 `{{variable}}` 占位符注入：

```
category: marketing
template: "你是一位专业的跨境电商营销专家...{{product_name}}...{{product_description}}...{{chinese_ad_copy}}...{{target_market}}..."
```

### PromptCategoryEnum

增加枚举值：`MARKETING("marketing", "营销策划")`

### Files Changed

| Operation | File |
|---|---|
| **New** | `djb-adcreater-module-ad/.../controller/MarketingController.java` |
| **New** | `djb-adcreater-module-ad/.../controller/vo/MarketingAnalyzeReqVO.java` |
| **New** | `djb-adcreater-module-ad/.../controller/vo/MarketingAnalyzeRespVO.java` |
| **Modify** | `djb-adcreater-common/.../enums/PromptCategoryEnum.java` — add `MARKETING` |
| **Modify** | `djb-adcreater-module-ai/.../service/PromptServiceImpl.java` — add default prompt |
| **Delete** | `djb-adcreater-module-ad/.../controller/AdCopyController.java` |
| **Delete** | `djb-adcreater-module-ad/.../controller/vo/TranslateReqVO.java` |
| **Delete** | `djb-adcreater-module-ad/.../controller/vo/TranslateRespVO.java` |

---

## Frontend Design

### MarketingPanel.vue (replaces AdCopyPanel.vue)

**路径:** `desktop/src/pages/components/MarketingPanel.vue`

**表单字段：**
| Field | Type | Validation |
|---|---|---|
| 产品名称 (productName) | `<el-input>` | required |
| 产品描述 (productDescription) | `<el-input type="textarea">` (rows=3) | required |
| 中文广告词 (chineseAdCopy) | `<el-input type="textarea">` (rows=3) | required |
| 目标市场 (targetMarket) | `<el-select>` | required |

**目标市场选项:** `USA, UK, Germany, Japan, Saudi Arabia, Brazil`（与旧下拉一致）

**按钮:** `<el-button type="primary" @click="analyze">` 生成营销策划

**结果展示（4 张卡片，API 返回后展示）：**

1. **合规评估** — `<el-tag>` 显示 riskLevel（safe=success 绿, warning=warning 橙）
2. **文化本土化建议** — `<el-alert type="info">` 显示 cultureNotes
3. **核心营销策略** — `<el-card>` 高亮显示 coreStrategy
4. **示例广告词** — `<el-card>` 显示 exampleAdCopy + `<el-button>` 一键复制（`navigator.clipboard.writeText`）

**交互：**
- 调用 API 时显示 `<el-button loading>`
- 返回后自动展开结果区
- "下一步"按钮 emit `next(data)` 携带完整数据

### AdCreatePage.vue Changes

| Change | Details |
|---|---|
| 导入 | `import MarketingPanel from './components/MarketingPanel.vue'` |
| 步骤定义 | `{ title: '策划', desc: '营销分析 & 文案生成' }` |
| Data ref | `adCopy` → `marketingData` |
| 模板 | `<AdCopyPanel>` → `<MarketingPanel>` |

### AdPreviewPanel.vue Changes

| Location | Old | New |
|---|---|---|
| 左侧 tab | "商品描述" | "营销分析" |
| 内容区 | 原文/译文两栏 | 四张卡片（同 MarketingPanel 结果区风格） |
| 空状态 | "文案尚未翻译" | "尚未进行营销分析" |

### Mock API (mock/index.ts)

替换 `/api/ad/copy/translate` → `/api/ad/marketing/analyze`：
- 按 targetMarket 返回不同的 mock JSON 数据
- 新增 `generateMarketingMock(market)` 函数

### Files Changed

| Operation | File |
|---|---|
| **New** | `desktop/src/pages/components/MarketingPanel.vue` |
| **Modify** | `desktop/src/pages/AdCreatePage.vue` |
| **Modify** | `desktop/src/pages/components/AdPreviewPanel.vue` |
| **Modify** | `desktop/mock/index.ts` |
| **Delete** | `desktop/src/pages/components/AdCopyPanel.vue` |

---

## Data Flow

```
MarketingPanel
  │  emits: next({ productName, productDescription, chineseAdCopy,
  │                targetMarket, analysis: { riskLevel, cultureNotes,
  │                coreStrategy, exampleAdCopy } })
  ▼
AdCreatePage stores → marketingData (ref)
  │  passes marketingData.analysis + marketingData.exampleAdCopy to:
  ├─ AdImagePanel (image prompt can incorporate ad copy)
  ├─ AdVideoPanel (video script can incorporate ad copy)
  └─ AdPreviewPanel (displays full analysis in "营销分析" tab)
```

## Error Handling

- **AI 返回非 JSON：** 正则提取 JSON 块，失败则显示原始文本在 cultureNotes 中
- **网络超时：** Axios 60s 超时，显示"分析超时，请重试"
- **点数不足：** 后端返回 402，前端 ElMessage 提示充值
- **模型全部失败（fallback 耗尽）：** 返回 500，前端显示"AI 服务暂时不可用"

## Billing

- 每次分析消费 **5 points**（与旧文案翻译一致）
- 使用 `billingService.preConsume()` / `confirmConsume()` / `rollbackConsume()` 流程

---

## Appendix A: Default AI Prompt

```
你是一位专业的跨境电商营销专家和内容策略师。

# 任务
分析提供的产品信息（图片描述、文字描述、中文广告推广词）、目标平台、目标市场，输出一个 JSON 对象。

# 输出 JSON 结构（键名固定，值中除示例广告词外均为分析结论）
{
  "risk_level": "safe 或 warning",
  "culture_notes": "文化背景分析与本土化建议",
  "core_strategy": "核心营销策略描述",
  "example_ad_copy": "示例广告词（英文或目标市场语言），用户可直接使用或作为修改参考"
}

# 分析要求
1. **合规性检查** → 输出 risk_level
   - 检查夸大功效、品牌侵权、违禁品类等。
   - safe = 无明显风险；warning = 存在需修改的内容。
2. **文化本土化** → 输出 culture_notes
   - 考虑幽默、禁忌、信任信号、计量单位、日期格式、季节性等。
3. **核心策略** → 输出 core_strategy
   - 选择最有效的单一营销心理触发点（痛点反差、价格锚点、从众心理、稀缺性、权威背书等）。
4. **示例广告词** → 输出 example_ad_copy
   - 基于原始情报（不可遗漏任何卖点）生成一段可直接展示的文案。
   - 必须符合平台政策和目标市场语言习惯，不是直接翻译中文。
   - 长度适中，适合产品页面展示。

# 输入信息
产品名称：{{product_name}}
产品描述：{{product_description}}
中文广告推广词：{{chinese_ad_copy}}
目标市场：{{target_market}}

# 重要约束
- 只输出 JSON，不要有任何额外文字。
- 所有值使用英文双引号，不要添加注释。
- 如果缺少某些信息，基于常识合理推断并注明（在 culture_notes 中说明）。
```

## Appendix B: AI Response JSON Parsing Strategy

AI 可能不完全遵循"只输出 JSON"的指令。解析策略（优先级递减）：
1. 直接 `JSON.parse()` 整个响应
2. 正则提取 `\{[\s\S]*\}` 块后解析
3. 以上都失败 → 返回原始文本放在 `cultureNotes` 中，`riskLevel = "warning"`
