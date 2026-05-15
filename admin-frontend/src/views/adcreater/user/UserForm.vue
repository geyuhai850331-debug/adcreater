<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="520px"
    :close-on-click-modal="false"
    @open="onOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
        <el-input
          v-model="form.password"
          type="password"
          :placeholder="isEdit ? '留空则不修改' : '请输入密码'"
          show-password
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="form.mobile" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch
          v-model="form.status"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="禁用"
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
import type { FormInstance, FormRules } from 'element-plus'
import { createUser, updateUser } from '@/api/adcreater/user'

defineOptions({ name: 'AdCreaterUserForm' })

const emit = defineEmits<{ success: [] }>()
const message = useMessage()

const visible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: null as number | null,
  username: '',
  nickname: '',
  password: '',
  mobile: '',
  email: '',
  status: 1
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  mobile: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

function onOpen() {
  formRef.value?.resetFields()
}

function open(row?: any) {
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.username = row.username || ''
    form.nickname = row.nickname || ''
    form.password = ''
    form.mobile = row.mobile || ''
    form.email = row.email || ''
    form.status = row.status
  } else {
    isEdit.value = false
    form.id = null
    form.username = ''
    form.nickname = ''
    form.password = ''
    form.mobile = ''
    form.email = ''
    form.status = 1
  }
  visible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const payload: any = {
        username: form.username,
        nickname: form.nickname,
        mobile: form.mobile,
        email: form.email,
        status: form.status
      }
      if (form.password) payload.password = form.password

      if (isEdit.value && form.id) {
        await updateUser(form.id, payload)
        message.success('用户更新成功')
      } else {
        await createUser(payload)
        message.success('用户创建成功')
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
