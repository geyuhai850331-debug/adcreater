<template>
  <div class="ad-video-panel">
    <h3>Step 3：广告视频生成</h3>

    <!-- Select Generated Images -->
    <div class="section">
      <h4>选择素材图片</h4>
      <div v-if="adImage?.generatedImage" class="image-selector">
        <el-checkbox v-model="useImage" :label="adImage.generatedImage.label" size="large" />
        <el-image
          v-if="adImage.generatedImage.url"
          :src="adImage.generatedImage.url"
          fit="contain"
          class="thumb-preview"
        />
      </div>
      <el-empty v-else description="请先在 Step 2 生成图片" :image-size="60" />
    </div>

    <!-- Duration -->
    <div class="section">
      <h4>视频时长：{{ duration }} 秒</h4>
      <el-slider
        v-model="duration"
        :min="5"
        :max="60"
        :step="5"
        show-stops
        show-input
        style="max-width: 400px"
      />
    </div>

    <!-- Style -->
    <div class="section">
      <h4>视频风格</h4>
      <el-radio-group v-model="videoStyle">
        <el-radio-button value="smooth">平滑过渡</el-radio-button>
        <el-radio-button value="kenburns">Ken Burns</el-radio-button>
        <el-radio-button value="zoom">缩放聚焦</el-radio-button>
        <el-radio-button value="slide">滑动展示</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Generate -->
    <div class="section">
      <el-button
        type="primary"
        size="large"
        :loading="generating"
        @click="handleGenerate"
      >
        {{ generating ? '生成中...' : '生成视频' }}
      </el-button>
    </div>

    <!-- Progress -->
    <div v-if="generating" class="progress-section">
      <el-progress :percentage="progress" :status="progressStatus" />
      <p class="progress-text">{{ progressText }}</p>
    </div>

    <!-- Result -->
    <div v-if="generatedVideo" class="result-section">
      <h4>视频预览</h4>
      <div class="video-result">
        <video :src="generatedVideo.url" controls class="result-video" />
        <div class="result-actions">
          <el-button type="primary" @click="saveVideo" :loading="saving">
            <el-icon><Download /></el-icon> 保存到本地
          </el-button>
        </div>
      </div>

      <div class="step-actions">
        <el-button @click="emit('prev')">上一步</el-button>
        <el-button type="primary" @click="handleNext">下一步：预览</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const props = defineProps<{
  adImage: any
}>()

const emit = defineEmits<{
  (e: 'next', data: any): void
  (e: 'prev'): void
}>()

const useImage = ref(true)
const duration = ref(15)
const videoStyle = ref('smooth')

const generating = ref(false)
const progress = ref(0)
const progressStatus = ref<'success' | 'exception' | 'warning' | ''>('')
const progressText = ref('')
const generatedVideo = ref<any>(null)
const saving = ref(false)

function simulateProgress() {
  progress.value = 0
  progressStatus.value = ''
  progressText.value = '正在初始化视频合成...'

  const steps = [
    { p: 15, text: '正在加载素材图片...', delay: 500 },
    { p: 30, text: '正在应用转场效果...', delay: 700 },
    { p: 50, text: '正在合成视频轨道...', delay: 1000 },
    { p: 70, text: '正在添加背景音乐...', delay: 800 },
    { p: 85, text: '正在渲染视频帧...', delay: 1200 },
    { p: 95, text: '正在编码输出...', delay: 900 },
    { p: 100, text: '视频生成完成！', delay: 500 }
  ]

  let i = 0
  const tick = () => {
    if (i >= steps.length) return
    const step = steps[i]
    progress.value = step.p
    progressText.value = step.text
    if (step.p === 100) progressStatus.value = 'success'
    i++
    if (i < steps.length) setTimeout(tick, step.delay)
  }
  tick()
}

async function handleGenerate() {
  generating.value = true
  simulateProgress()

  try {
    const res = await client.post('/ad/video/generate', {
      imageUrl: props.adImage?.generatedImage?.url,
      duration: duration.value,
      style: videoStyle.value,
      templateId: props.adImage?.templateId
    }) as any

    const data = res?.data ?? res
    generatedVideo.value = {
      url: data?.url || data?.videoUrl,
      label: `广告视频 - ${duration.value}s`
    }
    progress.value = 100
    progressStatus.value = 'success'
    progressText.value = '视频生成完成！'
    ElMessage.success('视频生成成功')
  } catch (err: any) {
    progressStatus.value = 'exception'
    progressText.value = '生成失败'
    ElMessage.error('生成失败: ' + (err?.message || '未知错误'))
  } finally {
    generating.value = false
  }
}

async function saveVideo() {
  if (!generatedVideo.value?.url) return
  saving.value = true
  try {
    if (window.electronAPI && generatedVideo.value.url.startsWith('data:')) {
      const filename = `ad-video-${Date.now()}.mp4`
      const savedPath = await window.electronAPI.saveFile({
        subDir: 'videos',
        filename,
        dataUrl: generatedVideo.value.url
      })
      ElMessage.success(`已保存到 ${savedPath}`)
    } else {
      const a = document.createElement('a')
      a.href = generatedVideo.value.url
      a.download = `ad-video-${Date.now()}.mp4`
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
    generatedVideo: generatedVideo.value,
    duration: duration.value,
    videoStyle: videoStyle.value
  })
}
</script>

<style scoped>
.ad-video-panel h3 {
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

.image-selector {
  display: flex;
  align-items: center;
  gap: 16px;
}

.thumb-preview {
  width: 120px;
  height: 120px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
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

.video-result {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.result-video {
  width: 400px;
  max-height: 300px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
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
