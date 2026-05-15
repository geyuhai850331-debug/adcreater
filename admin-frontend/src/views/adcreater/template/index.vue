<template>
  <ContentWrap>
    <div class="flex items-center justify-between">
      <span class="text-16px font-bold">模板管理</span>
      <el-button type="primary" plain @click="openForm()">
        <Icon icon="ep:plus" />新增模板
      </el-button>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="模板名称" width="180" />
      <el-table-column label="类别" width="120">
        <template #default="{ row }">
          <el-tag :type="(categoryColor(row.category) as any)" size="small">
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
          <Icon v-else icon="ep:picture" :size="40" color="#c0c4cc" />
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
          <el-button type="primary" size="small" link @click="openForm(row)">
            <Icon icon="ep:edit" />编辑
          </el-button>
          <el-button type="success" size="small" link @click="openPublish(row)">
            <Icon icon="ep:upload" />发布版本
          </el-button>
          <el-button type="warning" size="small" link @click="viewVersions(row)">
            <Icon icon="ep:list" />版本列表
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.pageSize"
      @pagination="fetchTemplates"
    />
  </ContentWrap>

  <el-dialog v-model="versionsVisible" title="版本列表" width="720px">
    <el-table :data="versionsData" stripe border max-height="400">
      <el-table-column prop="version" label="版本号" width="120" />
      <el-table-column prop="url" label="URL" min-width="250" show-overflow-tooltip />
      <el-table-column prop="changelog" label="更新日志" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="发布时间" width="180" />
    </el-table>
  </el-dialog>

  <TemplateForm ref="formRef" @success="fetchTemplates" />
  <PublishForm ref="publishFormRef" @success="fetchTemplates" />
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { getTemplatePage, deleteTemplate, getTemplateVersions } from '@/api/adcreater/template'
import TemplateForm from './TemplateForm.vue'
import PublishForm from './PublishForm.vue'

defineOptions({ name: 'AdCreaterTemplate' })

const message = useMessage()

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 20 })

const categoryMap: Record<string, string> = {
  copy: '文案',
  image: '图片',
  video: '视频',
  digital_human: '数字人'
}

function categoryLabel(cat: string): string {
  return categoryMap[cat] || cat || '-'
}

type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

function categoryColor(cat: string): TagType {
  const colors: Record<string, TagType> = { copy: 'success', image: 'info', video: 'warning', digital_human: 'danger' }
  return colors[cat] || 'info'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档' }
  return map[status] || status
}

async function fetchTemplates() {
  loading.value = true
  try {
    const res = await getTemplatePage(queryParams)
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

const publishFormRef = ref()
function openPublish(row: any) {
  publishFormRef.value.open(row)
}

const versionsVisible = ref(false)
const versionsData = ref<any[]>([])

async function viewVersions(row: any) {
  try {
    const res = await getTemplateVersions(row.id)
    versionsData.value = (res as any).list || (res as any).records || res || []
    versionsVisible.value = true
  } catch {
    // error handled by interceptor
  }
}

async function handleDelete(row: any) {
  try {
    await message.confirm(`确定要删除模板 "${row.name}" 吗？`)
    await deleteTemplate(row.id)
    message.success('删除成功')
    fetchTemplates()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchTemplates()
})
</script>
