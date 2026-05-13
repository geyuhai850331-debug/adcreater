<template>
  <div class="template-manage-page">
    <el-card shadow="never">
      <div class="page-header">
        <span class="page-title">模板管理</span>
        <el-button type="primary" @click="openEdit()">
          <el-icon><Plus /></el-icon> 新增模板
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" width="180" />
        <el-table-column label="类别" width="120">
          <template #default="{ row }">
            <el-tag :type="categoryColor(row.category)" size="small">
              {{ categoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="缩略图" width="100">
          <template #default="{ row }">
            <el-avatar
              v-if="row.thumbnailUrl"
              :src="row.thumbnailUrl"
              shape="square"
              :size="60"
            />
            <el-icon v-else :size="40" color="#c0c4cc"><Picture /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'DRAFT' ? 'info' : 'warning'"
              size="small"
            >
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentVersion" label="当前版本" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="success" size="small" link @click="openPublish(row)">发布版本</el-button>
            <el-button type="warning" size="small" link @click="viewVersions(row)">版本列表</el-button>
            <el-button type="danger" size="small" link @click="deleteTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="fetchTemplates"
        @current-change="fetchTemplates"
      />
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editVisible"
      :title="isEditing ? '编辑模板' : '新增模板'"
      width="620px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="editForm.name" placeholder="如 电商促销模板A" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择类别" style="width: 100%">
            <el-option label="文案" value="copy" />
            <el-option label="图片" value="image" />
            <el-option label="视频" value="video" />
            <el-option label="数字人" value="digital_human" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="模板描述"
          />
        </el-form-item>
        <el-form-item label="缩略图">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="handleThumbnailChange"
            :file-list="thumbnailFileList"
            list-type="picture"
            accept="image/*"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon> 上传缩略图
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="配置数据" prop="configData">
          <el-input
            v-model="editForm.configData"
            type="textarea"
            :rows="6"
            placeholder='JSON 格式配置，如 {"background": "#ffffff", "elements": []}'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Publish Dialog -->
    <el-dialog
      v-model="publishVisible"
      title="发布新版本"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="100px">
        <el-form-item label="模板名称">
          <el-input :model-value="publishForm.templateName" disabled />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="publishForm.version" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="URL" prop="url">
          <el-input v-model="publishForm.url" placeholder="如 https://cdn.example.com/tpl/v1.0.0.json" />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input
            v-model="publishForm.changelog"
            type="textarea"
            :rows="4"
            placeholder="本次更新的内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishLoading" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>

    <!-- Versions Dialog -->
    <el-dialog v-model="versionsVisible" title="版本列表" width="720px">
      <el-table :data="versionsData" stripe border max-height="400">
        <el-table-column prop="version" label="版本号" width="120" />
        <el-table-column prop="url" label="URL" min-width="250" show-overflow-tooltip />
        <el-table-column prop="changelog" label="更新日志" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const categoryMap: Record<string, string> = {
  copy: '文案',
  image: '图片',
  video: '视频',
  digital_human: '数字人'
}

function categoryLabel(cat: string): string {
  return categoryMap[cat] || cat || '-'
}

function categoryColor(cat: string): string {
  const colors: Record<string, string> = { copy: 'success', image: '', video: 'warning', digital_human: 'danger' }
  return colors[cat] || 'info'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档' }
  return map[status] || status
}

async function fetchTemplates() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    const res: any = await client.get('/templates', { params })
    if (res) {
      tableData.value = res.list || res.records || []
      pagination.total = res.total ?? 0
    }
  } catch {
    // silently fail
  } finally {
    loading.value = false
  }
}

// --- Edit ---
const editVisible = ref(false)
const isEditing = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const thumbnailFileList = ref<any[]>([])
const editForm = reactive({
  id: null as number | null,
  name: '',
  category: '',
  description: '',
  configData: '',
  thumbnailFile: null as File | null
})

const editRules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
}

function handleThumbnailChange(file: UploadFile) {
  editForm.thumbnailFile = file.raw || null
}

function openEdit(row?: any) {
  thumbnailFileList.value = []
  if (row) {
    isEditing.value = true
    editForm.id = row.id
    editForm.name = row.name || ''
    editForm.category = row.category || ''
    editForm.description = row.description || ''
    editForm.configData = row.configData
      ? (typeof row.configData === 'string' ? row.configData : JSON.stringify(row.configData, null, 2))
      : ''
    editForm.thumbnailFile = null
  } else {
    isEditing.value = false
    editForm.id = null
    editForm.name = ''
    editForm.category = ''
    editForm.description = ''
    editForm.configData = ''
    editForm.thumbnailFile = null
  }
  editVisible.value = true
}

async function handleEditSubmit() {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async valid => {
    if (!valid) return
    editLoading.value = true
    try {
      const formData = new FormData()
      formData.append('name', editForm.name)
      formData.append('category', editForm.category)
      formData.append('description', editForm.description)
      formData.append('configData', editForm.configData)
      if (editForm.thumbnailFile) {
        formData.append('thumbnail', editForm.thumbnailFile)
      }

      if (isEditing.value && editForm.id) {
        await client.put(`/templates/${editForm.id}`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        ElMessage.success('模板更新成功')
      } else {
        await client.post('/templates', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        ElMessage.success('模板创建成功')
      }
      editVisible.value = false
      fetchTemplates()
    } catch (err: any) {
      const msg = err.response?.data?.message || '操作失败'
      ElMessage.error(msg)
    } finally {
      editLoading.value = false
    }
  })
}

// --- Publish ---
const publishVisible = ref(false)
const publishLoading = ref(false)
const publishFormRef = ref<FormInstance>()
const publishForm = reactive({
  templateId: null as number | null,
  templateName: '',
  version: '',
  url: '',
  changelog: ''
})

const publishRules: FormRules = {
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  url: [{ required: true, message: '请输入 URL', trigger: 'blur' }],
  changelog: [{ required: true, message: '请输入更新日志', trigger: 'blur' }]
}

function openPublish(row: any) {
  publishForm.templateId = row.id
  publishForm.templateName = row.name || ''
  publishForm.version = ''
  publishForm.url = ''
  publishForm.changelog = ''
  publishVisible.value = true
}

async function handlePublish() {
  if (!publishFormRef.value) return
  await publishFormRef.value.validate(async valid => {
    if (!valid) return
    publishLoading.value = true
    try {
      await client.post(`/templates/${publishForm.templateId}/versions`, {
        version: publishForm.version,
        url: publishForm.url,
        changelog: publishForm.changelog
      })
      ElMessage.success('版本发布成功')
      publishVisible.value = false
      fetchTemplates()
    } catch (err: any) {
      const msg = err.response?.data?.message || '发布失败'
      ElMessage.error(msg)
    } finally {
      publishLoading.value = false
    }
  })
}

// --- Versions ---
const versionsVisible = ref(false)
const versionsData = ref<any[]>([])

async function viewVersions(row: any) {
  try {
    const res: any = await client.get(`/templates/${row.id}/versions`)
    versionsData.value = res.list || res.records || res || []
    versionsVisible.value = true
  } catch (err: any) {
    const msg = err.response?.data?.message || '获取版本列表失败'
    ElMessage.error(msg)
  }
}

// --- Delete ---
async function deleteTemplate(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除模板 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await client.delete(`/templates/${row.id}`)
    ElMessage.success('删除成功')
    fetchTemplates()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
</style>
