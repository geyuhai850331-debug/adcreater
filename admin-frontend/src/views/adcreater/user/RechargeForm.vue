<template>
  <el-dialog
    v-model="visible"
    title="用户充值"
    width="420px"
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="用户 ID">
        <el-input :model-value="form.userId" disabled />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input :model-value="form.username" disabled />
      </el-form-item>
      <el-form-item label="充值数量" prop="amount">
        <el-input-number
          v-model="form.amount"
          :min="1"
          :max="999999"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确认充值</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { rechargeUser } from '@/api/adcreater/user'

defineOptions({ name: 'AdCreaterRechargeForm' })

const emit = defineEmits<{ success: [] }>()
const message = useMessage()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  userId: 0,
  username: '',
  amount: 100,
  remark: ''
})

const rules: FormRules = {
  amount: [{ required: true, message: '请输入充值数量', trigger: 'blur' }]
}

function open(row: any) {
  form.userId = row.id
  form.username = row.username || ''
  form.amount = 100
  form.remark = ''
  visible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await rechargeUser({
        userId: form.userId,
        amount: form.amount,
        remark: form.remark
      })
      message.success('充值成功')
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
