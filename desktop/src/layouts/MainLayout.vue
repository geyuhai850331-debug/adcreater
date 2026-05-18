<template>
  <el-container class="main-layout">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <h2 v-if="!isCollapsed">AdCreater</h2>
        <span v-else class="logo-icon">◆</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        router
        background-color="transparent"
        text-color="var(--color-text-sidebar)"
        active-text-color="#fff"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-menu-item index="/ad/create">
          <el-icon><Edit /></el-icon>
          <template #title>广告制作</template>
        </el-menu-item>
        <el-menu-item index="/delivery">
          <el-icon><Promotion /></el-icon>
          <template #title>广告投放</template>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><Files /></el-icon>
          <template #title>模板管理</template>
        </el-menu-item>
        <el-menu-item index="/resources">
          <el-icon><FolderOpened /></el-icon>
          <template #title>资源管理</template>
        </el-menu-item>
        <el-menu-item index="/billing">
          <el-icon><Wallet /></el-icon>
          <template #title>点数充值</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span v-if="!isCollapsed" class="username">{{ username }}</span>
        </div>
        <div v-if="!isCollapsed" class="balance-info">
          <span class="balance-label">剩余点数</span>
          <span class="balance-value">{{ balance }}</span>
        </div>
        <div class="theme-toggle-row">
          <ThemeToggle />
          <span v-if="!isCollapsed" class="theme-label">{{ themeLabel }}</span>
        </div>
        <el-button
          v-if="!isCollapsed"
          type="danger"
          size="small"
          text
          class="logout-btn"
          @click="handleLogout"
        >
          退出登录
        </el-button>
        <el-button
          v-else
          type="danger"
          size="small"
          text
          class="logout-btn-icon"
          @click="handleLogout"
          title="退出登录"
        >
          <el-icon :size="16"><SwitchButton /></el-icon>
        </el-button>
      </div>

      <!-- Collapse toggle -->
      <div class="collapse-toggle" @click="toggleCollapse">
        <el-icon :size="18">
          <DArrowLeft v-if="!isCollapsed" />
          <DArrowRight v-else />
        </el-icon>
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
import { UserFilled, DArrowLeft, DArrowRight, SwitchButton } from '@element-plus/icons-vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { useThemeStore } from '@/stores/theme'
import client from '@/api/client'
import { clearAuthSession } from '@/utils/auth'

const COLLAPSE_KEY = 'adcreater-sidebar-collapsed'

const router = useRouter()
const route = useRoute()

const username = ref(localStorage.getItem('username') || '用户')
const themeStore = useThemeStore()
const balance = ref(0)
const isCollapsed = ref(localStorage.getItem(COLLAPSE_KEY) === 'true')

const activeMenu = computed(() => route.path)
const themeLabel = computed(() => themeStore.theme === 'dark' ? '深色模式' : '浅色模式')

async function fetchBalance() {
  try {
    const res = await client.get('/app-api/billing/balance') as any
    balance.value = res?.balance ?? res?.data?.balance ?? 0
  } catch {
    // balance fetch can fail silently
  }
}

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem(COLLAPSE_KEY, String(isCollapsed.value))
}

function handleLogout() {
  clearAuthSession()
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
  transition: width var(--transition-normal);
}

.sidebar.collapsed {
  width: 64px !important;
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

.logo-icon {
  display: block;
  text-align: center;
  font-family: var(--font-mono);
  font-size: 1.25rem;
  color: var(--color-primary);
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

.theme-toggle-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.theme-label {
  color: var(--color-text-sidebar);
  font-size: var(--text-xs);
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

.logout-btn-icon {
  width: 100%;
  justify-content: center !important;
  color: var(--color-text-sidebar) !important;
  font-size: var(--text-sm);
  transition: color var(--transition-fast);
}

.logout-btn-icon:hover {
  color: var(--color-error) !important;
}

/* Collapse toggle */
.collapse-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color var(--transition-fast), background var(--transition-fast);
  flex-shrink: 0;
}

.collapse-toggle:hover {
  color: var(--color-text-sidebar-active);
  background: var(--color-bg-sidebar-hover);
}

.el-main {
  background: var(--color-bg);
  padding: var(--space-6);
  overflow-y: auto;
}
</style>
