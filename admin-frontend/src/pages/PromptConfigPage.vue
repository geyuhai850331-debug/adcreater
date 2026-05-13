<template>
  <div class="prompt-config-page">
    <el-card shadow="never">
      <div class="page-header">
        <span class="page-title">Prompt 配置</span>
        <el-button type="primary" @click="openEdit()">
          <el-icon><Plus /></el-icon> 新增 Prompt
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column label="类别" width="120">
          <template #default="{ row }">
            <el-tag :type="categoryColor(row.category)" size="small">
              {{ categoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型" width="160" />
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isEnabled"
              @change="(val: boolean) => toggleEnabled(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="templateContent" label="模板内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="template-preview">{{ row.templateContent }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="deletePrompt(row)">删除</el-button>
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
        @size-change="fetchPrompts"
        @current-change="fetchPrompts"
      />
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editVisible"
      :title="isEditing ? '编辑 Prompt' : '新增 Prompt'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="110px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" placeholder="如 video-generation-v1" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择类别" style="width: 100%">
            <el-option label="文案生成" value="copy" />
            <el-option label="图片生成" value="image" />
            <el-option label="视频生成" value="video" />
            <el-option label="数字人" value="digital_human" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型" prop="modelId">
          <el-select v-model="editForm.modelId" placeholder="请选择模型" style="width: 100%">
            <el-option
              v-for="m in availableModels"
              :key="m.id"
              :label="m.modelName"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容" prop="templateContent">
          <el-input
            v-model="editForm.templateContent"
            type="textarea"
            :rows="12"
            placeholder="输入 Prompt 模板，变量用 {{variable}} 表示"
          />
        </el-form-item>
        <el-form-item label="变量提示">
          <el-tag
            v-for="v in detectedVariables"
            :key="v"
            style="margin-right: 8px; margin-bottom: 4px"
            type="info"
          >
            {{ wrapVariable(v) }}
          </el-tag>
          <span v-if="detectedVariables.length === 0" style="color: #909399; font-size: 12px">
            模板中使用 &lbrace;&lbrace;变量名&rbrace;&rbrace; 定义变量
          </span>
        </el-form-item>
        <el-form-item label="系统提示">
          <el-input
            v-model="editForm.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="可选，系统级上下文提示"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch
            v-model="editForm.isEnabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const tableData = ref<any[]>([])
const availableModels = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const categoryMap: Record<string, string> = {
  copy: '文案生成',
  image: '图片生成',
  video: '视频生成',
  digital_human: '数字人'
}

function categoryLabel(cat: string): string {
  return categoryMap[cat] || cat || '-'
}

function categoryColor(cat: string): string {
  const colors: Record<string, string> = {
    copy: 'success',
    image: '',
    video: 'warning',
    digital_human: 'danger'
  }
  return colors[cat] || 'info'
}

async function fetchPrompts() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    const res: any = await client.get('/ai/prompts', { params })
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

async function fetchModels() {
  try {
    // Fetch all models for the select dropdown
    const res: any = await client.get('/ai/models', { params: { page: 1, pageSize: 200 } })
    if (res) {
      availableModels.value = res.list || res.records || []
    }
  } catch {
    // silently fail
  }
}

function wrapVariable(name: string): string {
  return `{{${name}}}`
}

// Detected variables from template content
const detectedVariables = computed(() => {
  const matches = editForm.templateContent.match(/\{\{(\w+)\}\}/g)
  if (!matches) return []
  return [...new Set(matches.map(m => m.slice(2, -2)))]
})

// --- Edit ---
const editVisible = ref(false)
const isEditing = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: null as number | null,
  name: '',
  category: '',
  modelId: null as number | null,
  templateContent: '',
  systemPrompt: '',
  isEnabled: true
})

const editRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  modelId: [{ required: true, message: '请选择模型', trigger: 'change' }],
  templateContent: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

function openEdit(row?: any) {
  if (row) {
    isEditing.value = true
    editForm.id = row.id
    editForm.name = row.name || ''
    editForm.category = row.category || ''
    editForm.modelId = row.modelId || null
    editForm.templateContent = row.templateContent || ''
    editForm.systemPrompt = row.systemPrompt || ''
    editForm.isEnabled = row.isEnabled ?? true
  } else {
    isEditing.value = false
    editForm.id = null
    editForm.name = ''
    editForm.category = ''
    editForm.modelId = null
    editForm.templateContent = ''
    editForm.systemPrompt = ''
    editForm.isEnabled = true
  }
  editVisible.value = true
}

async function handleEditSubmit() {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async valid => {
    if (!valid) return
    editLoading.value = true
    try {
      const payload = {
        name: editForm.name,
        category: editForm.category,
        modelId: editForm.modelId,
        templateContent: editForm.templateContent,
        systemPrompt: editForm.systemPrompt,
        isEnabled: editForm.isEnabled
      }

      if (isEditing.value && editForm.id) {
        await client.put(`/ai/prompts/${editForm.id}`, payload)
        ElMessage.success('Prompt 更新成功')
      } else {
        await client.post('/ai/prompts', payload)
        ElMessage.success('Prompt 创建成功')
      }
      editVisible.value = false
      fetchPrompts()
    } catch (err: any) {
      const msg = err.response?.data?.message || '操作失败'
      ElMessage.error(msg)
    } finally {
      editLoading.value = false
    }
  })
}

// --- Toggle ---
async function toggleEnabled(row: any, val: boolean) {
  try {
    await client.put(`/ai/prompts/${row.id}/status`, { isEnabled: val })
    row.isEnabled = val
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch (err: any) {
    const msg = err.response?.data?.message || '操作失败'
    ElMessage.error(msg)
  }
}

// --- Delete ---
async function deletePrompt(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除 Prompt "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await client.delete(`/ai/prompts/${row.id}`)
    ElMessage.success('删除成功')
    fetchPrompts()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchPrompts()
  fetchModels()
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
.template-preview {
  color: #606266;
  font-size: 13px;
  font-family: monospace;
}
</style>
