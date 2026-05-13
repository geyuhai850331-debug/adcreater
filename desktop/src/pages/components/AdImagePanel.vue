<template>
  <div class="ad-image-panel">
    <h3>Step 2：广告图片生成</h3>

    <!-- Template Selector -->
    <div class="section">
      <h4>选择模板</h4>
      <el-radio-group v-model="selectedTemplate" size="default">
        <el-radio-button
          v-for="tpl in templates"
          :key="tpl.id"
          :value="tpl.id"
        >
          {{ tpl.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- Style Selector -->
    <div class="section">
      <h4>选择风格</h4>
      <div class="style-grid">
        <div
          v-for="style in styles"
          :key="style.key"
          class="style-item"
          :class="{ active: selectedStyle === style.key }"
          @click="selectedStyle = style.key"
        >
          <div class="style-preview" :style="{ backgroundColor: style.color }">
            <el-icon :size="28" color="#fff"><PictureFilled /></el-icon>
          </div>
          <span class="style-name">{{ style.label }}</span>
        </div>
      </div>
    </div>

    <!-- Dimension Presets -->
    <div class="section">
      <h4>尺寸预设</h4>
      <div class="dimension-grid">
        <div
          v-for="dim in dimensions"
          :key="dim.key"
          class="dimension-item"
          :class="{ active: selectedDimension === dim.key }"
          @click="selectedDimension = dim.key"
        >
          <span class="dim-label">{{ dim.label }}</span>
          <span class="dim-size">{{ dim.width }} x {{ dim.height }}</span>
        </div>
      </div>
    </div>

    <!-- Generate -->
    <div class="section">
      <el-button
        type="primary"
        size="large"
        :loading="generating"
        @click="handleGenerate"
      >
        {{ generating ? '生成中...' : '生成图片' }}
      </el-button>
    </div>

    <!-- Progress Bar (SSE simulation) -->
    <div v-if="generating" class="progress-section">
      <el-progress :percentage="progress" :status="progressStatus" />
      <p class="progress-text">{{ progressText }}</p>
    </div>

    <!-- Result -->
    <div v-if="generatedImage" class="result-section">
      <h4>生成结果</h4>
      <div class="image-result">
        <el-image
          :src="generatedImage.url"
          fit="contain"
          class="result-image"
          :preview-src-list="[generatedImage.url]"
        />
        <div class="result-actions">
          <el-button type="primary" @click="downloadImage" :loading="saving">
            <el-icon><Download /></el-icon> 保存到本地
          </el-button>
        </div>
      </div>

      <div class="step-actions">
        <el-button @click="emit('prev')">上一步</el-button>
        <el-button type="primary" @click="handleNext">下一步：生成视频</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PictureFilled, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const props = defineProps<{
  adCopy: any
}>()

const emit = defineEmits<{
  (e: 'next', data: any): void
  (e: 'prev'): void
}>()

const selectedTemplate = ref('')
const selectedStyle = ref('modern')
const selectedDimension = ref('amazon')

const generating = ref(false)
const progress = ref(0)
const progressStatus = ref<'success' | 'exception' | 'warning' | ''>('')
const progressText = ref('')
const generatedImage = ref<any>(null)
const saving = ref(false)

const templates = ref<Array<{ id: string; name: string }>>([])

const styles = [
  { key: 'modern', label: '现代简约', color: '#409EFF' },
  { key: 'warm', label: '温暖色调', color: '#E6A23C' },
  { key: 'tech', label: '科技蓝调', color: '#67C23A' },
  { key: 'luxury', label: '奢华黑金', color: '#303133' },
  { key: 'nature', label: '自然清新', color: '#52C41A' },
  { key: 'vintage', label: '复古风格', color: '#F56C6C' }
]

const dimensions = [
  { key: 'amazon', label: 'Amazon', width: 1500, height: 1500 },
  { key: 'facebook', label: 'Facebook', width: 1200, height: 628 },
  { key: 'instagram', label: 'Instagram', width: 1080, height: 1080 },
  { key: 'google', label: 'Google Display', width: 1200, height: 628 },
  { key: 'tiktok', label: 'TikTok', width: 1080, height: 1920 },
  { key: 'shopee', label: 'Shopee', width: 1024, height: 1024 }
]

async function fetchTemplates() {
  try {
    const res = await client.get('/templates/list') as any
    const list = res?.data ?? res ?? []
    templates.value = Array.isArray(list) ? list : []
    if (templates.value.length > 0 && !selectedTemplate.value) {
      selectedTemplate.value = templates.value[0].id
    }
  } catch {
    // Use default templates
    templates.value = [
      { id: 't1', name: '产品展示' },
      { id: 't2', name: '促销活动' },
      { id: 't3', name: '品牌故事' },
      { id: 't4', name: '生活方式' }
    ]
    selectedTemplate.value = 't1'
  }
}

function simulateProgress() {
  progress.value = 0
  progressStatus.value = ''
  progressText.value = '正在连接生成服务...'

  const steps = [
    { p: 20, text: '正在分析产品信息...', delay: 600 },
    { p: 40, text: '正在匹配模板样式...', delay: 800 },
    { p: 60, text: '正在渲染广告图片...', delay: 1000 },
    { p: 80, text: '正在优化图片质量...', delay: 800 },
    { p: 95, text: '正在完成最终处理...', delay: 600 },
    { p: 100, text: '生成完成！', delay: 400 }
  ]

  let i = 0
  const tick = () => {
    if (i >= steps.length) return
    const step = steps[i]
    progress.value = step.p
    progressText.value = step.text
    if (step.p === 100) {
      progressStatus.value = 'success'
    }
    i++
    if (i < steps.length) {
      setTimeout(tick, step.delay)
    }
  }
  tick()
}

async function handleGenerate() {
  generating.value = true
  simulateProgress()

  try {
    const dim = dimensions.find((d) => d.key === selectedDimension.value)
    const res = await client.post('/ad/image/generate', {
      productName: props.adCopy?.productName,
      targetMarket: props.adCopy?.targetMarket,
      templateId: selectedTemplate.value,
      style: selectedStyle.value,
      width: dim?.width || 1500,
      height: dim?.height || 1500
    }) as any

    const data = res?.data ?? res
    generatedImage.value = {
      url: data?.url || data?.imageUrl,
      label: `${dim?.label || 'Amazon'} - ${dim?.width}x${dim?.height}`
    }
    progress.value = 100
    progressStatus.value = 'success'
    progressText.value = '生成完成！'
    ElMessage.success('图片生成成功')
  } catch (err: any) {
    progressStatus.value = 'exception'
    progressText.value = '生成失败'
    ElMessage.error('生成失败: ' + (err?.message || '未知错误'))
  } finally {
    generating.value = false
  }
}

async function downloadImage() {
  if (!generatedImage.value?.url) return
  saving.value = true
  try {
    if (window.electronAPI && generatedImage.value.url.startsWith('data:')) {
      const filename = `ad-image-${Date.now()}.png`
      const savedPath = await window.electronAPI.saveFile({
        subDir: 'images',
        filename,
        dataUrl: generatedImage.value.url
      })
      ElMessage.success(`已保存到 ${savedPath}`)
    } else {
      const a = document.createElement('a')
      a.href = generatedImage.value.url
      a.download = `ad-image-${Date.now()}.png`
      a.click()
      ElMessage.success('下载已开始')
    }
  } catch (err: any) {
    ElMessage.error('保存失败: ' + (err.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

function handleNext() {
  emit('next', {
    generatedImage: generatedImage.value,
    templateId: selectedTemplate.value,
    style: selectedStyle.value,
    dimension: selectedDimension.value
  })
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.ad-image-panel h3 {
  margin: 0 0 20px 0;
  color: #303133;
}

.section {
  margin-bottom: 24px;
}

.section h4 {
  margin: 0 0 12px 0;
  color: #606266;
}

.style-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.style-item {
  text-align: center;
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 12px 8px;
  transition: border-color 0.2s;
}

.style-item:hover {
  border-color: #409EFF;
}

.style-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
}

.style-preview {
  width: 60px;
  height: 60px;
  margin: 0 auto 8px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.style-name {
  font-size: 13px;
  color: #606266;
}

.dimension-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.dimension-item {
  text-align: center;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px;
  transition: all 0.2s;
}

.dimension-item:hover {
  border-color: #409EFF;
}

.dimension-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
  color: #409EFF;
}

.dim-label {
  display: block;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.dim-size {
  font-size: 12px;
  color: #909399;
}

.progress-section {
  margin-bottom: 24px;
}

.progress-text {
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
}

.result-section h4 {
  margin: 0 0 12px 0;
}

.image-result {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.result-image {
  width: 300px;
  height: 300px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.step-actions {
  margin-top: 24px;
  display: flex;
  justify-content: space-between;
}
</style>
