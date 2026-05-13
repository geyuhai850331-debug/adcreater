<template>
  <div class="ad-preview-panel">
    <h3>Step 4：预览与完成</h3>

    <el-alert
      title="广告制作完成！请预览确认后下载全部素材。"
      type="success"
      :closable="false"
      show-icon
      class="preview-alert"
    />

    <!-- Copy Preview -->
    <el-card class="preview-card" v-if="adCopy">
      <template #header>
        <span>广告文案</span>
        <el-tag size="small" type="success" style="margin-left: 8px">已生成</el-tag>
      </template>
      <div class="copy-preview">
        <p><strong>产品：</strong>{{ adCopy.productName }}</p>
        <p><strong>目标市场：</strong>{{ marketNames[adCopy.targetMarket] || adCopy.targetMarket }}</p>
        <p><strong>卖点：</strong>{{ (adCopy.sellingPoints || []).join('、') }}</p>
        <div v-if="adCopy.translatedCopy" class="translated-box">
          <p><strong>翻译文案：</strong></p>
          <p class="translated-text">{{ adCopy.translatedCopy.translated }}</p>
        </div>
      </div>
    </el-card>

    <!-- Image Preview -->
    <el-card class="preview-card" v-if="adImage?.generatedImage">
      <template #header>
        <span>广告图片</span>
        <el-tag size="small" type="success" style="margin-left: 8px">已生成</el-tag>
      </template>
      <div class="image-preview">
        <el-image
          :src="adImage.generatedImage.url"
          fit="contain"
          class="preview-img"
          :preview-src-list="[adImage.generatedImage.url]"
        />
        <p>{{ adImage.generatedImage.label }}</p>
      </div>
    </el-card>

    <!-- Video Preview -->
    <el-card class="preview-card" v-if="adVideo?.generatedVideo">
      <template #header>
        <span>广告视频</span>
        <el-tag size="small" type="success" style="margin-left: 8px">已生成</el-tag>
      </template>
      <div class="video-preview">
        <video :src="adVideo.generatedVideo.url" controls class="preview-vid" />
        <p>{{ adVideo.generatedVideo.label }}</p>
      </div>
    </el-card>

    <!-- Actions -->
    <div class="preview-actions">
      <el-button @click="emit('prev')">上一步</el-button>
      <el-button type="success" size="large" @click="handleSaveAll">
        <el-icon><Download /></el-icon> 保存全部到本地
      </el-button>
      <el-button type="primary" size="large" @click="handleDownloadAll">
        <el-icon><FolderOpened /></el-icon> 下载全部素材
      </el-button>
      <el-button @click="handleNew">重新制作</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Download, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  adCopy: any
  adImage: any
  adVideo: any
}>()

const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'reset'): void
}>()

const marketNames: Record<string, string> = {
  US: '美国', UK: '英国', DE: '德国', JP: '日本', SA: '沙特', BR: '巴西'
}

async function handleSaveAll() {
  const items: Array<{ url: string; type: string }> = []
  if (props.adImage?.generatedImage?.url) {
    items.push({ url: props.adImage.generatedImage.url, type: 'image' })
  }
  if (props.adVideo?.generatedVideo?.url) {
    items.push({ url: props.adVideo.generatedVideo.url, type: 'video' })
  }

  if (items.length === 0) {
    ElMessage.warning('没有可保存的素材')
    return
  }

  let saved = 0
  for (const item of items) {
    if (!window.electronAPI) {
      // Fallback for browser
      const a = document.createElement('a')
      a.href = item.url
      a.download = `ad-${item.type}-${Date.now()}.${item.type === 'video' ? 'mp4' : 'png'}`
      a.click()
      saved++
      continue
    }
    try {
      const ext = item.type === 'video' ? 'mp4' : 'png'
      await window.electronAPI.saveFile({
        subDir: item.type === 'video' ? 'videos' : 'images',
        filename: `ad-${item.type}-${Date.now()}.${ext}`,
        dataUrl: item.url
      })
      saved++
    } catch (err: any) {
      ElMessage.error(`保存${item.type === 'video' ? '视频' : '图片'}失败: ${err.message}`)
    }
  }
  ElMessage.success(`成功保存 ${saved} 个文件到本地素材库`)
}

function handleDownloadAll() {
  // Store preview data and navigate to preview page
  const previewData = {
    copy: props.adCopy?.translatedCopy
      ? { [marketNames[props.adCopy.targetMarket] || 'Target']: props.adCopy.translatedCopy.translated }
      : null,
    images: props.adImage?.generatedImage ? [props.adImage.generatedImage] : [],
    video: props.adVideo?.generatedVideo || null
  }
  sessionStorage.setItem('adPreviewData', JSON.stringify(previewData))
  window.open('/#/ad/preview', '_blank')
}

function handleNew() {
  sessionStorage.removeItem('adPreviewData')
  emit('reset')
}
</script>

<style scoped>
.ad-preview-panel h3 {
  margin: 0 0 20px 0;
  color: #303133;
}

.preview-alert {
  margin-bottom: 20px;
}

.preview-card {
  margin-bottom: 20px;
}

.copy-preview {
  line-height: 1.8;
}

.translated-box {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.translated-text {
  color: #303133;
  font-size: 15px;
  margin: 4px 0 0 0;
}

.image-preview,
.video-preview {
  text-align: center;
}

.preview-img {
  max-width: 400px;
  max-height: 300px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.preview-vid {
  max-width: 500px;
  max-height: 350px;
  border-radius: 8px;
}

.image-preview p,
.video-preview p {
  margin: 8px 0 0;
  color: #909399;
}

.preview-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 8px;
}
</style>
