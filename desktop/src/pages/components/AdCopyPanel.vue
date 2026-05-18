<template>
  <div class="ad-copy-panel">
    <h3>Step 1：广告文案翻译</h3>

    <el-form :model="form" label-width="100px">
      <el-form-item label="产品名称" required>
        <el-input v-model="form.productName" placeholder="请输入产品名称" />
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

      <el-form-item label="卖点标签" required>
        <div class="tags-section">
          <el-tag
            v-for="tag in form.sellingPoints"
            :key="tag"
            closable
            :disable-transitions="false"
            @close="removeTag(tag)"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            size="small"
            class="tag-input"
            @keyup.enter="addTag"
            @blur="addTag"
          />
          <el-button v-else size="small" @click="showTagInput">+ 添加卖点</el-button>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          :loading="translating"
          :disabled="!canTranslate"
          @click="handleTranslate"
        >
          {{ translating ? '翻译中...' : '翻译文案' }}
        </el-button>
      </el-form-item>
    </el-form>

    <!-- Translated Copy Result -->
    <div v-if="translatedCopy" class="result-section">
      <h4>翻译结果</h4>
      <el-card shadow="never">
        <div class="copy-card">
          <div class="copy-original">
            <el-tag type="info" size="small">原文</el-tag>
            <p>{{ translatedCopy.original }}</p>
          </div>
          <div class="copy-translated">
            <el-tag type="success" size="small">{{ marketLabel }}</el-tag>
            <p>{{ translatedCopy.translated }}</p>
          </div>
        </div>
      </el-card>

      <div class="step-actions">
        <el-button type="primary" @click="handleNext">下一步：生成图片</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const emit = defineEmits<{
  (e: 'next', data: any): void
}>()

const form = reactive({
  productName: '',
  targetMarket: 'US',
  sellingPoints: [] as string[]
})

const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref<any>(null)
const translating = ref(false)
const translatedCopy = ref<any>(null)

const marketLabels: Record<string, string> = {
  US: '英语', UK: '英语', DE: '德语', JP: '日语', SA: '阿拉伯语', BR: '葡萄牙语'
}
const marketLangCodes: Record<string, string> = {
  US: 'en-US', UK: 'en-GB', DE: 'de-DE', JP: 'ja-JP', SA: 'ar-SA', BR: 'pt-BR'
}
const marketLabel = computed(() => marketLabels[form.targetMarket] || form.targetMarket)

const canTranslate = computed(() =>
  form.productName.trim() && form.sellingPoints.length > 0
)

function showTagInput() {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus?.()
  })
}

function addTag() {
  const val = tagInputValue.value.trim()
  if (val && !form.sellingPoints.includes(val)) {
    form.sellingPoints.push(val)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

function removeTag(tag: string) {
  form.sellingPoints = form.sellingPoints.filter((t) => t !== tag)
}

async function handleTranslate() {
  if (!canTranslate.value) {
    ElMessage.warning('请填写产品名称和至少一个卖点')
    return
  }
  translating.value = true
  try {
    const res = await client.post('/app-api/ad/copy/translate', {
      productTitle: form.productName,
      productDescription: '',
      targetMarket: form.targetMarket,
      sellingPoints: form.sellingPoints,
      sourceLang: 'zh-CN',
      targetLang: marketLangCodes[form.targetMarket] || 'en-US'
    }) as any
    const data = res?.data ?? res
    translatedCopy.value = {
      original: form.productName,
      translated: data?.localizedCopy || data?.translatedTitle || form.productName
    }
    ElMessage.success('翻译完成')
  } catch (err: any) {
    ElMessage.error('翻译失败: ' + (err?.message || '未知错误'))
  } finally {
    translating.value = false
  }
}

function handleNext() {
  emit('next', {
    productName: form.productName,
    targetMarket: form.targetMarket,
    sellingPoints: form.sellingPoints,
    translatedCopy: translatedCopy.value
  })
}
</script>

<style scoped>
.ad-copy-panel h3 {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-6) 0;
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
  align-items: center;
}

.tag-item {
  margin: 0;
  border-radius: var(--radius-sm);
}

.tag-input {
  width: 100px;
}

.result-section {
  margin-top: var(--space-8);
}

.result-section h4 {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-3) 0;
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text);
}

.copy-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.copy-original p,
.copy-translated p {
  margin: var(--space-1) 0 0 0;
  line-height: var(--leading-relaxed);
  color: var(--color-text);
}

.step-actions {
  margin-top: var(--space-5);
  text-align: right;
}
</style>
