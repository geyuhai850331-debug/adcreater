<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑模型' : '新增模型'"
    width="620px"
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="form.modelName" placeholder="如 gpt-4o, glm-4-flash" />
      </el-form-item>
      <el-form-item label="类别" prop="category">
        <el-select v-model="form.category" placeholder="请选择类别" class="w-1/1">
          <el-option label="文案生成" value="copy" />
          <el-option label="图片生成" value="image" />
          <el-option label="视频生成" value="video" />
          <el-option label="数字人" value="digital_human" />
        </el-select>
      </el-form-item>
      <el-form-item label="适配器类" prop="adapterClass">
        <el-input
          v-model="form.adapterClass"
          placeholder="如 OpenAIAdapter 或 com.djb.module.ai.adapter.OpenAIAdapter"
        />
      </el-form-item>
      <el-form-item label="API Key" prop="apiKey">
        <el-input
          v-model="form.apiKey"
          :type="showApiKey ? 'text' : 'password'"
          placeholder="API 密钥"
          show-password
        >
          <template #prefix>
            <Icon icon="ep:key" />
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="接口地址" prop="endpointUrl">
        <el-input v-model="form.endpointUrl" placeholder="如 https://api.openai.com/v1" />
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="form.isEnabled" active-text="启用" inactive-text="禁用" />
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="form.priority" placeholder="请选择优先级" class="!w-200px">
          <el-option
            v-for="priority in priorityOptions"
            :key="priority"
            :label="String(priority)"
            :value="priority"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="额外配置" prop="extraConfig">
        <el-input
          v-model="form.extraConfig"
          type="textarea"
          :rows="4"
          placeholder='JSON 格式，如 {"temperature": 0.7, "max_tokens": 4096}'
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="testing" @click="handleTestConnection">测试连接</el-button>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createModel,
  getModelDetail,
  testModelConnection,
  updateModel,
  type AiModelVO
} from '@/api/adcreater/model'

defineOptions({ name: 'AdCreaterModelForm' })

const emit = defineEmits<{ success: [] }>()
const message = useMessage()

const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const testing = ref(false)
const showApiKey = ref(false)
const formRef = ref<FormInstance>()
const priorityOptions = [1, 2, 3, 4, 5]

const form = reactive({
  id: null as number | null,
  modelName: '',
  category: 'copy',
  adapterClass: 'OpenAIAdapter',
  apiKey: '',
  endpointUrl: '',
  isEnabled: true,
  priority: 1,
  extraConfig: ''
})

const rules: FormRules = {
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  adapterClass: [{ required: true, message: '请输入适配器类', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入 API Key', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

function fillForm(row?: Partial<AiModelVO>) {
  form.id = row?.id ?? null
  form.modelName = row?.modelName || ''
  form.category = row?.category || 'copy'
  form.adapterClass = row?.adapterClass || 'OpenAIAdapter'
  form.apiKey = row?.apiKey || ''
  form.endpointUrl = row?.endpointUrl || ''
  form.isEnabled = row?.isEnabled ?? true
  form.priority = priorityOptions.includes(row?.priority ?? 1) ? (row?.priority ?? 1) : 1
  form.extraConfig = row?.extraConfig
    ? typeof row.extraConfig === 'string'
      ? row.extraConfig
      : JSON.stringify(row.extraConfig, null, 2)
    : ''
  showApiKey.value = false
}

async function open(row?: AiModelVO) {
  formRef.value?.clearValidate()
  if (row?.id) {
    isEdit.value = true
    const detail = await getModelDetail(row.id)
    fillForm(detail)
  } else {
    isEdit.value = false
    fillForm()
  }
  visible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
      try {
        const payload = buildPayload()
        if (isEdit.value && form.id) {
          await updateModel(form.id, payload)
          message.success('模型更新成功')
        } else {
        await createModel(payload)
        message.success('模型创建成功')
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

function buildPayload() {
  const normalizedExtraConfig = form.extraConfig.trim() ? form.extraConfig.trim() : undefined
  const payload: any = {
    id: form.id ?? undefined,
    modelName: form.modelName,
    category: form.category,
    adapterClass: form.adapterClass,
    apiKey: form.apiKey.trim(),
    endpointUrl: form.endpointUrl,
    isEnabled: form.isEnabled,
    priority: form.priority,
    extraConfig: normalizedExtraConfig
  }
  return payload
}

async function handleTestConnection() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  testing.value = true
  try {
    const payload = buildPayload()
    await testModelConnection(payload)
    message.success('连接测试成功')
  } catch {
    // error handled by interceptor
  } finally {
    testing.value = false
  }
}

defineExpose({ open })
</script>
