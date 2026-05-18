<template>
  <div class="login-page">
    <div class="login-hero">
      <div class="login-theme-toggle">
        <ThemeToggle />
      </div>

      <div class="hero-content">
        <div class="hero-demo">
          <div class="demo-inner">
            <el-icon :size="36"><VideoPlay /></el-icon>
            <span>产品 demo 视频循环播放</span>
          </div>
        </div>
        <p class="hero-text">
          AI 驱动的电商创作平台<br>
          文案 · 图片 · 视频 · 一键发布
        </p>
      </div>
    </div>

    <!-- Form panel -->
    <div class="login-form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <div class="brand-mono">◆</div>
          <h1 class="form-logo">AdCreater</h1>
          <p class="form-subtitle">
            {{ isRegisterMode ? '会员注册后即可进入创意工作台' : '会员手机号登录，直达创意工作台' }}
          </p>
        </div>

        <el-form
          ref="authFormRef"
          :model="authForm"
          :rules="authRules"
          label-position="top"
          @submit.prevent="handleSubmit"
        >
          <el-form-item label="手机号" prop="mobile">
            <el-input
              v-model="authForm.mobile"
              placeholder="输入手机号"
              :prefix-icon="Iphone"
              size="large"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="authForm.password"
              type="password"
              placeholder="输入 6-16 位密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
          <el-form-item v-if="isRegisterMode" label="确认密码" prop="confirmPassword">
            <el-input
              v-model="authForm.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              :prefix-icon="Key"
              size="large"
              show-password
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleSubmit"
            >
              {{ loading ? (isRegisterMode ? '注册中...' : '登录中...') : (isRegisterMode ? '注 册' : '登 录') }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="divider-row">
          <span class="divider-line"></span>
          <span class="divider-text">会员账号</span>
          <span class="divider-line"></span>
        </div>

        <p class="alt-link" @click="toggleAuthMode">
          {{ isRegisterMode ? '已有账号，去登录 →' : '没有账号，立即注册 →' }}
        </p>

        <p class="register-link">当前仅支持手机号 + 密码，不使用短信验证码</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Key, Iphone, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import ThemeToggle from '@/components/ThemeToggle.vue'
import client from '@/api/client'
import { getApiResultMessage, getAuthPayload, getErrorMessage, saveAuthSession } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const isRegisterMode = ref(false)
const authFormRef = ref<FormInstance>()
const authForm = reactive({
  mobile: '',
  password: '',
  confirmPassword: ''
})

const authRules = computed<FormRules>(() => ({
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度为 6-16 位', trigger: 'blur' }
  ],
  confirmPassword: isRegisterMode.value
    ? [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
          validator: (_rule, value, callback) => {
            if (!value) {
              callback(new Error('请再次输入密码'))
              return
            }
            if (value !== authForm.password) {
              callback(new Error('两次输入的密码不一致'))
              return
            }
            callback()
          },
          trigger: 'blur'
        }
      ]
    : []
}))

function toggleAuthMode() {
  isRegisterMode.value = !isRegisterMode.value
  authForm.confirmPassword = ''
  authFormRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await authFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const endpoint = isRegisterMode.value ? '/app-api/member/auth/register' : '/app-api/member/auth/login'
    const res = await client.post(endpoint, {
      mobile: authForm.mobile,
      password: authForm.password
    }) as any

    const payload = getAuthPayload(res)
    if (!payload) {
      ElMessage.error(getApiResultMessage(res, isRegisterMode.value ? '注册响应格式错误' : '登录响应格式错误'))
      return
    }

    saveAuthSession(payload, authForm.mobile)
    ElMessage.success(isRegisterMode.value ? '注册成功' : '登录成功')
    router.push('/home')
  } catch (err: any) {
    ElMessage.error(getErrorMessage(err, isRegisterMode.value ? '注册失败' : '登录失败'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  background: var(--color-bg);
}

.login-hero {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-theme-toggle {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
}

.hero-content {
  text-align: center;
}

.hero-demo {
  margin-bottom: var(--space-6);
}

.demo-inner {
  width: 280px;
  height: 180px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-xl);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  transition: border-color var(--transition-fast);
}

.demo-inner:hover {
  border-color: var(--color-primary);
}

.hero-text {
  color: rgba(255, 255, 255, 0.55);
  font-size: var(--text-base);
  line-height: 1.8;
  margin: 0;
}

.login-form-panel {
  width: 460px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
  background: var(--color-bg);
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: var(--space-8);
}

.brand-mono {
  font-family: var(--font-mono);
  font-size: 1.5rem;
  color: var(--color-primary);
  margin-bottom: var(--space-3);
}

.form-logo {
  font-family: var(--font-heading);
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 var(--space-2) 0;
  letter-spacing: -0.02em;
}

.form-subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: var(--text-lg);
  font-weight: 600;
  letter-spacing: 0.1em;
}

.divider-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: var(--space-5) 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.divider-text {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.alt-link {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0 0 var(--space-2) 0;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.alt-link:hover {
  color: var(--color-primary);
}

.register-link {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0;
}
</style>
