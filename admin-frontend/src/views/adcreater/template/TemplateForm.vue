<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑模板' : '新增模板'"
    width="620px"
    :close-on-click-modal="false"
    @open="onOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="form.name" placeholder="如 电商促销模板A" />
      </el-form-item>
      <el-form-item label="类别" prop="category">
        <el-select v-model="form.category" placeholder="请选择类别" class="w-1/1">
          <el-option label="文案" value="copy" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
          <el-option label="数字人" value="digital_human" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="模板描述"
        />
      </el-form-item>
      <el-form-item label="缩略图 URL" prop="thumbnailUrl">
        <el-input v-model="form.thumbnailUrl" placeholder="如 https://cdn.example.com/thumb.png" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择状态" class="w-1/1">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createTemplate, updateTemplate } from '@/api/adcreater/template'

defineOptions({ name: 'AdCreaterTemplateForm' })

const emit = defineEmits<{ success: [] }>()
const message = useMessage()

const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: null as number | null,
  name: '',
  category: '',
  description: '',
  thumbnailUrl: '',
  status: 'DRAFT'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
}

function onOpen() {
  formRef.value?.resetFields()
}

function open(row?: any) {
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.name = row.name || ''
    form.category = row.category || ''
    form.description = row.description || ''
    form.thumbnailUrl = row.thumbnailUrl || ''
    form.status = row.status || 'DRAFT'
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.category = ''
    form.description = ''
    form.thumbnailUrl = ''
    form.status = 'DRAFT'
  }
  visible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const payload = {
        name: form.name,
        category: form.category,
        description: form.description,
        thumbnailUrl: form.thumbnailUrl,
        status: form.status
      }
      if (isEdit.value && form.id) {
        await updateTemplate(form.id, payload)
        message.success('模板更新成功')
      } else {
        await createTemplate(payload)
        message.success('模板创建成功')
      }
      visible.value = false
      emit('success')
    } catch {
      // error handled by interceptor
    } finally {
      loading.value = false
    }
  })
}

defineExpose({ open })
</script>
