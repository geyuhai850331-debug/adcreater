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
      <el-form-item label="缩略图">
        <el-upload
          :auto-upload="false"
          :limit="1"
          :on-change="handleThumbnailChange"
          :file-list="thumbnailFileList"
          list-type="picture"
          accept="image/*"
        >
          <el-button type="primary" plain>
            <Icon icon="ep:upload" />上传缩略图
          </el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="配置数据" prop="configData">
        <el-input
          v-model="form.configData"
          type="textarea"
          :rows="6"
          placeholder='JSON 格式配置，如 {"background": "#ffffff", "elements": []}'
        />
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
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { createTemplate, updateTemplate } from '@/api/adcreater/template'

defineOptions({ name: 'AdCreaterTemplateForm' })

const emit = defineEmits<{ success: [] }>()
const message = useMessage()

const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const thumbnailFileList = ref<any[]>([])

const form = reactive({
  id: null as number | null,
  name: '',
  category: '',
  description: '',
  configData: '',
  thumbnailFile: null as File | null
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
}

function handleThumbnailChange(file: UploadFile) {
  form.thumbnailFile = file.raw || null
}

function onOpen() {
  formRef.value?.resetFields()
}

function open(row?: any) {
  thumbnailFileList.value = []
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.name = row.name || ''
    form.category = row.category || ''
    form.description = row.description || ''
    form.configData = row.configData
      ? typeof row.configData === 'string' ? row.configData : JSON.stringify(row.configData, null, 2)
      : ''
    form.thumbnailFile = null
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.category = ''
    form.description = ''
    form.configData = ''
    form.thumbnailFile = null
  }
  visible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const formData = new FormData()
      formData.append('name', form.name)
      formData.append('category', form.category)
      formData.append('description', form.description)
      formData.append('configData', form.configData)
      if (form.thumbnailFile) {
        formData.append('thumbnail', form.thumbnailFile)
      }
      if (isEdit.value && form.id) {
        await updateTemplate(form.id, formData)
        message.success('模板更新成功')
      } else {
        await createTemplate(formData)
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
