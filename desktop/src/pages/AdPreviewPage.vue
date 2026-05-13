<template>
  <div class="ad-preview-page">
    <div class="page-header">
      <h2>广告预览</h2>
      <div class="header-actions">
        <el-button type="primary" @click="downloadAll" :loading="downloading">
          <el-icon><Download /></el-icon> 下载全部
        </el-button>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <div v-if="!previewData" class="empty-state">
      <el-empty description="暂无预览内容，请先创建广告">
        <el-button type="primary" @click="$router.push('/ad/create')">去创建</el-button>
      </el-empty>
    </div>

    <div v-else class="preview-content">
      <!-- Copy Section -->
      <el-card class="preview-section" v-if="previewData.copy">
        <template #header><span>广告文案</span></template>
        <div class="copy-display">
          <div v-for="(text, lang) in previewData.copy" :key="lang" class="copy-item">
            <el-tag size="small" class="lang-tag">{{ lang }}</el-tag>
            <p>{{ text }}</p>
          </div>
        </div>
      </el-card>

      <!-- Image Section -->
      <el-card class="preview-section" v-if="previewData.images?.length">
        <template #header><span>广告图片</span></template>
        <div class="image-grid">
          <div v-for="(img, idx) in previewData.images" :key="idx" class="image-item">
            <el-image
              :src="img.url"
              :preview-src-list="[img.url]"
              fit="contain"
              class="preview-image"
            />
            <p class="image-label">{{ img.label || `图片 ${idx + 1}` }}</p>
            <el-button size="small" type="primary" link @click="downloadFile(img)">
              下载
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- Video Section -->
      <el-card class="preview-section" v-if="previewData.video">
        <template #header><span>广告视频</span></template>
        <div class="video-display">
          <video :src="previewData.video.url" controls class="preview-video" />
          <p class="video-label">{{ previewData.video.label }}</p>
          <el-button size="small" type="primary" link @click="downloadFile(previewData.video)">
            下载视频
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const previewData = ref<any>(null)
const downloading = ref(false)

onMounted(() => {
  // Load preview data from sessionStorage (set by AdCreatePage after generation)
  const stored = sessionStorage.getItem('adPreviewData')
  if (stored) {
    try {
      previewData.value = JSON.parse(stored)
    } catch { /* ignore */ }
  }
})

async function downloadFile(item: any) {
  if (!item?.url) return
  if (window.electronAPI && item.url.startsWith('data:')) {
    try {
      const subDir = item.type === 'video' ? 'videos' : 'images'
      const ext = item.type === 'video' ? 'mp4' : 'png'
      const filename = `${Date.now()}.${ext}`
      const savedPath = await window.electronAPI.saveFile({
        subDir,
        filename,
        dataUrl: item.url
      })
      ElMessage.success(`已保存到 ${savedPath}`)
    } catch (err: any) {
      ElMessage.error('保存失败: ' + (err.message || '未知错误'))
    }
  } else {
    // Fallback: open in browser
    const a = document.createElement('a')
    a.href = item.url
    a.download = item.label || 'download'
    a.click()
  }
}

async function downloadAll() {
  if (!previewData.value) return
  downloading.value = true
  try {
    const items = [
      ...(previewData.value.images || []).map((i: any) => ({ ...i, type: 'image' })),
      ...(previewData.value.video ? [{ ...previewData.value.video, type: 'video' }] : [])
    ]
    for (const item of items) {
      await downloadFile(item)
    }
    ElMessage.success('全部下载完成')
  } finally {
    downloading.value = false
  }
}
</script>

<style scoped>
.ad-preview-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  padding: 60px 0;
}

.preview-section {
  margin-bottom: 20px;
}

.copy-display {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.copy-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.lang-tag {
  margin-top: 2px;
  flex-shrink: 0;
}

.copy-item p {
  margin: 0;
  line-height: 1.6;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.image-item {
  text-align: center;
}

.preview-image {
  width: 100%;
  height: 180px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.image-label {
  margin: 8px 0 4px;
  font-size: 13px;
  color: #606266;
}

.video-display {
  text-align: center;
}

.preview-video {
  max-width: 100%;
  max-height: 400px;
  border-radius: 6px;
}

.video-label {
  margin: 8px 0 4px;
  font-size: 13px;
  color: #606266;
}
</style>
