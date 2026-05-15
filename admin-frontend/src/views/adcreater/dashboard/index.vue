<template>
  <ContentWrap>
    <el-row :gutter="20">
      <el-col :span="6" :xs="12">
        <el-card shadow="hover">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-14px text-gray-400 mb-8px">今日 API 调用</div>
              <div class="text-28px font-bold">{{ stats.todayApiCalls }}</div>
            </div>
            <Icon icon="ep:connection" :size="48" color="#409EFF" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-14px text-gray-400 mb-8px">活跃用户</div>
              <div class="text-28px font-bold">{{ stats.activeUsers }}</div>
            </div>
            <Icon icon="ep:user-filled" :size="48" color="#67C23A" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-14px text-gray-400 mb-8px">今日点数消耗</div>
              <div class="text-28px font-bold">{{ stats.todayPointsConsumed }}</div>
            </div>
            <Icon icon="ep:coin" :size="48" color="#E6A23C" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-14px text-gray-400 mb-8px">总用户数</div>
              <div class="text-28px font-bold">{{ stats.totalUsers }}</div>
            </div>
            <Icon icon="ep:avatar" :size="48" color="#F56C6C" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </ContentWrap>

  <ContentWrap class="mt-16px">
    <el-row :gutter="20">
      <el-col :span="12" :xs="24">
        <el-card shadow="never">
          <template #header><span>API 调用趋势（近7天）</span></template>
          <div class="h-300px flex items-center justify-center">
            <el-empty description="图表区域（接入 ECharts 后展示）" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12" :xs="24">
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
  </ContentWrap>
</template>

<script lang="ts" setup>
import { reactive, onMounted } from 'vue'
import { getDashboardStats } from '@/api/adcreater/dashboard'

defineOptions({ name: 'AdCreaterDashboard' })

const stats = reactive({
  todayApiCalls: 0,
  activeUsers: 0,
  todayPointsConsumed: 0,
  totalUsers: 0
})

async function fetchStats() {
  try {
    const res = await getDashboardStats()
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
