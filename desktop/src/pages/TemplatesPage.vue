<template>
  <div class="templates-page">
    <div class="page-header">
      <h2>模板管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="syncTemplates" :loading="syncing">
          <el-icon><Refresh /></el-icon> 同步模板
        </el-button>
      </div>
    </div>

    <!-- Category Tabs -->
    <el-tabs v-model="activeCategory" class="category-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="图片模板" name="image" />
      <el-tab-pane label="视频模板" name="video" />
    </el-tabs>

    <!-- Template Grid -->
    <div v-if="filteredTemplates.length" class="template-grid">
      <el-card
        v-for="tpl in filteredTemplates"
        :key="tpl.id"
        class="template-card"
        shadow="hover"
      >
        <div class="template-thumb" @click="previewTemplate(tpl)">
          <el-image
            v-if="tpl.thumbnail"
            :src="tpl.thumbnail"
            fit="cover"
            class="thumb-img"
          />
          <div v-else class="thumb-placeholder">
            <el-icon :size="48" color="#c0c4cc">
              <PictureFilled v-if="tpl.type === 'image'" />
              <VideoCameraFilled v-else />
            </el-icon>
          </div>
        </div>
        <div class="template-info">
          <h4 class="template-name">{{ tpl.name }}</h4>
          <div class="template-meta">
            <el-tag :type="tpl.type === 'image' ? 'success' : 'warning'" size="small">
              {{ tpl.type === 'image' ? '图片' : '视频' }}
            </el-tag>
            <span class="template-size" v-if="tpl.width && tpl.height">
              {{ tpl.width }}x{{ tpl.height }}
            </span>
          </div>
        </div>
        <div class="template-actions">
          <el-button type="primary" size="small" @click="useTemplate(tpl)">
            使用
          </el-button>
          <el-button size="small" @click="previewTemplate(tpl)">
            预览
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-else description="暂无模板" />

    <!-- Preview Dialog -->
    <el-dialog v-model="previewVisible" title="模板预览" width="700px">
      <div v-if="previewTpl" class="preview-dialog-body">
        <el-image
          v-if="previewTpl.thumbnail"
          :src="previewTpl.thumbnail"
          fit="contain"
          class="preview-full-image"
        />
        <div v-else class="preview-placeholder">
          <el-icon :size="80" color="#c0c4cc"><PictureFilled /></el-icon>
          <p>暂无预览图</p>
        </div>
        <div class="preview-meta">
          <p><strong>名称：</strong>{{ previewTpl.name }}</p>
          <p><strong>类型：</strong>{{ previewTpl.type === 'image' ? '图片模板' : '视频模板' }}</p>
          <p v-if="previewTpl.width && previewTpl.height">
            <strong>尺寸：</strong>{{ previewTpl.width }} x {{ previewTpl.height }}
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="useTemplate(previewTpl); previewVisible = false">
          使用此模板
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, PictureFilled, VideoCameraFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const router = useRouter()

const templates = ref<any[]>([])
const syncing = ref(false)
const activeCategory = ref('all')
const previewVisible = ref(false)
const previewTpl = ref<any>(null)

const filteredTemplates = computed(() => {
  if (activeCategory.value === 'all') return templates.value
  return templates.value.filter((t) => t.type === activeCategory.value)
})

async function fetchTemplates() {
  try {
    const res = await client.get('/templates/list') as any
    const list = res?.data ?? res ?? []
    templates.value = Array.isArray(list) ? list : []
  } catch {
    templates.value = []
  }
}

async function syncTemplates() {
  syncing.value = true
  try {
    await client.post('/templates/sync')
    ElMessage.success('同步成功')
    await fetchTemplates()
  } catch (err: any) {
    ElMessage.error('同步失败: ' + (err?.message || '未知错误'))
  } finally {
    syncing.value = false
  }
}

function previewTemplate(tpl: any) {
  previewTpl.value = tpl
  previewVisible.value = true
}

function useTemplate(tpl: any) {
  router.push('/ad/create')
  sessionStorage.setItem('selectedTemplate', JSON.stringify(tpl))
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.templates-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.category-tabs {
  margin-bottom: 20px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.template-card {
  transition: transform 0.2s;
}

.template-card:hover {
  transform: translateY(-2px);
}

.template-thumb {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  height: 160px;
  margin-bottom: 12px;
}

.thumb-img {
  width: 100%;
  height: 100%;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-info {
  margin-bottom: 12px;
}

.template-name {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.template-size {
  font-size: 12px;
  color: #909399;
}

.template-actions {
  display: flex;
  gap: 8px;
}

.preview-dialog-body {
  text-align: center;
}

.preview-full-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
}

.preview-placeholder {
  padding: 60px;
  background: #f5f7fa;
  border-radius: 8px;
}

.preview-placeholder p {
  color: #909399;
  margin-top: 12px;
}

.preview-meta {
  text-align: left;
  margin-top: 20px;
  line-height: 1.8;
}

.preview-meta p {
  margin: 4px 0;
}
</style>
