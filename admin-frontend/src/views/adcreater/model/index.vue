<template>
  <ContentWrap>
    <div class="flex items-center justify-between">
      <span class="text-16px font-bold">AI 模型配置</span>
      <el-button type="primary" plain @click="openForm()">
        <Icon icon="ep:plus" />新增模型
      </el-button>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="tableData" stripe border>
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
          <el-button type="primary" size="small" link @click="openForm(row)">
            <Icon icon="ep:edit" />编辑
          </el-button>
          <el-button type="success" size="small" link @click="testConnection(row)">
            <Icon icon="ep:connection" />测试连接
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.pageSize"
      @pagination="fetchModels"
    />
  </ContentWrap>

  <ModelForm ref="formRef" @success="fetchModels" />
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { getModelPage, updateModelStatus, testModelConnection, deleteModel } from '@/api/adcreater/model'
import ModelForm from './ModelForm.vue'

defineOptions({ name: 'AdCreaterModel' })

const message = useMessage()

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 20 })

async function fetchModels() {
  loading.value = true
  try {
    const res = await getModelPage(queryParams)
    if (res) {
      tableData.value = res.list || res.records || []
      total.value = res.total ?? 0
    }
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

const formRef = ref()
function openForm(row?: any) {
  formRef.value.open(row)
}

async function toggleEnabled(row: any, val: boolean) {
  try {
    await updateModelStatus(row.id, val)
    row.isEnabled = val
    message.success(val ? '已启用' : '已禁用')
  } catch {
    // error handled by interceptor
  }
}

async function testConnection(row: any) {
  try {
    const success = await testModelConnection({ id: row.id } as any)
    if (!success) return
    message.success(`${row.modelName} 连接测试成功`)
  } catch {
    // error handled by interceptor
  }
}

async function handleDelete(row: any) {
  try {
    await message.confirm(`确定要删除模型 "${row.modelName}" 吗？`)
    await deleteModel(row.id)
    message.success('删除成功')
    fetchModels()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchModels()
})
</script>
