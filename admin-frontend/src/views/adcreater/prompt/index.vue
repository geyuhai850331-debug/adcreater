<template>
  <ContentWrap>
    <div class="flex items-center justify-between">
      <span class="text-16px font-bold">Prompt 配置</span>
      <el-button type="primary" plain @click="openForm()">
        <Icon icon="ep:plus" />新增 Prompt
      </el-button>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="180" />
      <el-table-column label="类别" width="120">
        <template #default="{ row }">
          <el-tag :type="(categoryColor(row.category) as any)" size="small">
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
          <span class="text-13px text-gray-600 font-mono">{{ row.templateContent }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openForm(row)">
            <Icon icon="ep:edit" />编辑
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.pageSize"
      @pagination="fetchPrompts"
    />
  </ContentWrap>

  <PromptForm ref="formRef" @success="fetchPrompts" />
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { getPromptPage, updatePromptStatus, deletePrompt } from '@/api/adcreater/prompt'
import PromptForm from './PromptForm.vue'

defineOptions({ name: 'AdCreaterPrompt' })

const message = useMessage()

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 20 })

const categoryMap: Record<string, string> = {
  copy: '文案生成',
  image: '图片生成',
  video: '视频生成',
  digital_human: '数字人'
}

function categoryLabel(cat: string): string {
  return categoryMap[cat] || cat || '-'
}

type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

function categoryColor(cat: string): TagType {
  const map: Record<string, TagType> = {
    copy: 'success',
    image: 'info',
    video: 'warning',
    digital_human: 'danger'
  }
  return (map[cat] || 'info') as TagType
}

async function fetchPrompts() {
  loading.value = true
  try {
    const res = await getPromptPage(queryParams)
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
    await updatePromptStatus(row.id, val)
    row.isEnabled = val
    message.success(val ? '已启用' : '已禁用')
  } catch {
    // error handled by interceptor
  }
}

async function handleDelete(row: any) {
  try {
    await message.confirm(`确定要删除 Prompt "${row.name}" 吗？`)
    await deletePrompt(row.id)
    message.success('删除成功')
    fetchPrompts()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchPrompts()
})
</script>
