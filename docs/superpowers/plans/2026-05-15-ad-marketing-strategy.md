# 广告营销策划 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Step 1 "广告文案翻译" with AI-powered "广告营销策划" — submit product info + target market, get compliance check + culture notes + strategy + ad copy.

**Architecture:** New `MarketingController` (replaces `AdCopyController`) calls `PromptService.resolvePrompt("marketing", vars)` → `OrchestrationService.translate()` via new `OpenAIChatAdapter` (text/chat completions, fills gap where only image adapters existed). Frontend `MarketingPanel.vue` renders 4-card layout.

**Tech Stack:** Java 17 Spring Boot 3 MyBatis-Plus (backend), Vue 3 Element Plus TypeScript (frontend)

**Design Spec:** `docs/superpowers/specs/2026-05-15-ad-marketing-strategy-design.md`

---

### Task 1: Create OpenAIChatAdapter for text/chat completions

**Files:**
- Create: `adcreater-server/djb-adcreater-module-ai/src/main/java/com/djb/module/ai/adapter/OpenAIChatAdapter.java`

**Why:** Existing `OpenAIAdapter` only does DALL-E image generation (`/v1/images/generations`). The `translate()` method in `ModelOrchestrationServiceImpl` sends text prompts but no adapter handles chat completions. Without this, marketing analysis cannot call LLMs.

- [ ] **Step 1: Create OpenAIChatAdapter.java**

```java
package com.djb.module.ai.adapter;

import com.djb.module.ai.dal.dataobject.AiModelConfigDO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Component
public class OpenAIChatAdapter implements AiModelAdapter {

    private final WebClient webClient = WebClient.create();

    @Override
    public AiResult call(AiRequest request, AiModelConfigDO config) {
        String apiKey = new String(Base64.getDecoder().decode(config.getApiKey()));
        String endpoint = (config.getEndpointUrl() != null ? config.getEndpointUrl() : "https://api.openai.com")
                + "/v1/chat/completions";

        String model = request.getModel() != null ? request.getModel() : "gpt-4o-mini";

        List<Map<String, String>> messages = List.of(
            Map.of("role", "user", "content", request.getPrompt())
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2048);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.post()
            .uri(endpoint)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        @SuppressWarnings("unchecked")
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        int inputTokens = usage != null && usage.get("prompt_tokens") instanceof Number
            ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
        int outputTokens = usage != null && usage.get("completion_tokens") instanceof Number
            ? ((Number) usage.get("completion_tokens")).intValue() : 0;

        return AiResult.builder()
            .success(true)
            .revisedPrompt(content)
            .inputTokens(inputTokens)
            .outputTokens(outputTokens)
            .build();
    }

    @Override
    public boolean validateConfig(AiModelConfigDO config) {
        try {
            String apiKey = new String(Base64.getDecoder().decode(config.getApiKey()));
            String endpoint = (config.getEndpointUrl() != null ? config.getEndpointUrl() : "https://api.openai.com");
            Map<String, Object> response = webClient.get()
                .uri(endpoint + "/v1/models")
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
        return 5;
    }
}
```

- [ ] **Step 2: Verify the new adapter compiles**

Run: `cd adcreater-server && mvn compile -pl djb-adcreater-module-ai -am -q`

- [ ] **Step 3: Commit**

```bash
git add adcreater-server/djb-adcreater-module-ai/src/main/java/com/djb/module/ai/adapter/OpenAIChatAdapter.java
git commit -m "feat(ai): add OpenAIChatAdapter for text/chat completions"
```

---

### Task 2: Add MARKETING prompt category and default prompt

**Files:**
- Modify: `adcreater-server/djb-adcreater-common/src/main/java/com/djb/common/enums/PromptCategoryEnum.java`
- Modify: `adcreater-server/djb-adcreater-module-ai/src/main/java/com/djb/module/ai/service/PromptServiceImpl.java`

- [ ] **Step 1: Add MARKETING enum value to PromptCategoryEnum.java**

```java
// Add after DIGITAL_HUMAN line, before the closing brace
MARKETING("marketing", "营销策划");
```

Full file after edit (showing only the changed section, lines 14-16):

```java
    VIDEO_KEYFRAME_GRID("video_keyframe_grid", "视频-关键帧网格"),
    DIGITAL_HUMAN("digital_human", "数字人"),
    MARKETING("marketing", "营销策划");
```

- [ ] **Step 2: Add default marketing prompt to PromptServiceImpl.java**

Replace the `buildDefaultPrompt` method (lines 103-123) by adding a `"marketing"` branch AFTER the `"copy"` block and BEFORE the default fallback:

```java
    if ("marketing".equals(category)) {
        return "你是一位专业的跨境电商营销专家和内容策略师。\n\n" +
            "# 任务\n" +
            "分析提供的产品信息（图片描述、文字描述、中文广告推广词）、目标平台、目标市场，输出一个 JSON 对象。\n\n" +
            "# 输出 JSON 结构（键名固定，值中除示例广告词外均为分析结论）\n" +
            "{\n" +
            "  \"risk_level\": \"safe 或 warning\",\n" +
            "  \"culture_notes\": \"文化背景分析与本土化建议\",\n" +
            "  \"core_strategy\": \"核心营销策略描述\",\n" +
            "  \"example_ad_copy\": \"示例广告词（英文或目标市场语言），用户可直接使用或作为修改参考\"\n" +
            "}\n\n" +
            "# 分析要求\n" +
            "1. **合规性检查** → 输出 risk_level\n" +
            "   - 检查夸大功效、品牌侵权、违禁品类等。\n" +
            "   - safe = 无明显风险；warning = 存在需修改的内容。\n" +
            "2. **文化本土化** → 输出 culture_notes\n" +
            "   - 考虑幽默、禁忌、信任信号、计量单位、日期格式、季节性等。\n" +
            "3. **核心策略** → 输出 core_strategy\n" +
            "   - 选择最有效的单一营销心理触发点（痛点反差、价格锚点、从众心理、稀缺性、权威背书等）。\n" +
            "4. **示例广告词** → 输出 example_ad_copy\n" +
            "   - 基于原始情报（不可遗漏任何卖点）生成一段可直接展示的文案。\n" +
            "   - 必须符合平台政策和目标市场语言习惯，不是直接翻译中文。\n" +
            "   - 长度适中，适合产品页面展示。\n\n" +
            "# 输入信息\n" +
            "产品名称：" + productName + "\n" +
            "产品描述：" + variables.getOrDefault("product_description", "") + "\n" +
            "中文广告推广词：" + variables.getOrDefault("chinese_ad_copy", "") + "\n" +
            "目标市场：" + variables.getOrDefault("target_market", "USA") + "\n\n" +
            "# 重要约束\n" +
            "- 只输出 JSON，不要有任何额外文字。\n" +
            "- 所有值使用英文双引号，不要添加注释。\n" +
            "- 如果缺少某些信息，基于常识合理推断并注明（在 culture_notes 中说明）。";
    }
```

- [ ] **Step 3: Compile common + ai modules**

Run: `cd adcreater-server && mvn compile -pl djb-adcreater-common,djb-adcreater-module-ai -am -q`

- [ ] **Step 4: Commit**

```bash
git add adcreater-server/djb-adcreater-common/src/main/java/com/djb/common/enums/PromptCategoryEnum.java adcreater-server/djb-adcreater-module-ai/src/main/java/com/djb/module/ai/service/PromptServiceImpl.java
git commit -m "feat(ai): add MARKETING prompt category and default marketing expert prompt"
```

---

### Task 3: Create request/response VOs for marketing analyze endpoint

**Files:**
- Create: `adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/MarketingAnalyzeReqVO.java`
- Create: `adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/MarketingAnalyzeRespVO.java`

- [ ] **Step 1: Create MarketingAnalyzeReqVO.java**

```java
package com.djb.module.ad.controller.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class MarketingAnalyzeReqVO {

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotBlank(message = "产品描述不能为空")
    private String productDescription;

    @NotBlank(message = "中文广告词不能为空")
    private String chineseAdCopy;

    @NotBlank(message = "目标市场不能为空")
    private String targetMarket;
}
```

- [ ] **Step 2: Create MarketingAnalyzeRespVO.java**

```java
package com.djb.module.ad.controller.vo;

import lombok.Data;

@Data
public class MarketingAnalyzeRespVO {
    private String riskLevel;
    private String cultureNotes;
    private String coreStrategy;
    private String exampleAdCopy;
}
```

- [ ] **Step 3: Commit**

```bash
git add adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/MarketingAnalyzeReqVO.java adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/MarketingAnalyzeRespVO.java
git commit -m "feat(ad): add MarketingAnalyzeReqVO and MarketingAnalyzeRespVO"
```

---

### Task 4: Create MarketingController, delete old AdCopyController and VOs

**Files:**
- Create: `adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/MarketingController.java`
- Delete: `adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/AdCopyController.java`
- Delete: `adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/TranslateReqVO.java`
- Delete: `adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/TranslateRespVO.java`

- [ ] **Step 1: Create MarketingController.java**

```java
package com.djb.module.ad.controller;

import com.djb.module.ad.controller.vo.*;
import com.djb.module.ai.adapter.AiResult;
import com.djb.module.ai.service.ModelOrchestrationService;
import com.djb.module.ai.service.PromptService;
import com.djb.module.billing.service.BillingService;
import com.djb.framework.common.pojo.CommonResult;
import com.djb.framework.web.core.util.WebFrameworkUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.djb.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - 广告营销策划
 *
 * @author adcreater
 */
@Tag(name = "用户端 - 广告营销策划")
@RestController
@RequestMapping("/api/ad/marketing")
@Validated
@Slf4j
public class MarketingController {

    @Resource
    private ModelOrchestrationService orchestrationService;

    @Resource
    private PromptService promptService;

    @Resource
    private BillingService billingService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
        "\\{[^{}]*\"risk_level\"[^{}]*\"culture_notes\"[^{}]*\"core_strategy\"[^{}]*\"example_ad_copy\"[^{}]*\\}",
        Pattern.DOTALL);

    @Operation(summary = "分析营销策略")
    @PostMapping("/analyze")
    public CommonResult<MarketingAnalyzeRespVO> analyze(@Valid @RequestBody MarketingAnalyzeReqVO reqVO) {
        Long userId = WebFrameworkUtils.getLoginUserId();

        Map<String, String> vars = Map.of(
            "product_name", reqVO.getProductName(),
            "product_description", reqVO.getProductDescription(),
            "chinese_ad_copy", reqVO.getChineseAdCopy(),
            "target_market", reqVO.getTargetMarket()
        );

        String prompt = promptService.resolvePrompt("marketing", vars);

        int cost = 5;
        Long txId = billingService.preConsume(userId, cost, null);

        try {
            AiResult result = orchestrationService.translate(
                prompt, "auto", "en", reqVO.getTargetMarket());

            if (!result.isSuccess()) {
                throw new RuntimeException("AI 分析失败: " + result.getErrorMessage());
            }

            String aiContent = result.getRevisedPrompt();
            MarketingAnalyzeRespVO resp = parseMarketingResponse(aiContent);

            billingService.confirmConsume(txId);
            return success(resp);
        } catch (Exception e) {
            billingService.rollbackConsume(txId);
            log.error("Marketing analyze failed for user {}", userId, e);
            throw new RuntimeException("营销分析失败: " + e.getMessage(), e);
        }
    }

    private MarketingAnalyzeRespVO parseMarketingResponse(String aiContent) {
        MarketingAnalyzeRespVO resp = new MarketingAnalyzeRespVO();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> json = OBJECT_MAPPER.readValue(aiContent, Map.class);
            resp.setRiskLevel(String.valueOf(json.getOrDefault("risk_level", "warning")));
            resp.setCultureNotes(String.valueOf(json.getOrDefault("culture_notes", "")));
            resp.setCoreStrategy(String.valueOf(json.getOrDefault("core_strategy", "")));
            resp.setExampleAdCopy(String.valueOf(json.getOrDefault("example_ad_copy", "")));
        } catch (Exception e) {
            // Fallback: regex extract JSON block
            Matcher matcher = JSON_BLOCK_PATTERN.matcher(aiContent);
            if (matcher.find()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> json = OBJECT_MAPPER.readValue(matcher.group(), Map.class);
                    resp.setRiskLevel(String.valueOf(json.getOrDefault("risk_level", "warning")));
                    resp.setCultureNotes(String.valueOf(json.getOrDefault("culture_notes", "")));
                    resp.setCoreStrategy(String.valueOf(json.getOrDefault("core_strategy", "")));
                    resp.setExampleAdCopy(String.valueOf(json.getOrDefault("example_ad_copy", "")));
                    return resp;
                } catch (Exception ignored) {}
            }
            // Final fallback: return raw text as cultureNotes
            resp.setRiskLevel("warning");
            resp.setCultureNotes("AI 返回格式异常，原始响应：\n" + aiContent);
            resp.setCoreStrategy("请重新生成分析");
            resp.setExampleAdCopy("");
        }
        return resp;
    }
}
```

- [ ] **Step 2: Delete old files**

```bash
rm adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/AdCopyController.java
rm adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/TranslateReqVO.java
rm adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/TranslateRespVO.java
```

- [ ] **Step 3: Compile ad module**

Run: `cd adcreater-server && mvn compile -pl djb-adcreater-module-ad -am -q`

- [ ] **Step 4: Commit**

```bash
git add adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/MarketingController.java
git add adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/AdCopyController.java
git add adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/TranslateReqVO.java
git add adcreater-server/djb-adcreater-module-ad/src/main/java/com/djb/module/ad/controller/vo/TranslateRespVO.java
git commit -m "feat(ad): replace AdCopyController with MarketingController for AI marketing strategy"
```

---

### Task 5: Create MarketingPanel.vue (Step 1 component)

**Files:**
- Create: `desktop/src/pages/components/MarketingPanel.vue`

- [ ] **Step 1: Create MarketingPanel.vue**

```vue
<template>
  <div class="marketing-panel">
    <h3>Step 1：广告营销策划</h3>

    <el-form :model="form" label-width="110px">
      <el-form-item label="产品名称" required>
        <el-input v-model="form.productName" placeholder="请输入产品名称" />
      </el-form-item>

      <el-form-item label="产品描述" required>
        <el-input
          v-model="form.productDescription"
          type="textarea"
          :rows="3"
          placeholder="输入产品图片描述、文字描述、规格参数等..."
        />
      </el-form-item>

      <el-form-item label="中文广告词" required>
        <el-input
          v-model="form.chineseAdCopy"
          type="textarea"
          :rows="3"
          placeholder="输入中文广告推广词，如：品质之选，限时优惠..."
        />
      </el-form-item>

      <el-form-item label="目标市场" required>
        <el-select v-model="form.targetMarket" placeholder="选择目标市场" style="width: 100%">
          <el-option label="美国 (US)" value="US" />
          <el-option label="英国 (UK)" value="UK" />
          <el-option label="德国 (DE)" value="DE" />
          <el-option label="日本 (JP)" value="JP" />
          <el-option label="沙特 (SA)" value="SA" />
          <el-option label="巴西 (BR)" value="BR" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          :loading="analyzing"
          :disabled="!canAnalyze"
          @click="handleAnalyze"
        >
          {{ analyzing ? '分析中...' : '生成营销策划' }}
        </el-button>
      </el-form-item>
    </el-form>

    <!-- Analysis Result -->
    <div v-if="analysis" class="result-section">
      <h4>分析结果</h4>

      <div class="analysis-cards">
        <!-- 1. Compliance -->
        <div class="analysis-card compliance-card">
          <div class="card-header">
            <span class="card-title">合规评估</span>
            <el-tag
              :type="analysis.riskLevel === 'safe' ? 'success' : 'warning'"
              size="default"
              effect="dark"
            >
              {{ analysis.riskLevel === 'safe' ? 'SAFE 合规' : 'WARNING 需关注' }}
            </el-tag>
          </div>
        </div>

        <!-- 2. Culture Notes -->
        <div class="analysis-card">
          <div class="card-header">
            <span class="card-title">文化本土化建议</span>
          </div>
          <el-alert
            :title="analysis.cultureNotes"
            type="info"
            :closable="false"
            show-icon
          />
        </div>

        <!-- 3. Core Strategy -->
        <div class="analysis-card strategy-card">
          <div class="card-header">
            <span class="card-title">核心营销策略</span>
          </div>
          <p class="strategy-text">{{ analysis.coreStrategy }}</p>
        </div>

        <!-- 4. Example Ad Copy -->
        <div class="analysis-card copy-card">
          <div class="card-header">
            <span class="card-title">示例广告词</span>
            <el-button
              size="small"
              type="primary"
              plain
              @click="copyAdCopy"
            >
              <el-icon><DocumentCopy /></el-icon> 复制
            </el-button>
          </div>
          <div class="copy-content">
            <p>{{ analysis.exampleAdCopy }}</p>
          </div>
        </div>
      </div>

      <div class="step-actions">
        <el-button type="primary" @click="handleNext">下一步：生成图片</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import client from '@/api/client'

const emit = defineEmits<{
  (e: 'next', data: any): void
}>()

const form = reactive({
  productName: '',
  productDescription: '',
  chineseAdCopy: '',
  targetMarket: 'US'
})

const analyzing = ref(false)
const analysis = ref<any>(null)

const canAnalyze = computed(() =>
  form.productName.trim() &&
  form.productDescription.trim() &&
  form.chineseAdCopy.trim() &&
  form.targetMarket.trim()
)

async function handleAnalyze() {
  if (!canAnalyze.value) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  analyzing.value = true
  try {
    const res = await client.post('/ad/marketing/analyze', {
      productName: form.productName,
      productDescription: form.productDescription,
      chineseAdCopy: form.chineseAdCopy,
      targetMarket: form.targetMarket
    }) as any
    analysis.value = res?.data ?? res
    ElMessage.success('营销分析完成')
  } catch (err: any) {
    ElMessage.error('分析失败: ' + (err?.message || '未知错误'))
  } finally {
    analyzing.value = false
  }
}

async function copyAdCopy() {
  if (!analysis.value?.exampleAdCopy) return
  try {
    await navigator.clipboard.writeText(analysis.value.exampleAdCopy)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

function handleNext() {
  emit('next', {
    productName: form.productName,
    productDescription: form.productDescription,
    chineseAdCopy: form.chineseAdCopy,
    targetMarket: form.targetMarket,
    analysis: analysis.value
  })
}
</script>

<style scoped>
.marketing-panel h3 {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-6) 0;
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.result-section {
  margin-top: var(--space-8);
}

.result-section h4 {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-4) 0;
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text);
}

.analysis-cards {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.analysis-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-bg-card);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.card-title {
  font-weight: 600;
  font-size: var(--text-base);
  color: var(--color-text);
}

.compliance-card {
  border-left: 4px solid var(--color-primary);
}

.strategy-card {
  border-left: 4px solid var(--color-warning);
  background: var(--color-bg);
}

.strategy-text {
  margin: 0;
  line-height: var(--leading-relaxed);
  color: var(--color-text);
  font-weight: 500;
}

.copy-content {
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  padding: var(--space-3);
}

.copy-content p {
  margin: 0;
  line-height: var(--leading-relaxed);
  color: var(--color-text);
  white-space: pre-wrap;
}

.step-actions {
  margin-top: var(--space-5);
  text-align: right;
}
</style>
```

- [ ] **Step 2: Verify the component has no syntax errors by checking with vite build (TypeScript)**

Run: `cd desktop && npx vue-tsc --noEmit src/pages/components/MarketingPanel.vue 2>&1 || echo "Checking via build..."`

- [ ] **Step 3: Commit**

```bash
git add desktop/src/pages/components/MarketingPanel.vue
git commit -m "feat(desktop): add MarketingPanel component for AI marketing strategy analysis"
```

---

### Task 6: Update AdCreatePage.vue to use MarketingPanel

**Files:**
- Modify: `desktop/src/pages/AdCreatePage.vue`

- [ ] **Step 1: Update AdCreatePage.vue — import, step definitions, template, and reset**

Change the import (line 63):
```typescript
import MarketingPanel from './components/MarketingPanel.vue'
```

Change step definitions (line 73-78):
```typescript
const steps = [
  { title: '策划', desc: '营销分析 & 文案生成' },
  { title: '图片', desc: '多尺寸生成' },
  { title: '视频', desc: '动态素材' },
  { title: '完成', desc: '预览导出' }
]
```

Change the data ref (line 69):
```typescript
const marketingData = ref<any>(null)
```

Change the template — Step 0 section (lines 25-28):
```vue
      <!-- Step 1: 营销策划 -->
      <div v-if="activeStep === 0" class="step-panel">
        <MarketingPanel @next="(data) => { marketingData = data; activeStep = 1 }" />
      </div>
```

Change the template — Step 2 section (lines 31-37), update the prop name:
```vue
      <!-- Step 2: 图片生成 -->
      <div v-if="activeStep === 1" class="step-panel">
        <AdImagePanel
          :ad-copy="marketingData"
          @next="(data) => { adImage = data; activeStep = 2 }"
          @prev="activeStep = 0"
        />
      </div>
```

Change the template — Step 4 section (lines 48-56), update the prop name:
```vue
      <!-- Step 4: 预览 -->
      <div v-if="activeStep === 3" class="step-panel">
        <AdPreviewPanel
          :ad-copy="marketingData"
          :ad-image="adImage"
          :ad-video="adVideo"
          @prev="activeStep = 2"
          @reset="handleReset"
        />
      </div>
```

Change handleReset (line 80-85):
```typescript
function handleReset() {
  activeStep.value = 0
  marketingData.value = null
  adImage.value = null
  adVideo.value = null
}
```

- [ ] **Step 2: Commit**

```bash
git add desktop/src/pages/AdCreatePage.vue
git commit -m "feat(desktop): update AdCreatePage to use MarketingPanel as Step 1"
```

---

### Task 7: Update AdPreviewPanel.vue for marketing analysis display

**Files:**
- Modify: `desktop/src/pages/components/AdPreviewPanel.vue`

- [ ] **Step 1: Update the "商品描述" tab to "营销分析" with 4-card layout**

Replace the entire `<el-tab-pane label="商品描述" name="copy">` block (lines 146-172) with:

```vue
        <!-- Tab 1: 营销分析 -->
        <el-tab-pane label="营销分析" name="copy">
          <div class="tab-content" v-if="adCopy?.analysis">
            <div class="description-block">
              <h4 class="block-title">产品信息</h4>
              <div class="desc-meta">
                <span><strong>产品名称：</strong>{{ adCopy.productName }}</span>
                <span><strong>目标市场：</strong>{{ marketNames[adCopy.targetMarket] || adCopy.targetMarket }}</span>
                <span><strong>中文广告词：</strong>{{ adCopy.chineseAdCopy || '未设置' }}</span>
              </div>
            </div>

            <div class="analysis-cards">
              <!-- Risk Level -->
              <div class="analysis-card compliance-card">
                <div class="card-header">
                  <span class="card-label">合规评估</span>
                  <el-tag
                    :type="adCopy.analysis.riskLevel === 'safe' ? 'success' : 'warning'"
                    size="default"
                    effect="dark"
                  >
                    {{ adCopy.analysis.riskLevel === 'safe' ? 'SAFE 合规' : 'WARNING 需关注' }}
                  </el-tag>
                </div>
              </div>

              <!-- Culture Notes -->
              <div class="analysis-card">
                <div class="card-header">
                  <span class="card-label">文化本土化建议</span>
                </div>
                <el-alert
                  :title="adCopy.analysis.cultureNotes"
                  type="info"
                  :closable="false"
                  show-icon
                />
              </div>

              <!-- Core Strategy -->
              <div class="analysis-card strategy-card">
                <div class="card-header">
                  <span class="card-label">核心营销策略</span>
                </div>
                <p class="strategy-text">{{ adCopy.analysis.coreStrategy }}</p>
              </div>

              <!-- Example Ad Copy -->
              <div class="analysis-card copy-card">
                <div class="card-header">
                  <span class="card-label">示例广告词</span>
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    @click="copyAdCopy"
                  >
                    <el-icon><DocumentCopy /></el-icon> 复制
                  </el-button>
                </div>
                <div class="copy-content">
                  <p>{{ adCopy.analysis.exampleAdCopy }}</p>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="尚未进行营销分析" :image-size="60" />
        </el-tab-pane>
```

- [ ] **Step 2: Add the copyAdCopy method to the script section**

Add this import (line 258):
```typescript
import { DocumentCopy } from '@element-plus/icons-vue'
```

Add this method after the `handleNew` function (before `</script>`):
```typescript
async function copyAdCopy() {
  if (!props.adCopy?.analysis?.exampleAdCopy) return
  try {
    await navigator.clipboard.writeText(props.adCopy.analysis.exampleAdCopy)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
```

- [ ] **Step 3: Add the analysis card styles to the scoped CSS**

Add at the end of `<style scoped>` (before the closing `</style>`):
```css
/* Analysis cards in preview */
.analysis-cards {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.analysis-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  background: var(--color-bg-card);
}

.analysis-card .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.analysis-card .card-label {
  font-weight: 600;
  font-size: var(--text-sm);
  color: var(--color-text);
}

.analysis-card.compliance-card {
  border-left: 4px solid var(--color-primary);
}

.analysis-card.strategy-card {
  border-left: 4px solid var(--color-warning);
  background: var(--color-bg);
}

.analysis-card .strategy-text {
  margin: 0;
  line-height: var(--leading-relaxed);
  color: var(--color-text);
  font-weight: 500;
  font-size: var(--text-sm);
}

.analysis-card .copy-content {
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  padding: var(--space-2);
}

.analysis-card .copy-content p {
  margin: 0;
  line-height: var(--leading-relaxed);
  color: var(--color-text);
  white-space: pre-wrap;
  font-size: var(--text-sm);
}
```

Also update `estimatedPoints` to use marketingData structure (line 298):
```typescript
if (props.adCopy?.analysis) pts += 5
```

- [ ] **Step 4: Commit**

```bash
git add desktop/src/pages/components/AdPreviewPanel.vue
git commit -m "feat(desktop): update AdPreviewPanel to show marketing analysis cards"
```

---

### Task 8: Update mock server and delete old AdCopyPanel.vue

**Files:**
- Modify: `desktop/mock/index.ts`
- Delete: `desktop/src/pages/components/AdCopyPanel.vue`

- [ ] **Step 1: Replace `/api/ad/copy/translate` mock with `/api/ad/marketing/analyze`**

Replace the translate mock handler (lines 141-176) with:

```typescript
  // ── Ad Marketing ──────────────────────────────────────────────────
  if (path === '/api/ad/marketing/analyze' && method === 'POST') {
    const body = await parseBody(req)
    const market = body.targetMarket as string || 'US'
    const productName = body.productName as string || 'Product'

    const marketAnalysis: Record<string, {
      riskLevel: string;
      cultureNotes: string;
      coreStrategy: string;
      exampleAdCopy: string;
    }> = {
      US: {
        riskLevel: 'safe',
        cultureNotes: '美国消费者注重效率和产品品质。建议突出产品的实用价值和性价比，使用英制单位（inch/lb），避免过于夸张的营销语言。Amazon US 平台要求图片白底、无文字覆盖。',
        coreStrategy: '痛点反差策略：先呈现目标用户日常遇到的问题场景，再展示产品如何高效解决，配合限时折扣制造紧迫感。',
        exampleAdCopy: `Introducing ${productName} — the smarter way to solve your daily challenges. Designed with premium materials and engineered for reliability. Whether you're at home or on the go, experience unmatched performance that fits seamlessly into your lifestyle. Limited stock available — order today.`
      },
      UK: {
        riskLevel: 'safe',
        cultureNotes: '英国消费者偏好低调、幽默的营销风格。避免过于激进的促销语言（如\"buy now\"），改用更礼貌的邀请式表达。注意英式拼写差异（colour, centre），并符合ASA广告标准。',
        coreStrategy: '权威背书策略：通过产品认证、评测数据和用户评价建立信任，结合英式幽默传达产品价值。',
        exampleAdCopy: `${productName} — thoughtfully crafted for those who appreciate quality. Trusted by thousands across the UK, our product combines innovation with everyday practicality. Free delivery on all orders.`
      },
      DE: {
        riskLevel: 'safe',
        cultureNotes: '德国市场重视技术规格、环保认证（Blue Angel, CE）和数据透明度。避免模糊的营销语言，需提供具体参数。退货政策必须清晰（德国消费者退货率较高）。',
        coreStrategy: '技术权威策略：详细展示产品技术参数、认证和质量检测结果，用数据说服理性消费者。',
        exampleAdCopy: `${productName} — zertifizierte Qualität und Präzision. Jedes Produkt durchläuft strenge Qualitätskontrollen und erfüllt alle EU-Normen. Nachhaltig produziert, entwickelt für den Langzeiteinsatz.`
      },
      JP: {
        riskLevel: 'warning',
        cultureNotes: '日本市场对产品外观要求极高（\"美品\"文化）。包装必须完美无瑕，任何瑕疵都可能导致退货。建议使用敬语风格，强调\"安心\"和\"信頼\"。避免直接比较竞争对手。',
        coreStrategy: '安心信赖策略：强调产品质量保证、售后服务和用户评价，配合\"限定\"概念制造稀缺感。',
        exampleAdCopy: `${productName} — 信頼の品質、安心の選択。厳選された素材と日本の職人技術が融合した逸品です。万が一の不良品には交換保証付き。数量限定販売。`
      },
      SA: {
        riskLevel: 'warning',
        cultureNotes: '沙特市场严格遵守伊斯兰文化规范。女性模特需佩戴头巾，避免任何酒精、猪肉相关图像或文字。斋月期间消费模式变化显著。使用阿拉伯语更佳，数字用阿拉伯文数字。',
        coreStrategy: '文化认同策略：强调产品符合伊斯兰价值观，突出家庭和社交场景，配合节日促销节点。',
        exampleAdCopy: `${productName} — quality you can trust for your family. Designed to meet the highest standards of craftsmanship and reliability. Special Ramadan offers available.`
      },
      BR: {
        riskLevel: 'safe',
        cultureNotes: '巴西消费者热衷社交媒体分享，偏好鲜艳色彩和情感化营销。分期付款（\"parcelamento\"）是常见支付方式。Mercado Livre 是主要电商平台，需注意葡萄牙语本地化。',
        coreStrategy: '社交从众策略：利用社交媒体口碑和KOL推荐，结合分期付款降低购买门槛，制造\"大家都在用\"的从众效应。',
        exampleAdCopy: `${productName} — a escolha de quem busca qualidade sem complicação. Junte-se a milhares de clientes satisfeitos. Parcele em até 12x no cartão. Entrega rápida para todo o Brasil.`
      }
    }

    const data = marketAnalysis[market] || marketAnalysis['US']
    await new Promise(r => setTimeout(r, 400 + Math.random() * 600))
    sendJson(res, 200, ok({
      riskLevel: data.riskLevel,
      cultureNotes: data.cultureNotes,
      coreStrategy: data.coreStrategy,
      exampleAdCopy: data.exampleAdCopy
    }))
    return true
  }
```

- [ ] **Step 2: Delete AdCopyPanel.vue**

```bash
rm desktop/src/pages/components/AdCopyPanel.vue
```

- [ ] **Step 3: Commit**

```bash
git add desktop/mock/index.ts desktop/src/pages/components/AdCopyPanel.vue
git commit -m "feat(desktop): add marketing analyze mock and remove old AdCopyPanel"
```

---

### Task 9: Integration verification

- [ ] **Step 1: Verify backend compiles end-to-end**

Run: `cd adcreater-server && mvn compile -q`

- [ ] **Step 2: Verify frontend dev server starts successfully**

Run: `cd desktop && npx vite build --mode development 2>&1 | tail -20`
Expected: Build completes without errors.

- [ ] **Step 3: Verify mock API returns correct data structure**

Run: `cd desktop && npx vite --host 0.0.0.0 --port 5173 &`
Expected: Open browser at `http://localhost:5173`, login, navigate to "广告制作", fill form, click "生成营销策划", verify 4 cards appear.

- [ ] **Step 4: Verify data flow to subsequent steps**

Pre-condition: Complete Step 1 analysis.
Action: Click "下一步：生成图片".
Expected: Step 2 loads with the marketing data available. Click through to preview and verify "营销分析" tab shows the analysis cards.
