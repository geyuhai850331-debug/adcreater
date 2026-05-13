<template>
  <el-container class="main-layout">
    <!-- Sidebar -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h2>AdCreater</h2>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="var(--color-text-sidebar)"
        active-text-color="#fff"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/ad/create">
          <el-icon><Edit /></el-icon>
          <span>广告制作</span>
        </el-menu-item>
        <el-menu-item index="/delivery">
          <el-icon><Promotion /></el-icon>
          <span>广告投放</span>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><Files /></el-icon>
          <span>模板管理</span>
        </el-menu-item>
        <el-menu-item index="/resources">
          <el-icon><FolderOpened /></el-icon>
          <span>资源管理</span>
        </el-menu-item>
        <el-menu-item index="/billing">
          <el-icon><Wallet /></el-icon>
          <span>点数充值</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="username">{{ username }}</span>
        </div>
        <div class="balance-info">
          <span class="balance-label">剩余点数</span>
          <span class="balance-value">{{ balance }}</span>
        </div>
        <el-button
          type="danger"
          size="small"
          text
          class="logout-btn"
          @click="handleLogout"
        >
          退出登录
        </el-button>
      </div>
    </el-aside>

    <!-- Main Content -->
    <el-container>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import client from '@/api/client'

const router = useRouter()
const route = useRoute()

const username = ref(localStorage.getItem('username') || '用户')
const balance = ref(0)

const activeMenu = computed(() => route.path)

async function fetchBalance() {
  try {
    const res = await client.get('/billing/balance') as any
    balance.value = res?.balance ?? res?.data?.balance ?? 0
  } catch {
    // balance fetch can fail silently
  }
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}

onMounted(() => {
  fetchBalance()
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background: var(--color-bg-sidebar);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 220px !important;
  transition: width var(--transition-normal);
}

.logo {
  padding: var(--space-5) var(--space-5) var(--space-4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo h2 {
  font-family: var(--font-heading);
  color: #fff;
  margin: 0;
  font-size: 1.125rem;
  font-weight: 700;
  text-align: center;
  letter-spacing: -0.01em;
}

.el-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  background: transparent;
  padding: var(--space-2) 0;
}

.el-menu :deep(.el-menu-item) {
  margin: 1px var(--space-2);
  border-radius: var(--radius-md);
  height: 40px;
  line-height: 40px;
  font-size: var(--text-base);
  color: var(--color-text-sidebar);
  transition: all var(--transition-fast);
}

.el-menu :deep(.el-menu-item:hover) {
  background: var(--color-bg-sidebar-hover);
  color: var(--color-text-sidebar-active);
}

.el-menu :deep(.el-menu-item.is-active) {
  background: var(--color-primary);
  color: #fff;
  font-weight: 500;
}

.el-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

.sidebar-footer {
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-text-sidebar);
}

.user-info :deep(.el-avatar) {
  background: var(--color-primary);
  flex-shrink: 0;
}

.username {
  font-size: var(--text-sm);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.balance-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) var(--space-3);
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-md);
}

.balance-label {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.balance-value {
  color: var(--color-success);
  font-weight: 700;
  font-size: var(--text-lg);
  font-family: var(--font-mono);
}

.logout-btn {
  width: 100%;
  justify-content: flex-start;
  color: var(--color-text-sidebar) !important;
  font-size: var(--text-sm);
  transition: color var(--transition-fast);
}

.logout-btn:hover {
  color: var(--color-error) !important;
}

.el-main {
  background: var(--color-bg);
  padding: var(--space-6);
  overflow-y: auto;
}
</style>
