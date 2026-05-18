<template>
  <div class="delivery-page">
    <h2 class="page-title">广告投放</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab 1: 数字人 -->
      <el-tab-pane label="数字人" name="avatar">
        <div class="tab-content">
          <!-- Avatar Selection -->
          <div class="section">
            <h4>选择数字人</h4>
            <div class="avatar-grid">
              <div
                v-for="avatar in avatars"
                :key="avatar.id"
                class="avatar-item"
                :class="{ active: selectedAvatar === avatar.id }"
                @click="selectedAvatar = avatar.id"
              >
                <div class="avatar-img" :style="{ backgroundColor: avatar.color }">
                  <el-icon :size="40" color="#fff"><UserFilled /></el-icon>
                </div>
                <span class="avatar-name">{{ avatar.name }}</span>
                <span class="avatar-lang">{{ avatar.lang }}</span>
              </div>
            </div>
          </div>

          <!-- Script -->
          <div class="section">
            <h4>播报文案</h4>
            <el-input
              v-model="script"
              type="textarea"
              :rows="4"
              placeholder="请输入数字人播报文案..."
              maxlength="500"
              show-word-limit
            />
          </div>

          <!-- Voice -->
          <div class="section">
            <h4>语音选择</h4>
            <el-select v-model="selectedVoice" placeholder="选择语音" style="width: 260px">
              <el-option
                v-for="voice in voices"
                :key="voice.key"
                :label="voice.label"
                :value="voice.key"
              />
            </el-select>
          </div>

          <!-- Duration -->
          <div class="section">
            <h4>视频时长：{{ duration }} 秒</h4>
            <el-slider
              v-model="duration"
              :min="10"
              :max="120"
              :step="10"
              show-stops
              show-input
              style="max-width: 400px"
            />
          </div>

          <!-- Generate Button -->
          <div class="section">
            <el-button
              type="primary"
              size="large"
              :loading="generatingAvatar"
              @click="handleGenerateAvatar"
            >
              {{ generatingAvatar ? '生成中...' : '生成数字人视频' }}
            </el-button>
          </div>

          <!-- Progress -->
          <div v-if="generatingAvatar" class="progress-section">
            <el-progress :percentage="avatarProgress" :status="avatarStatus" />
            <p class="progress-text">{{ avatarProgressText }}</p>
          </div>

          <!-- Result -->
          <div v-if="avatarVideo" class="result-section">
            <h4>生成结果</h4>
            <video :src="avatarVideo.url" controls class="result-video" />
            <el-button
              type="primary"
              size="small"
              class="save-btn"
              @click="saveAvatarVideo"
              :loading="savingAvatar"
            >
              保存到本地
            </el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 平台投放 -->
      <el-tab-pane label="平台投放" name="platform">
        <div class="tab-content">
          <!-- Platforms -->
          <div class="section">
            <h4>选择投放平台</h4>
            <el-checkbox-group v-model="selectedPlatforms">
              <el-checkbox
                v-for="p in platforms"
                :key="p.key"
                :label="p.key"
                :value="p.key"
                class="platform-checkbox"
              >
                <span class="platform-label">{{ p.name }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <!-- Base Image Upload -->
          <div class="section">
            <h4>基础素材图片</h4>
            <el-upload
              drag
              :auto-upload="false"
              :on-change="handleImageChange"
              :limit="1"
              accept="image/*"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽图片到此或<em>点击上传</em></div>
            </el-upload>
            <div v-if="baseImageUrl" class="upload-preview">
              <el-image :src="baseImageUrl" fit="contain" style="width:200px;height:200px" />
            </div>
          </div>

          <!-- Export Button -->
          <div class="section">
            <el-button type="primary" size="large" @click="handleExport" :loading="exporting">
              导出各平台尺寸
            </el-button>
          </div>

          <!-- Platform Size Grid -->
          <div v-if="exportedSizes.length" class="section">
            <h4>导出尺寸预览</h4>
            <div class="size-grid">
              <div v-for="item in exportedSizes" :key="item.platform" class="size-card">
                <div class="size-card-header">{{ item.platform }}</div>
                <div class="size-card-body">{{ item.width }} x {{ item.height }}</div>
                <el-button size="small" type="primary" link @click="downloadSize(item)">
                  下载
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UserFilled, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const activeTab = ref('avatar')

// ── Avatar Tab ──────────────────────────────────────────────────
const selectedAvatar = ref('a1')
const script = ref('')
const selectedVoice = ref('zh_female_1')
const duration = ref(30)
const generatingAvatar = ref(false)
const avatarProgress = ref(0)
const avatarStatus = ref<'success' | 'exception' | 'warning' | ''>('')
const avatarProgressText = ref('')
const avatarVideo = ref<any>(null)
const savingAvatar = ref(false)

const avatars = [
  { id: 'a1', name: '晓月', color: 'var(--color-primary)', lang: '中文女声' },
  { id: 'a2', name: '浩然', color: 'var(--color-success)', lang: '中文男声' },
  { id: 'a3', name: 'Emma', color: 'var(--color-warning)', lang: 'English Female' },
  { id: 'a4', name: 'David', color: 'var(--color-error)', lang: 'English Male' },
  { id: 'a5', name: '凛子', color: 'var(--color-text-muted)', lang: '日本語女性' },
  { id: 'a6', name: 'Carlos', color: 'var(--color-text)', lang: 'Español Male' }
]

const voices = [
  { key: 'zh_female_1', label: '中文女声 - 温柔' },
  { key: 'zh_female_2', label: '中文女声 - 活泼' },
  { key: 'zh_male_1', label: '中文男声 - 沉稳' },
  { key: 'zh_male_2', label: '中文男声 - 磁性' },
  { key: 'en_female_1', label: 'English Female - Natural' },
  { key: 'en_male_1', label: 'English Male - Professional' },
  { key: 'ja_female_1', label: '日本語女性' },
  { key: 'es_male_1', label: 'Español Male' }
]

function simulateAvatarProgress() {
  avatarProgress.value = 0
  avatarStatus.value = ''
  avatarProgressText.value = '正在初始化数字人...'

  const steps = [
    { p: 20, text: '正在加载数字人模型...', delay: 600 },
    { p: 40, text: '正在合成语音...', delay: 800 },
    { p: 60, text: '正在驱动口型动画...', delay: 1000 },
    { p: 80, text: '正在渲染视频...', delay: 1200 },
    { p: 95, text: '正在编码输出...', delay: 800 },
    { p: 100, text: '数字人视频生成完成！', delay: 500 }
  ]

  let i = 0
  const tick = () => {
    if (i >= steps.length) return
    const s = steps[i]
    avatarProgress.value = s.p
    avatarProgressText.value = s.text
    if (s.p === 100) avatarStatus.value = 'success'
    i++
    if (i < steps.length) setTimeout(tick, s.delay)
  }
  tick()
}

async function handleGenerateAvatar() {
  if (!script.value.trim()) {
    ElMessage.warning('请输入播报文案')
    return
  }
  generatingAvatar.value = true
  simulateAvatarProgress()

  try {
    const res = await client.post('/app-api/delivery/avatar/generate', {
      avatarId: selectedAvatar.value,
      script: script.value,
      voiceId: selectedVoice.value,
      duration: duration.value
    }) as any
    const data = res?.data ?? res
    avatarVideo.value = {
      url: data?.url || data?.videoUrl,
      label: '数字人视频'
    }
    avatarProgress.value = 100
    avatarStatus.value = 'success'
    avatarProgressText.value = '数字人视频生成完成！'
    ElMessage.success('数字人视频生成成功')
  } catch (err: any) {
    avatarStatus.value = 'exception'
    avatarProgressText.value = '生成失败'
    ElMessage.error('生成失败: ' + (err?.message || '未知错误'))
  } finally {
    generatingAvatar.value = false
  }
}

async function saveAvatarVideo() {
  if (!avatarVideo.value?.url) return
  savingAvatar.value = true
  try {
    if (window.electronAPI) {
      const path = await window.electronAPI.saveFile({
        subDir: 'videos',
        filename: `avatar-${Date.now()}.mp4`,
        dataUrl: avatarVideo.value.url
      })
      ElMessage.success(`已保存到 ${path}`)
    }
  } catch (err: any) {
    ElMessage.error('保存失败: ' + (err.message || '未知错误'))
  } finally {
    savingAvatar.value = false
  }
}

// ── Platform Tab ────────────────────────────────────────────────
const platforms = [
  { key: 'amazon', name: 'Amazon' },
  { key: 'facebook', name: 'Facebook' },
  { key: 'shopee', name: 'Shopee' },
  { key: 'tiktok', name: 'TikTok' },
  { key: 'google', name: 'Google' }
]

const selectedPlatforms = ref<string[]>([])
const baseImageUrl = ref('')
const exporting = ref(false)
const exportedSizes = ref<Array<{ platform: string; width: number; height: number; url?: string }>>([])

const platformSizes: Record<string, Array<{ w: number; h: number }>> = {
  amazon: [{ w: 1500, h: 1500 }, { w: 2000, h: 2000 }, { w: 1600, h: 1600 }],
  facebook: [{ w: 1200, h: 628 }, { w: 1080, h: 1080 }, { w: 1080, h: 1920 }],
  shopee: [{ w: 1024, h: 1024 }, { w: 800, h: 800 }],
  tiktok: [{ w: 1080, h: 1920 }, { w: 720, h: 1280 }],
  google: [{ w: 1200, h: 628 }, { w: 300, h: 250 }, { w: 728, h: 90 }, { w: 160, h: 600 }, { w: 300, h: 600 }]
}

function handleImageChange(file: any) {
  const reader = new FileReader()
  reader.onload = (e) => {
    baseImageUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file.raw)
}

async function handleExport() {
  if (selectedPlatforms.value.length === 0) {
    ElMessage.warning('请至少选择一个投放平台')
    return
  }
  if (!baseImageUrl.value) {
    ElMessage.warning('请先上传基础素材图片')
    return
  }

  exporting.value = true
  try {
    const res = await client.post('/app-api/delivery/export-sizes', {
      platforms: selectedPlatforms.value,
      baseImageUrl: baseImageUrl.value
    }) as any

    const data = res?.data ?? res
    const allSizes: any[] = []
    for (const platform of selectedPlatforms.value) {
      const sizes = platformSizes[platform] || []
      for (const s of sizes) {
        allSizes.push({
          platform: platform.toUpperCase(),
          width: s.w,
          height: s.h,
          url: data?.urls?.[platform]?.[`${s.w}x${s.h}`] || baseImageUrl.value
        })
      }
    }
    exportedSizes.value = allSizes
    ElMessage.success(`已生成 ${allSizes.length} 个平台尺寸`)
  } catch (err: any) {
    ElMessage.error('导出失败: ' + (err?.message || '未知错误'))
  } finally {
    exporting.value = false
  }
}

function downloadSize(item: any) {
  if (!item.url) return
  const a = document.createElement('a')
  a.href = item.url
  a.download = `${item.platform}_${item.width}x${item.height}.png`
  a.click()
}
</script>

<style scoped>
.delivery-page {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.page-title {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-6) 0;
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.tab-content {
  padding: var(--space-2) 0;
}

.section {
  margin-bottom: var(--space-6);
}

.section h4 {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-3) 0;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--space-3);
}

.avatar-item {
  text-align: center;
  cursor: pointer;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-2);
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.avatar-item:hover {
  border-color: var(--color-primary);
}

.avatar-item.active {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.avatar-img {
  width: 60px;
  height: 60px;
  margin: 0 auto var(--space-2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-name {
  display: block;
  font-weight: 600;
  font-size: var(--text-base);
  color: var(--color-text);
}

.avatar-lang {
  display: block;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  margin-top: var(--space-1);
}

.progress-section {
  margin-bottom: var(--space-6);
}

.progress-text {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  margin-top: var(--space-2);
}

.result-section {
  margin-top: var(--space-4);
}

.result-section h4 {
  margin: 0 0 var(--space-3) 0;
}

.result-video {
  width: 400px;
  max-height: 300px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  display: block;
  margin-bottom: var(--space-3);
}

.save-btn {
  margin-top: var(--space-2);
}

.platform-checkbox {
  margin-right: var(--space-6);
  margin-bottom: var(--space-2);
}

.platform-label {
  font-size: var(--text-lg);
  font-weight: 500;
}

.upload-preview {
  margin-top: var(--space-3);
}

.size-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: var(--space-3);
}

.size-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  text-align: center;
  background: var(--color-bg-card);
}

.size-card-header {
  font-family: var(--font-heading);
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--space-2);
}

.size-card-body {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2);
}
</style>
