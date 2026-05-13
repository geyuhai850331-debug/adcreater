<template>
  <div class="home-page">
    <!-- Welcome -->
    <div class="welcome-section">
      <h1>{{ username }} 的工作台</h1>
      <p class="welcome-desc">广告创意制作 · 模板管理 · 素材预览</p>
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
              <el-icon color="var(--color-primary)"><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="剩余点数" :value="stats.balance">
            <template #prefix>
              <el-icon color="var(--color-cta)"><Coin /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="可用模板数" :value="stats.templateCount">
            <template #prefix>
              <el-icon color="var(--color-warning)"><Files /></el-icon>
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

      <div v-if="recentProjects.length" class="project-grid">
        <el-card
          v-for="project in recentProjects"
          :key="project.name"
          class="project-card"
          shadow="hover"
        >
          <div class="project-thumb" @click="$router.push('/ad/preview')">
            <div class="thumb-placeholder">
              <el-icon :size="40" color="var(--color-text-muted)">
                <PictureFilled v-if="project.type === 'image'" />
                <VideoCameraFilled v-else />
              </el-icon>
            </div>
          </div>
          <div class="project-info">
            <h4 class="project-name">{{ project.name }}</h4>
            <div class="project-meta">
              <el-tag :type="project.type === 'image' ? 'success' : 'warning'" size="small">
                {{ project.type === 'image' ? '图片广告' : '视频广告' }}
              </el-tag>
              <el-tag :type="statusTagType(project.status)" size="small" effect="plain">
                {{ project.status }}
              </el-tag>
            </div>
            <span class="project-date">{{ formatTime(project.createdAt) }}</span>
          </div>
          <div class="project-actions">
            <el-button type="primary" size="small" @click="$router.push('/ad/preview')">
              查看
            </el-button>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无项目，快去创建吧" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Edit, Files, FolderOpened, TrendCharts, Coin,
  PictureFilled, VideoCameraFilled
} from '@element-plus/icons-vue'
import client from '@/api/client'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

const quickActions = [
  { key: 'create', title: '新建广告', desc: '文案 → 图片 → 视频，三步完成', path: '/ad/create', icon: Edit, color: 'var(--color-primary)' },
  { key: 'templates', title: '模板浏览', desc: '管理端同步模板，一键套用', path: '/templates', icon: Files, color: 'var(--color-secondary)' },
  { key: 'resources', title: '本地素材', desc: '已生成图片/视频统一管理', path: '/resources', icon: FolderOpened, color: 'var(--color-cta)' }
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

function formatTime(iso: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
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
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: var(--space-6);
}

.welcome-section h1 {
  font-family: var(--font-heading);
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.welcome-desc {
  color: var(--color-text-secondary);
  margin: var(--space-1) 0 0 0;
  font-size: var(--text-base);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.action-card {
  cursor: pointer;
  transition: border-color var(--transition-normal), transform var(--transition-normal), box-shadow var(--transition-normal);
  border-top: 3px solid transparent;
}

.action-card:hover {
  border-top-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.action-content {
  padding: var(--space-2) 0;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.action-content .el-icon {
  margin-bottom: var(--space-1);
}

.action-content h3 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text);
}

.action-content p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
}

.stats-row {
  margin-bottom: var(--space-6);
}

.stats-row :deep(.el-card) {
  border-top: 3px solid transparent;
}

.stats-row :deep(.el-card:nth-child(1)) { border-top-color: var(--color-primary); }
.stats-row :deep(.el-card:nth-child(2)) { border-top-color: var(--color-cta); }
.stats-row :deep(.el-card:nth-child(3)) { border-top-color: var(--color-warning); }

.stats-row :deep(.el-statistic__head) {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stats-row :deep(.el-statistic__number) {
  font-family: var(--font-mono);
  font-size: 1.75rem;
  font-weight: 700;
}

.recent-section {
  border-top: 1px solid var(--color-border);
}

.recent-section .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-4);
}

.project-card {
  transition: border-color var(--transition-normal), transform var(--transition-normal), box-shadow var(--transition-normal);
  border-top: 3px solid transparent;
}

.project-card:hover {
  border-top-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.project-thumb {
  cursor: pointer;
  border-radius: var(--radius-md);
  overflow: hidden;
  height: 140px;
  margin-bottom: var(--space-3);
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.project-info {
  margin-bottom: var(--space-3);
}

.project-name {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-2) 0;
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.project-date {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.project-actions {
  display: flex;
  gap: var(--space-2);
}
</style>
