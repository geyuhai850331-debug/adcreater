<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <div class="stat-label">今日 API 调用</div>
              <div class="stat-value">{{ stats.todayApiCalls }}</div>
            </div>
            <el-icon class="stat-icon" color="#409EFF" :size="48"><Connection /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <div class="stat-label">活跃用户</div>
              <div class="stat-value">{{ stats.activeUsers }}</div>
            </div>
            <el-icon class="stat-icon" color="#67C23A" :size="48"><UserFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <div class="stat-label">今日点数消耗</div>
              <div class="stat-value">{{ stats.todayPointsConsumed }}</div>
            </div>
            <el-icon class="stat-icon" color="#E6A23C" :size="48"><Coin /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <div class="stat-label">总用户数</div>
              <div class="stat-value">{{ stats.totalUsers }}</div>
            </div>
            <el-icon class="stat-icon" color="#F56C6C" :size="48"><Avatar /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>API 调用趋势（近7天）</span></template>
          <div class="chart-placeholder">
            <el-empty description="图表区域（接入 ECharts 后展示）" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>系统状态</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="服务版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="运行状态">
              <el-tag type="success">正常运行</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="已注册用户">{{ stats.totalUsers }}</el-descriptions-item>
            <el-descriptions-item label="今日调用量">{{ stats.todayApiCalls }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import client from '@/api/client'

const stats = reactive({
  todayApiCalls: 0,
  activeUsers: 0,
  todayPointsConsumed: 0,
  totalUsers: 0
})

async function fetchStats() {
  try {
    const res: any = await client.get('/system/dashboard/stats')
    if (res) {
      stats.todayApiCalls = res.todayApiCalls ?? 0
      stats.activeUsers = res.activeUsers ?? 0
      stats.todayPointsConsumed = res.todayPointsConsumed ?? 0
      stats.totalUsers = res.totalUsers ?? 0
    }
  } catch {
    // Show zeroes when backend is unavailable
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard { padding: 0; }
.stat-row { margin-bottom: 20px; }
.stat-card { cursor: default; }
.stat-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stat-label { color: #909399; font-size: 14px; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.chart-placeholder { height: 300px; display: flex; align-items: center; justify-content: center; }
</style>
