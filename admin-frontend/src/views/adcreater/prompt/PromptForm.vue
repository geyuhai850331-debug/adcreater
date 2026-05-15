<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑 Prompt' : '新增 Prompt'"
    width="700px"
    :close-on-click-modal="false"
    @open="onOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="如 video-generation-v1" />
      </el-form-item>
      <el-form-item label="类别" prop="category">
        <el-select v-model="form.category" placeholder="请选择类别" class="w-1/1">
          <el-option label="文案生成" value="copy" />
          <el-option label="图片生成" value="image" />
          <el-option label="视频生成" value="video" />
          <el-option label="数字人" value="digital_human" />
        </el-select>
      </el-form-item>
      <el-form-item label="模型" prop="modelId">
        <el-select v-model="form.modelId" placeholder="请选择模型" class="w-1/1">
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
          v-model="form.templateContent"
          type="textarea"
          :rows="12"
          placeholder="输入 Prompt 模板，变量用 {{variable}} 表示"
        />
      </el-form-item>
      <el-form-item label="变量提示">
        <el-tag
          v-for="v in detectedVariables"
          :key="v"
          class="mr-8px mb-4px"
          type="info"
        >
          {{ wrapVariable(v) }}
        </el-tag>
        <span v-if="detectedVariables.length === 0" class="text-12px text-gray-400">
          模板中使用 &#123;&#123;变量名&#125;&#125; 定义变量
        </span>
      </el-form-item>
      <el-form-item label="系统提示">
        <el-input
          v-model="form.systemPrompt"
          type="textarea"
          :rows="4"
          placeholder="可选，系统级上下文提示"
        />
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="form.isEnabled" active-text="启用" inactive-text="禁用" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createPrompt, updatePrompt } from '@/api/adcreater/prompt'
import { getModelList } from '@/api/adcreater/model'

defineOptions({ name: 'AdCreaterPromptForm' })

const emit = defineEmits<{ success: [] }>()
const message = useMessage()

const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const availableModels = ref<any[]>([])

const form = reactive({
  id: null as number | null,
  name: '',
  category: '',
  modelId: null as number | null,
  templateContent: '',
  systemPrompt: '',
  isEnabled: true
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  modelId: [{ required: true, message: '请选择模型', trigger: 'change' }],
  templateContent: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

const detectedVariables = computed(() => {
  const matches = form.templateContent.match(/\{\{(\w+)\}\}/g)
  if (!matches) return []
  return [...new Set(matches.map((m) => m.slice(2, -2)))]
})

function wrapVariable(name: string): string {
  return `{{${name}}}`
}

async function fetchModels() {
  try {
    const res = await getModelList()
    if (res) {
      availableModels.value = (res as any).list || (res as any).records || res || []
    }
  } catch {
    // silently fail
  }
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
    form.modelId = row.modelId || null
    form.templateContent = row.templateContent || ''
    form.systemPrompt = row.systemPrompt || ''
    form.isEnabled = row.isEnabled ?? true
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.category = ''
    form.modelId = null
    form.templateContent = ''
    form.systemPrompt = ''
    form.isEnabled = true
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
        modelId: form.modelId,
        templateContent: form.templateContent,
        systemPrompt: form.systemPrompt,
        isEnabled: form.isEnabled
      }
      if (isEdit.value && form.id) {
        await updatePrompt(form.id, payload)
        message.success('Prompt 更新成功')
      } else {
        await createPrompt(payload)
        message.success('Prompt 创建成功')
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

onMounted(() => {
  fetchModels()
})

defineExpose({ open })
</script>
