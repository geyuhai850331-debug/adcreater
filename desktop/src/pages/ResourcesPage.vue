<template>
  <div class="resources-page">
    <h2 class="page-title">本地资源管理</h2>

    <!-- Tab Bar -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="图片" name="images" />
      <el-tab-pane label="视频" name="videos" />
      <el-tab-pane label="导出" name="exports" />
    </el-tabs>

    <!-- Month Filter -->
    <div class="filter-bar">
      <el-select
        v-model="selectedMonth"
        placeholder="选择月份"
        clearable
        @change="loadFiles"
        style="width: 200px"
      >
        <el-option
          v-for="m in availableMonths"
          :key="m.value"
          :label="m.label"
          :value="m.value"
        />
      </el-select>
      <span class="file-count" v-if="files.length">共 {{ files.length }} 个文件</span>
    </div>

    <!-- File Grid -->
    <div v-if="files.length" class="file-grid">
      <div v-for="file in files" :key="file.path" class="file-card">
        <div class="file-thumb" @click="previewFile(file)">
          <el-image
            v-if="isImage(file.name)"
            :src="fileUrl(file.path)"
            fit="cover"
            class="thumb-img"
            loading="lazy"
          />
          <div v-else class="thumb-video">
            <el-icon :size="48" color="#909399"><VideoCameraFilled /></el-icon>
            <span class="video-ext">{{ fileExt(file.name) }}</span>
          </div>
        </div>
        <div class="file-info">
          <el-tooltip :content="file.name" placement="top">
            <span class="file-name">{{ file.name }}</span>
          </el-tooltip>
          <span class="file-date">{{ formatDate(file.mtime) }}</span>
          <span class="file-size">{{ formatSize(file.size) }}</span>
        </div>
        <div class="file-actions">
          <el-button size="small" type="primary" link @click="previewFile(file)">
            预览
          </el-button>
          <el-popconfirm title="确认删除此文件？" @confirm="deleteFile(file)">
            <template #reference>
              <el-button size="small" type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无文件" />

    <!-- Preview Dialog -->
    <el-dialog v-model="previewVisible" :title="previewFileObj?.name" width="750px">
      <div v-if="previewFileObj" class="preview-dialog-body">
        <el-image
          v-if="isImage(previewFileObj.name)"
          :src="fileUrl(previewFileObj.path)"
          fit="contain"
          class="preview-image-full"
        />
        <video
          v-else
          :src="fileUrl(previewFileObj.path)"
          controls
          class="preview-video-full"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { VideoCameraFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('images')
const files = ref<FileEntry[]>([])
const selectedMonth = ref('')
const availableMonths = ref<Array<{ label: string; value: string }>>([])
const previewVisible = ref(false)
const previewFileObj = ref<FileEntry | null>(null)

const imageExts = ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp']
const videoExts = ['.mp4', '.webm', '.avi', '.mov', '.mkv']

function isImage(filename: string) {
  const ext = filename.toLowerCase().slice(filename.lastIndexOf('.'))
  return imageExts.includes(ext)
}

function fileExt(filename: string) {
  return filename.slice(filename.lastIndexOf('.'))
}

function fileUrl(filePath: string) {
  // On Windows, convert to file:// URL for display
  // In Electron, we can use the file path directly for <img>/<video> with proper CSP
  return `file://${filePath.replace(/\\/g, '/')}`
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function formatSize(bytes: number) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(1)} ${units[i]}`
}

function generateMonths() {
  const months: Array<{ label: string; value: string }> = []
  const now = new Date()
  for (let i = 0; i < 12; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    const label = `${d.getFullYear()}年${d.getMonth() + 1}月`
    months.push({ label, value })
  }
  availableMonths.value = months
  selectedMonth.value = months[0]?.value || ''
}

async function loadFiles() {
  if (!window.electronAPI) {
    ElMessage.warning('文件浏览仅在桌面端可用')
    files.value = []
    return
  }

  try {
    const result = await window.electronAPI.listFiles({
      subDir: activeTab.value,
      month: selectedMonth.value || ''
    })
    files.value = result || []
  } catch (err: any) {
    ElMessage.error('加载文件失败: ' + (err.message || '未知错误'))
    files.value = []
  }
}

function handleTabChange() {
  loadFiles()
}

function previewFile(file: FileEntry) {
  previewFileObj.value = file
  previewVisible.value = true
}

async function deleteFile(file: FileEntry) {
  if (!window.electronAPI) return
  try {
    await window.electronAPI.deleteFile(file.path)
    files.value = files.value.filter((f) => f.path !== file.path)
    ElMessage.success('删除成功')
  } catch (err: any) {
    ElMessage.error('删除失败: ' + (err.message || '未知错误'))
  }
}

onMounted(() => {
  generateMonths()
  loadFiles()
})
</script>

<style scoped>
.resources-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 16px 0;
  color: #303133;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.file-count {
  color: #909399;
  font-size: 13px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.file-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.file-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.file-thumb {
  cursor: pointer;
  height: 150px;
  overflow: hidden;
  background: #f5f7fa;
}

.thumb-img {
  width: 100%;
  height: 100%;
}

.thumb-video {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.video-ext {
  font-size: 12px;
  color: #909399;
}

.file-info {
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-date,
.file-size {
  font-size: 12px;
  color: #909399;
}

.file-actions {
  padding: 0 12px 10px;
  display: flex;
  justify-content: space-between;
}

.preview-dialog-body {
  text-align: center;
}

.preview-image-full {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 8px;
}

.preview-video-full {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 8px;
}
</style>
