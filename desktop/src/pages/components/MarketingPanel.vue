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

        <!-- 2. Culture Notes (editable) -->
        <div class="analysis-card">
          <div class="card-header">
            <span class="card-title">文化本土化建议</span>
            <el-tag size="small" type="info" effect="plain">可编辑</el-tag>
          </div>
          <el-input
            v-model="analysis.cultureNotes"
            type="textarea"
            :rows="3"
            placeholder="文化本土化建议..."
          />
        </div>

        <!-- 3. Core Strategy (editable) -->
        <div class="analysis-card strategy-card">
          <div class="card-header">
            <span class="card-title">核心营销策略</span>
            <el-tag size="small" type="warning" effect="plain">可编辑</el-tag>
          </div>
          <el-input
            v-model="analysis.coreStrategy"
            type="textarea"
            :rows="3"
            placeholder="核心营销策略..."
          />
        </div>

        <!-- 4. Example Ad Copy -->
        <div class="analysis-card copy-card">
          <div class="card-header">
            <span class="card-title">示例文案</span>
            <div class="card-header-actions">
              <el-button
                size="small"
                type="primary"
                plain
                @click="copyAdCopy"
              >
                <el-icon><DocumentCopy /></el-icon> 复制
              </el-button>
            </div>
          </div>
          <div class="copy-content">
            <p>{{ analysis.exampleAdCopy }}</p>
          </div>
        </div>
      </div>

      <!-- Regenerate button -->
      <div class="regenerate-row">
        <el-button
          type="warning"
          :loading="regenerating"
          @click="handleRegenerate"
        >
          {{ regenerating ? '重新生成中...' : '基于修改内容重新生成示例文案' }}
        </el-button>
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
  targetMarket: 'US'
})

const analyzing = ref(false)
const regenerating = ref(false)
const analysis = ref<any>(null)

const canAnalyze = computed(() =>
  form.productName.trim() &&
  form.productDescription.trim() &&
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
      chineseAdCopy: '',
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

async function handleRegenerate() {
  if (!analysis.value) return
  regenerating.value = true
  try {
    const res = await client.post('/ad/marketing/regenerate-copy', {
      productName: form.productName,
      productDescription: form.productDescription,
      chineseAdCopy: '',
      targetMarket: form.targetMarket,
      riskLevel: analysis.value.riskLevel,
      cultureNotes: analysis.value.cultureNotes,
      coreStrategy: analysis.value.coreStrategy
    }) as any
    const data = res?.data ?? res
    analysis.value.exampleAdCopy = data.exampleAdCopy
    ElMessage.success('示例文案已重新生成')
  } catch (err: any) {
    ElMessage.error('重新生成失败: ' + (err?.message || '未知错误'))
  } finally {
    regenerating.value = false
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

.card-header-actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
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

.regenerate-row {
  margin-top: var(--space-4);
  text-align: center;
}

.step-actions {
  margin-top: var(--space-5);
  text-align: right;
}
</style>
