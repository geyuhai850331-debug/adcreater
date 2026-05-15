<template>
  <el-dialog
    v-model="visible"
    title="发布新版本"
    width="480px"
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="模板名称">
        <el-input :model-value="form.templateName" disabled />
      </el-form-item>
      <el-form-item label="版本号" prop="version">
        <el-input v-model="form.version" placeholder="如 1.0.0" />
      </el-form-item>
      <el-form-item label="URL" prop="url">
        <el-input v-model="form.url" placeholder="如 https://cdn.example.com/tpl/v1.0.0.json" />
      </el-form-item>
      <el-form-item label="更新日志" prop="changelog">
        <el-input
          v-model="form.changelog"
          type="textarea"
          :rows="4"
          placeholder="本次更新的内容"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">发布</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { publishTemplateVersion } from '@/api/adcreater/template'

defineOptions({ name: 'AdCreaterPublishForm' })

const emit = defineEmits<{ success: [] }>()
const message = useMessage()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  templateId: null as number | null,
  templateName: '',
  version: '',
  url: '',
  changelog: ''
})

const rules: FormRules = {
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  url: [{ required: true, message: '请输入 URL', trigger: 'blur' }],
  changelog: [{ required: true, message: '请输入更新日志', trigger: 'blur' }]
}

function open(row: any) {
  form.templateId = row.id
  form.templateName = row.name || ''
  form.version = ''
  form.url = ''
  form.changelog = ''
  visible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await publishTemplateVersion(form.templateId!, {
        version: form.version,
        url: form.url,
        changelog: form.changelog
      })
      message.success('版本发布成功')
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
