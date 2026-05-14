<template>
  <div class="login-page">
    <!-- Brand / Hero panel -->
    <div class="login-hero">
      <!-- Theme toggle - top right -->
      <div class="login-theme-toggle">
        <ThemeToggle />
      </div>

      <div class="hero-content">
        <!-- Demo placeholder -->
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
          <p class="form-subtitle">跨境电商广告创意制作平台</p>
        </div>

        <!-- Password login form -->
        <el-form
          v-if="loginMode === 'password'"
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-position="top"
          @submit.prevent="handlePasswordLogin"
        >
          <el-form-item label="手机号 / 邮箱" prop="account">
            <el-input
              v-model="passwordForm.account"
              placeholder="输入手机号或邮箱"
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="passwordForm.password"
              type="password"
              placeholder="输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handlePasswordLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handlePasswordLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- SMS login form -->
        <el-form
          v-else
          ref="smsFormRef"
          :model="smsForm"
          :rules="smsRules"
          label-position="top"
          @submit.prevent="handleSmsLogin"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="smsForm.phone"
              placeholder="输入手机号"
              :prefix-icon="Iphone"
              size="large"
            />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <div class="sms-code-row">
              <el-input
                v-model="smsForm.code"
                placeholder="输入验证码"
                :prefix-icon="Key"
                size="large"
                class="sms-code-input"
                @keyup.enter="handleSmsLogin"
              />
              <el-button
                type="primary"
                size="large"
                :disabled="cooldown > 0"
                class="sms-send-btn"
                @click="sendSmsCode"
              >
                {{ cooldown > 0 ? `${cooldown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleSmsLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- "or" divider -->
        <div class="divider-row">
          <span class="divider-line"></span>
          <span class="divider-text">或</span>
          <span class="divider-line"></span>
        </div>

        <!-- Alternative login method -->
        <p class="alt-link" @click="toggleLoginMode">
          {{ loginMode === 'password' ? '短信验证码登录 →' : '密码登录 →' }}
        </p>

        <!-- Register link -->
        <p class="register-link">没有账号？联系管理员开通</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Lock, Key, Iphone, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import ThemeToggle from '@/components/ThemeToggle.vue'
import client from '@/api/client'

const router = useRouter()
const loading = ref(false)
const loginMode = ref<'password' | 'sms'>('password')
const cooldown = ref(0)

// Password form
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  account: '',
  password: ''
})

const passwordRules: FormRules = {
  account: [{ required: true, message: '请输入手机号或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// SMS form
const smsFormRef = ref<FormInstance>()
const smsForm = reactive({
  phone: '',
  code: ''
})

const smsRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

function toggleLoginMode() {
  loginMode.value = loginMode.value === 'password' ? 'sms' : 'password'
}

function startCooldown() {
  cooldown.value = 60
  const timer = setInterval(() => {
    cooldown.value--
    if (cooldown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

async function handlePasswordLogin() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await client.post('/system/auth/login', {
      username: passwordForm.account,
      password: passwordForm.password
    }) as any

    const token = res?.access_token || res?.data?.access_token || res?.token
    if (!token) {
      ElMessage.error('登录响应格式错误')
      return
    }

    localStorage.setItem('token', token)
    localStorage.setItem('username', passwordForm.account)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

async function sendSmsCode() {
  const phoneValid = smsForm.phone && /^1[3-9]\d{9}$/.test(smsForm.phone)
  if (!phoneValid) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    await client.post('/system/auth/sms/send', { phone: smsForm.phone })
    ElMessage.success('验证码已发送')
    startCooldown()
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '发送失败'
    ElMessage.error(msg)
  }
}

async function handleSmsLogin() {
  const valid = await smsFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await client.post('/system/auth/sms/login', {
      phone: smsForm.phone,
      code: smsForm.code
    }) as any

    const token = res?.access_token || res?.data?.access_token || res?.token
    if (!token) {
      ElMessage.error('登录响应格式错误')
      return
    }

    localStorage.setItem('token', token)
    localStorage.setItem('username', smsForm.phone)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '登录失败'
    ElMessage.error(msg)
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

/* ── Hero panel ─────────────────────────────────── */
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

/* ── Form panel ─────────────────────────────────── */
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

/* ── Login button ───────────────────────────────── */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: var(--text-lg);
  font-weight: 600;
  letter-spacing: 0.1em;
}

/* ── SMS code row ───────────────────────────────── */
.sms-code-row {
  display: flex;
  gap: var(--space-2);
}

.sms-code-input {
  flex: 1;
}

.sms-send-btn {
  flex-shrink: 0;
  min-width: 110px;
  font-size: var(--text-sm);
}

/* ── Divider ────────────────────────────────────── */
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

/* ── Alternative links ──────────────────────────── */
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
