<template>
  <div class="home-page">
    <!-- Welcome -->
    <div class="welcome-section">
      <h1>欢迎回来，{{ username }}</h1>
      <p class="welcome-desc">这里是你的广告创意工作台，快速开始创作吧。</p>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <el-card
        v-for="action in quickActions"
        :key="action.key"
        class="action-card"
        shadow="hover"
        @click="navigateTo(action.path)"
      >
        <div class="action-content">
          <el-icon :size="36" :color="action.color">
            <component :is="action.icon" />
          </el-icon>
          <h3>{{ action.title }}</h3>
          <p>{{ action.desc }}</p>
        </div>
      </el-card>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="本月生成数" :value="stats.monthlyGenerated">
            <template #prefix>
              <el-icon color="#409EFF"><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="剩余点数" :value="stats.balance">
            <template #prefix>
              <el-icon color="#67C23A"><Coin /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="可用模板数" :value="stats.templateCount">
            <template #prefix>
              <el-icon color="#E6A23C"><Files /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- Recent Projects -->
    <el-card class="recent-section">
      <template #header>
        <div class="card-header">
          <span>最近项目</span>
          <el-button type="primary" size="small" @click="$router.push('/ad/create')">
            新建项目
          </el-button>
        </div>
      </template>
      <el-table :data="recentProjects" stripe style="width: 100%" v-if="recentProjects.length">
        <el-table-column prop="name" label="项目名称" min-width="180" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'image' ? 'success' : 'warning'" size="small">
              {{ row.type === 'image' ? '图片广告' : '视频广告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="$router.push('/ad/preview')">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无项目，快去创建吧" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Edit, Files, FolderOpened, TrendCharts, Coin
} from '@element-plus/icons-vue'
import client from '@/api/client'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

const quickActions = [
  { key: 'create', title: '创建广告', desc: '开始新的广告创意制作', path: '/ad/create', icon: Edit, color: '#409EFF' },
  { key: 'templates', title: '管理模板', desc: '浏览和管理广告模板', path: '/templates', icon: Files, color: '#67C23A' },
  { key: 'resources', title: '查看资源', desc: '浏览本地素材资源', path: '/resources', icon: FolderOpened, color: '#E6A23C' }
]

const stats = reactive({
  monthlyGenerated: 0,
  balance: 0,
  templateCount: 0
})

const recentProjects = ref<any[]>([])

function statusTagType(status: string) {
  const map: Record<string, string> = {
    '已完成': 'success',
    '进行中': 'warning',
    '失败': 'danger'
  }
  return map[status] || 'info'
}

async function fetchData() {
  try {
    const bal = await client.get('/billing/balance') as any
    stats.balance = bal?.balance ?? bal?.data?.balance ?? 0
  } catch { /* ignore */ }

  try {
    const tmpl = await client.get('/templates/page', { params: { page: 1, pageSize: 1 } }) as any
    const total = tmpl?.total ?? tmpl?.data?.total ?? 0
    stats.templateCount = total
  } catch { /* ignore */ }

  try {
    const proj = await client.get('/ad/projects/recent') as any
    const list = proj?.data ?? proj ?? []
    recentProjects.value = Array.isArray(list) ? list.slice(0, 5) : []
    stats.monthlyGenerated = recentProjects.value.filter(
      (p: any) => p.createdAt && new Date(p.createdAt).getMonth() === new Date().getMonth()
    ).length
  } catch { /* ignore */ }
}

function navigateTo(path: string) {
  router.push(path)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-section h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.welcome-desc {
  color: #909399;
  margin: 8px 0 0 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.action-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
}

.action-content {
  text-align: center;
  padding: 8px 0;
}

.action-content h3 {
  margin: 12px 0 4px;
  color: #303133;
}

.action-content p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.stats-row {
  margin-bottom: 24px;
}

.recent-section .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
