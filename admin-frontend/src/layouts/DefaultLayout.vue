<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">AdCreater</span>
        <span v-else>AD</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/billing">
          <el-icon><Wallet /></el-icon>
          <span>点数管理</span>
        </el-menu-item>
        <el-menu-item index="/ai/models">
          <el-icon><Cpu /></el-icon>
          <span>模型配置</span>
        </el-menu-item>
        <el-menu-item index="/ai/prompts">
          <el-icon><EditPen /></el-icon>
          <span>Prompt 配置</span>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><Files /></el-icon>
          <span>模板管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-button
            @click="isCollapse = !isCollapse"
            :icon="isCollapse ? Expand : Fold"
            text
          />
        </div>
        <div class="header-right">
          <span class="username">{{ username }}</span>
          <el-button type="danger" @click="logout" size="small">退出</el-button>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Expand, Fold } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const username = ref(localStorage.getItem('username') || 'Admin')

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.layout-aside { background-color: #304156; overflow-x: hidden; transition: width 0.3s; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid #4a5a6a;
  overflow: hidden;
  white-space: nowrap;
}
.layout-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}
.layout-main { background: #f0f2f5; padding: 20px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.username { color: #606266; }
</style>
