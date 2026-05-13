<template>
  <div class="login-page">
    <!-- Brand panel -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-mono">◆</div>
        <h1>AdCreater</h1>
        <p class="brand-desc">跨境电商广告创意制作平台</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>多语言文案本地化</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>AI 图片/视频生成</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>多平台一键适配导出</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Form panel -->
    <div class="login-form-panel">
      <div class="form-wrapper">
        <h2 class="form-title">登录</h2>
        <p class="form-subtitle">使用管理员分配的账号登录</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="login-hint">Demo: 用户名任意，密码 <code>admin</code></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import client from '@/api/client'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await client.post('/system/auth/login', {
      username: form.username,
      password: form.password
    }) as any

    const token = res?.access_token || res?.data?.access_token || res?.token
    if (!token) {
      ElMessage.error('登录响应格式错误')
      return
    }

    localStorage.setItem('token', token)
    localStorage.setItem('username', form.username)
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

.login-brand {
  flex: 1;
  background: var(--color-bg-sidebar);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
}

.brand-content {
  max-width: 360px;
  color: var(--color-text-sidebar-active);
}

.brand-mono {
  font-family: var(--font-mono);
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: var(--space-6);
}

.brand-content h1 {
  font-family: var(--font-heading);
  font-size: 2.25rem;
  font-weight: 700;
  margin: 0 0 var(--space-4) 0;
  letter-spacing: -0.02em;
}

.brand-desc {
  font-size: var(--text-lg);
  color: var(--color-text-sidebar);
  margin: 0 0 var(--space-8) 0;
  line-height: var(--leading-relaxed);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-base);
  color: var(--color-text-sidebar);
}

.feature-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  flex-shrink: 0;
}

.login-form-panel {
  width: 460px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-title {
  font-family: var(--font-heading);
  font-size: var(--text-3xl);
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 var(--space-2) 0;
  letter-spacing: -0.02em;
}

.form-subtitle {
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  margin: 0 0 var(--space-8) 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: var(--text-lg);
  font-weight: 600;
  letter-spacing: 0.1em;
  transition: background var(--transition-fast);
}

.login-hint {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: var(--space-6);
}

.login-hint code {
  font-family: var(--font-mono);
  background: var(--color-primary-light);
  color: var(--color-primary);
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
}
</style>
