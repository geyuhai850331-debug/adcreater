<template>
  <div class="model-config-page">
    <el-card shadow="never">
      <div class="page-header">
        <span class="page-title">AI 模型配置</span>
        <el-button type="primary" @click="openEdit()">
          <el-icon><Plus /></el-icon> 新增模型
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="modelName" label="模型名称" width="160" />
        <el-table-column prop="adapterClass" label="适配器类" width="200" show-overflow-tooltip />
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isEnabled"
              @change="(val: boolean) => toggleEnabled(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="endpointUrl" label="接口地址" show-overflow-tooltip min-width="200" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="success" size="small" link @click="testConnection(row)">
              <el-icon><Connection /></el-icon> 测试连接
            </el-button>
            <el-button type="danger" size="small" link @click="deleteModel(row)">删除</el-button>
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
        @size-change="fetchModels"
        @current-change="fetchModels"
      />
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editVisible"
      :title="isEditing ? '编辑模型' : '新增模型'"
      width="620px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="110px">
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="editForm.modelName" placeholder="如 gpt-4o, glm-4-flash" />
        </el-form-item>
        <el-form-item label="适配器类" prop="adapterClass">
          <el-input v-model="editForm.adapterClass" placeholder="如 OpenAiAdapter" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="editForm.apiKey"
            :type="showApiKey ? 'text' : 'password'"
            placeholder="API 密钥"
            show-password
            @focus="onApiKeyFocus"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="接口地址" prop="endpointUrl">
          <el-input v-model="editForm.endpointUrl" placeholder="如 https://api.openai.com/v1" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch
            v-model="editForm.isEnabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number
            v-model="editForm.priority"
            :min="0"
            :max="999"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="额外配置" prop="extraConfig">
          <el-input
            v-model="editForm.extraConfig"
            type="textarea"
            :rows="4"
            placeholder='JSON 格式，如 {"temperature": 0.7, "max_tokens": 4096}'
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
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

async function fetchModels() {
  loading.value = true
  try {
    const params = { page: pagination.page, pageSize: pagination.pageSize }
    const res: any = await client.get('/ai/models', { params })
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
const showApiKey = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: null as number | null,
  modelName: '',
  adapterClass: '',
  apiKey: '',
  endpointUrl: '',
  isEnabled: true,
  priority: 1,
  extraConfig: ''
})

const editRules: FormRules = {
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  adapterClass: [{ required: true, message: '请输入适配器类', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入 API Key', trigger: 'blur' }]
}

function onApiKeyFocus() {
  // When editing, show the key field so the user can type a new one
}

function openEdit(row?: any) {
  if (row) {
    isEditing.value = true
    editForm.id = row.id
    editForm.modelName = row.modelName || ''
    editForm.adapterClass = row.adapterClass || ''
    editForm.apiKey = '' // never pre-fill api key for security; user must re-enter
    editForm.endpointUrl = row.endpointUrl || ''
    editForm.isEnabled = row.isEnabled ?? true
    editForm.priority = row.priority ?? 1
    editForm.extraConfig = row.extraConfig
      ? (typeof row.extraConfig === 'string' ? row.extraConfig : JSON.stringify(row.extraConfig, null, 2))
      : ''
    showApiKey.value = false
  } else {
    isEditing.value = false
    editForm.id = null
    editForm.modelName = ''
    editForm.adapterClass = ''
    editForm.apiKey = ''
    editForm.endpointUrl = ''
    editForm.isEnabled = true
    editForm.priority = 1
    editForm.extraConfig = ''
    showApiKey.value = false
  }
  editVisible.value = true
}

async function handleEditSubmit() {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async valid => {
    if (!valid) return
    editLoading.value = true
    try {
      const payload: any = {
        modelName: editForm.modelName,
        adapterClass: editForm.adapterClass,
        apiKey: editForm.apiKey,
        endpointUrl: editForm.endpointUrl,
        isEnabled: editForm.isEnabled,
        priority: editForm.priority,
        extraConfig: editForm.extraConfig
      }

      if (isEditing.value && editForm.id) {
        // Don't send apiKey if it was left blank (keeping existing)
        if (!editForm.apiKey) delete payload.apiKey
        await client.put(`/ai/models/${editForm.id}`, payload)
        ElMessage.success('模型更新成功')
      } else {
        await client.post('/ai/models', payload)
        ElMessage.success('模型创建成功')
      }
      editVisible.value = false
      fetchModels()
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
    await client.put(`/ai/models/${row.id}/status`, { isEnabled: val })
    row.isEnabled = val
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch (err: any) {
    const msg = err.response?.data?.message || '操作失败'
    ElMessage.error(msg)
  }
}

// --- Test Connection ---
async function testConnection(row: any) {
  try {
    await client.post(`/ai/models/${row.id}/test`)
    ElMessage.success(`${row.modelName} 连接测试成功`)
  } catch (err: any) {
    const msg = err.response?.data?.message || '连接测试失败'
    ElMessage.error(msg)
  }
}

// --- Delete ---
async function deleteModel(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除模型 "${row.modelName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await client.delete(`/ai/models/${row.id}`)
    ElMessage.success('删除成功')
    fetchModels()
  } catch {
    // cancelled
  }
}

onMounted(() => {
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
</style>
