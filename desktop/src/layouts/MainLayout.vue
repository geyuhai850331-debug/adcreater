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
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
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
  background-color: #304156;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  color: #fff;
  margin: 0;
  font-size: 20px;
  text-align: center;
}

.el-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #bfcbd9;
}

.username {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.balance-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-label {
  color: #999;
  font-size: 12px;
}

.balance-value {
  color: #67c23a;
  font-weight: bold;
  font-size: 16px;
}

.logout-btn {
  width: 100%;
  color: #f56c6c !important;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
