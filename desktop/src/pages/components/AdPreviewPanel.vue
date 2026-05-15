<template>
  <div class="ad-preview-panel">
    <h3 class="panel-title">Step 4：预览与导出</h3>

    <el-alert
      title="广告素材已就绪，确认预览无误后可一键下载全部素材到本地。"
      type="success"
      :closable="false"
      show-icon
      class="preview-alert"
    />

    <!-- Gallery + Product Info -->
    <div class="preview-top">
      <!-- Left: Image / Video Gallery -->
      <div class="gallery-col">
        <div class="gallery-main">
          <el-carousel
            v-if="galleryItems.length"
            :autoplay="false"
            trigger="click"
            height="420px"
            arrow="always"
            indicator-position="none"
          >
            <el-carousel-item v-for="(item, idx) in galleryItems" :key="idx">
              <template v-if="item.type === 'image'">
                <el-image :src="item.url" fit="contain" class="gallery-img" :preview-src-list="[item.url]" />
              </template>
              <template v-else>
                <video :src="item.url" controls class="gallery-video" />
              </template>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="gallery-empty">
            <el-icon :size="48" color="var(--color-text-muted)"><PictureFilled /></el-icon>
            <p>暂无预览素材</p>
          </div>
        </div>
        <!-- Thumbnail strip -->
        <div class="gallery-thumbs" v-if="galleryItems.length > 1">
          <div
            v-for="(item, idx) in galleryItems"
            :key="idx"
            class="thumb-item"
            :class="{ active: activeGalleryIdx === idx }"
            @click="activeGalleryIdx = idx"
          >
            <el-image
              v-if="item.type === 'image'"
              :src="item.url"
              fit="cover"
              class="thumb-img"
            />
            <div v-else class="thumb-video-badge">
              <el-icon :size="24"><VideoCameraFilled /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Info -->
      <div class="info-col">
        <!-- Product name -->
        <h1 class="product-name">{{ adCopy?.productName || '未命名商品' }}</h1>

        <!-- Market tags -->
        <div class="market-row">
          <el-tag
            v-if="adCopy"
            type="primary"
            size="default"
            effect="dark"
            round
          >
            {{ marketNames[adCopy.targetMarket] || adCopy.targetMarket }}
          </el-tag>
          <el-tag v-if="adCopy" size="default" effect="plain" round>
            {{ marketLangLabel }}
          </el-tag>
          <el-tag
            v-if="adImage"
            type="success"
            size="default"
            effect="plain"
            round
          >
            {{ adImage.generatedImage?.label || '主图' }}
          </el-tag>
          <span class="stat-label" v-if="adVideo">含视频素材</span>
        </div>

        <!-- Price / Point display -->
        <div class="price-section">
          <span class="price-symbol">约消耗</span>
          <span class="price-value">{{ estimatedPoints }}</span>
          <span class="price-unit">点数</span>
        </div>

        <!-- Specs table -->
        <div class="specs-table">
          <div class="spec-row">
            <span class="spec-label">素材类型</span>
            <span class="spec-value">{{ assetTypes.join(' / ') || '—' }}</span>
          </div>
          <div class="spec-row">
            <span class="spec-label">图片尺寸</span>
            <span class="spec-value">{{ adImage?.generatedImage?.label || '—' }}</span>
          </div>
          <div class="spec-row" v-if="adVideo">
            <span class="spec-label">视频时长</span>
            <span class="spec-value">{{ adVideo.duration || '—' }} 秒 | {{ styleLabel(adVideo.videoStyle) }}</span>
          </div>
          <div class="spec-row">
            <span class="spec-label">目标市场</span>
            <span class="spec-value">{{ marketNames[adCopy?.targetMarket] || '—' }}</span>
          </div>
          <div class="spec-row" v-if="adCopy?.sellingPoints?.length">
            <span class="spec-label">核心卖点</span>
            <span class="spec-value">{{ adCopy.sellingPoints.join(' · ') }}</span>
          </div>
        </div>

        <!-- Primary CTA -->
        <div class="info-actions">
          <el-button type="primary" size="large" class="btn-download-all" @click="handleSaveAll">
            <el-icon><Download /></el-icon> 下载全部素材
          </el-button>
          <el-button size="large" @click="handleDownloadAll">
            <el-icon><FolderOpened /></el-icon> 全屏预览
          </el-button>
        </div>

        <!-- Secondary actions -->
        <div class="info-secondary">
          <el-button size="small" text @click="emit('prev')">← 返回上一步</el-button>
          <el-button size="small" text type="danger" @click="handleNew">重新制作</el-button>
        </div>
      </div>
    </div>

    <!-- Bottom Section: Tabs -->
    <div class="preview-bottom">
      <el-tabs v-model="activeTab" class="detail-tabs">
        <!-- Tab 1: 营销分析 -->
        <el-tab-pane label="营销分析" name="copy">
          <div class="tab-content" v-if="adCopy?.analysis">
            <div class="description-block">
              <h4 class="block-title">产品信息</h4>
              <div class="desc-meta">
                <span><strong>产品名称：</strong>{{ adCopy.productName }}</span>
                <span><strong>目标市场：</strong>{{ marketNames[adCopy.targetMarket] || adCopy.targetMarket }}</span>
                <span><strong>中文广告词：</strong>{{ adCopy.chineseAdCopy || '未设置' }}</span>
              </div>
            </div>

            <div class="analysis-cards">
              <!-- Risk Level -->
              <div class="analysis-card compliance-card">
                <div class="card-header">
                  <span class="card-label">合规评估</span>
                  <el-tag
                    :type="adCopy.analysis.riskLevel === 'safe' ? 'success' : 'warning'"
                    size="default"
                    effect="dark"
                  >
                    {{ adCopy.analysis.riskLevel === 'safe' ? 'SAFE 合规' : 'WARNING 需关注' }}
                  </el-tag>
                </div>
              </div>

              <!-- Culture Notes -->
              <div class="analysis-card">
                <div class="card-header">
                  <span class="card-label">文化本土化建议</span>
                </div>
                <el-alert
                  :title="adCopy.analysis.cultureNotes"
                  type="info"
                  :closable="false"
                  show-icon
                />
              </div>

              <!-- Core Strategy -->
              <div class="analysis-card strategy-card">
                <div class="card-header">
                  <span class="card-label">核心营销策略</span>
                </div>
                <p class="strategy-text">{{ adCopy.analysis.coreStrategy }}</p>
              </div>

              <!-- Example Ad Copy -->
              <div class="analysis-card copy-card">
                <div class="card-header">
                  <span class="card-label">示例广告词</span>
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    @click="copyAdCopy"
                  >
                    <el-icon><DocumentCopy /></el-icon> 复制
                  </el-button>
                </div>
                <div class="copy-content">
                  <p>{{ adCopy.analysis.exampleAdCopy }}</p>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="尚未进行营销分析" :image-size="60" />
        </el-tab-pane>

        <!-- Tab 2: 素材详情 -->
        <el-tab-pane label="素材详情" name="assets">
          <div class="tab-content">
            <!-- Image asset -->
            <div class="asset-block" v-if="adImage?.generatedImage">
              <h4 class="block-title">广告图片
                <el-tag size="small" type="success" effect="plain" round style="margin-left:8px">已生成</el-tag>
              </h4>
              <div class="asset-display">
                <el-image
                  :src="adImage.generatedImage.url"
                  fit="contain"
                  class="asset-img-lg"
                  :preview-src-list="[adImage.generatedImage.url]"
                />
                <div class="asset-meta">
                  <div class="meta-item">
                    <span class="meta-key">标签</span>
                    <span class="meta-val">{{ adImage.generatedImage.label }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-key">风格</span>
                    <span class="meta-val">{{ styleLabel(adImage.style) }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-key">模板</span>
                    <span class="meta-val">{{ templateName(adImage.templateId) }}</span>
                  </div>
                  <el-button size="small" plain @click="handleSaveImage" :loading="savingImage">
                    <el-icon><Download /></el-icon> 下载此图
                  </el-button>
                </div>
              </div>
            </div>

            <!-- Video asset -->
            <div class="asset-block" v-if="adVideo?.generatedVideo">
              <h4 class="block-title">广告视频
                <el-tag size="small" type="success" effect="plain" round style="margin-left:8px">已生成</el-tag>
              </h4>
              <div class="asset-display">
                <video :src="adVideo.generatedVideo.url" controls class="asset-video-lg" />
                <div class="asset-meta">
                  <div class="meta-item">
                    <span class="meta-key">时长</span>
                    <span class="meta-val">{{ adVideo.duration || '—' }} 秒</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-key">风格</span>
                    <span class="meta-val">{{ styleLabel(adVideo.videoStyle) }}</span>
                  </div>
                  <el-button size="small" plain @click="handleSaveVideo" :loading="savingVideo">
                    <el-icon><Download /></el-icon> 下载此视频
                  </el-button>
                </div>
              </div>
            </div>

            <el-empty v-if="!hasAssets" description="暂无素材，请返回上一步生成" :image-size="60" />
          </div>
        </el-tab-pane>

        <!-- Tab 3: 卖点展示 -->
        <el-tab-pane label="卖点展示" name="features" v-if="adCopy?.sellingPoints?.length">
          <div class="tab-content">
            <div class="features-grid">
              <div
                v-for="(pt, idx) in adCopy.sellingPoints"
                :key="idx"
                class="feature-card"
              >
                <span class="feature-num">{{ String(idx + 1).padStart(2, '0') }}</span>
                <span class="feature-text">{{ pt }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Download, FolderOpened, PictureFilled, VideoCameraFilled, DocumentCopy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  adCopy: any
  adImage: any
  adVideo: any
}>()

const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'reset'): void
}>()

const activeTab = ref('copy')
const activeGalleryIdx = ref(0)
const savingImage = ref(false)
const savingVideo = ref(false)

const marketNames: Record<string, string> = {
  US: '美国', UK: '英国', DE: '德国', JP: '日本', SA: '沙特', BR: '巴西'
}

const marketLangLabel = computed(() => {
  const langMap: Record<string, string> = { US: 'English', UK: 'English', DE: 'Deutsch', JP: '日本語', SA: 'العربية', BR: 'Português' }
  return langMap[props.adCopy?.targetMarket] || ''
})

const galleryItems = computed(() => {
  const items: Array<{ type: 'image' | 'video'; url: string }> = []
  if (props.adImage?.generatedImage?.url) {
    items.push({ type: 'image', url: props.adImage.generatedImage.url })
  }
  if (props.adVideo?.generatedVideo?.url) {
    items.push({ type: 'video', url: props.adVideo.generatedVideo.url })
  }
  return items
})

const estimatedPoints = computed(() => {
  let pts = 0
  if (props.adCopy?.analysis) pts += 5
  if (props.adImage?.generatedImage) pts += 10
  if (props.adVideo?.generatedVideo) pts += 30
  return pts
})

const assetTypes = computed(() => {
  const types: string[] = []
  if (props.adImage?.generatedImage) types.push('图片')
  if (props.adVideo?.generatedVideo) types.push('视频')
  return types
})

const hasAssets = computed(() =>
  !!(props.adImage?.generatedImage || props.adVideo?.generatedVideo)
)

function styleLabel(s: string): string {
  const map: Record<string, string> = {
    modern: '现代简约', warm: '温暖色调', tech: '科技蓝调', luxury: '奢华黑金',
    nature: '自然清新', vintage: '复古风格',
    smooth: '平滑过渡', kenburns: 'Ken Burns', zoom: '缩放聚焦', slide: '滑动展示'
  }
  return map[s] || s || '—'
}

function templateName(id: string): string {
  const map: Record<string, string> = {
    t1: '产品展示', t2: '促销活动', t3: '品牌故事', t4: '生活方式',
    t5: '产品评测', t6: '开箱体验', t7: '限时特惠', t8: '社交分享'
  }
  return map[id] || id || '—'
}

async function handleSaveAll() {
  const items: Array<{ url: string; type: string }> = []
  if (props.adImage?.generatedImage?.url) {
    items.push({ url: props.adImage.generatedImage.url, type: 'image' })
  }
  if (props.adVideo?.generatedVideo?.url) {
    items.push({ url: props.adVideo.generatedVideo.url, type: 'video' })
  }
  if (items.length === 0) {
    ElMessage.warning('没有可保存的素材')
    return
  }

  let saved = 0
  for (const item of items) {
    if (!window.electronAPI) {
      const a = document.createElement('a')
      a.href = item.url
      a.download = `ad-${item.type}-${Date.now()}.${item.type === 'video' ? 'mp4' : 'png'}`
      a.click()
      saved++
      continue
    }
    try {
      const ext = item.type === 'video' ? 'mp4' : 'png'
      await window.electronAPI.saveFile({
        subDir: item.type === 'video' ? 'videos' : 'images',
        filename: `ad-${item.type}-${Date.now()}.${ext}`,
        dataUrl: item.url
      })
      saved++
    } catch (err: any) {
      ElMessage.error(`保存失败: ${err.message}`)
    }
  }
  ElMessage.success(`成功保存 ${saved} 个文件到本地素材库`)
}

async function handleSaveImage() {
  if (!props.adImage?.generatedImage?.url) return
  savingImage.value = true
  try {
    if (window.electronAPI) {
      await window.electronAPI.saveFile({
        subDir: 'images',
        filename: `ad-image-${Date.now()}.png`,
        dataUrl: props.adImage.generatedImage.url
      })
      ElMessage.success('图片已保存')
    }
  } catch (err: any) {
    ElMessage.error('保存失败: ' + err.message)
  } finally {
    savingImage.value = false
  }
}

async function handleSaveVideo() {
  if (!props.adVideo?.generatedVideo?.url) return
  savingVideo.value = true
  try {
    if (window.electronAPI) {
      await window.electronAPI.saveFile({
        subDir: 'videos',
        filename: `ad-video-${Date.now()}.mp4`,
        dataUrl: props.adVideo.generatedVideo.url
      })
      ElMessage.success('视频已保存')
    }
  } catch (err: any) {
    ElMessage.error('保存失败: ' + err.message)
  } finally {
    savingVideo.value = false
  }
}

function handleDownloadAll() {
  const previewData = {
    copy: props.adCopy?.translatedCopy
      ? { [marketNames[props.adCopy.targetMarket] || 'Target']: props.adCopy.translatedCopy.translated }
      : null,
    images: props.adImage?.generatedImage ? [props.adImage.generatedImage] : [],
    video: props.adVideo?.generatedVideo || null
  }
  sessionStorage.setItem('adPreviewData', JSON.stringify(previewData))
  window.open('/#/ad/preview', '_blank')
}

function handleNew() {
  sessionStorage.removeItem('adPreviewData')
  emit('reset')
}

async function copyAdCopy() {
  if (!props.adCopy?.analysis?.exampleAdCopy) return
  try {
    await navigator.clipboard.writeText(props.adCopy.analysis.exampleAdCopy)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.panel-title {
  font-family: var(--font-heading);
  margin: 0 0 var(--space-5) 0;
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.preview-alert {
  margin-bottom: var(--space-5);
}

.preview-top {
  display: flex;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
  min-height: 440px;
}

.gallery-col {
  width: 480px;
  flex-shrink: 0;
}

.gallery-main {
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: var(--space-3);
}

.gallery-main :deep(.el-carousel__container) {
  height: 420px;
}

.gallery-img {
  width: 100%;
  height: 420px;
  object-fit: contain;
}

.gallery-video {
  width: 100%;
  height: 420px;
  object-fit: contain;
  background: #000;
}

.gallery-empty {
  height: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  gap: var(--space-2);
}

.gallery-empty p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

/* Thumbnail strip */
.gallery-thumbs {
  display: flex;
  gap: var(--space-2);
  padding: 0 2px;
}

.thumb-item {
  width: 64px;
  height: 64px;
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: border-color var(--transition-fast);
  flex-shrink: 0;
}

.thumb-item:hover {
  border-color: var(--color-primary);
}

.thumb-item.active {
  border-color: var(--color-primary);
}

.thumb-img {
  width: 100%;
  height: 100%;
}

.thumb-video-badge {
  width: 100%;
  height: 100%;
  background: var(--color-bg-sidebar);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.info-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.product-name {
  font-family: var(--font-heading);
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

/* Market & type tags */
.market-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  align-items: center;
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--color-warning);
  font-weight: 500;
}

.price-section {
  background: linear-gradient(to right, var(--el-color-danger-light-9), var(--el-color-warning-light-9));
  padding: var(--space-4);
  border-radius: var(--radius-md);
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
}

.price-symbol {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.price-value {
  font-family: var(--font-mono);
  font-size: 2.25rem;
  font-weight: 700;
  color: var(--color-error);
  line-height: 1;
}

.price-unit {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

/* Specs table */
.specs-table {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.spec-row {
  display: flex;
  background: var(--color-bg);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
}

.spec-label {
  width: 80px;
  flex-shrink: 0;
  color: var(--color-text-muted);
}

.spec-value {
  color: var(--color-text);
  flex: 1;
}

/* Primary action buttons */
.info-actions {
  display: flex;
  gap: var(--space-3);
}

.btn-download-all {
  flex: 1;
  height: 46px;
  font-size: var(--text-lg);
  font-weight: 600;
  letter-spacing: 0.02em;
}

.info-secondary {
  display: flex;
  justify-content: space-between;
  padding-top: var(--space-1);
}

.preview-bottom {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.detail-tabs {
  padding: 0;
}

.detail-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 var(--space-6);
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.detail-tabs :deep(.el-tab-pane) {
  padding: var(--space-6);
}

/* Tab content */
.tab-content {
  min-height: 200px;
}

.block-title {
  font-family: var(--font-heading);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 var(--space-4) 0;
  padding-bottom: var(--space-2);
  border-bottom: 2px solid var(--color-primary);
  display: inline-block;
}

.description-block {
  margin-bottom: var(--space-6);
}

.desc-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  font-size: var(--text-base);
  color: var(--color-text);
  line-height: var(--leading-relaxed);
}

/* Translated / Original copy cards */
.translated-card,
.original-card {
  padding: var(--space-4);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-3);
}

.translated-card {
  background: var(--el-color-success-light-9);
  border: 1px solid var(--el-color-success-light-5);
}

.original-card {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
}

.lang-badge {
  display: inline-block;
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-success);
  margin-bottom: var(--space-2);
}

.lang-badge.original {
  color: var(--color-text-muted);
}

.translated-text,
.original-text {
  margin: 0;
  line-height: var(--leading-relaxed);
  font-size: var(--text-base);
  color: var(--color-text);
}

.asset-block {
  margin-bottom: var(--space-8);
}

.asset-display {
  display: flex;
  gap: var(--space-6);
  align-items: flex-start;
}

.asset-img-lg {
  width: 360px;
  height: 360px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  flex-shrink: 0;
}

.asset-video-lg {
  width: 480px;
  max-height: 360px;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.asset-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.meta-item {
  display: flex;
  justify-content: space-between;
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-border-light);
  font-size: var(--text-base);
}

.meta-key {
  color: var(--color-text-muted);
}

.meta-val {
  color: var(--color-text);
  font-weight: 500;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-3);
}

.feature-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.feature-num {
  font-family: var(--font-mono);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-primary);
  opacity: 0.3;
  line-height: 1;
}

.feature-text {
  font-size: var(--text-base);
  font-weight: 500;
  color: var(--color-text);
}

/* Analysis cards in preview */
.analysis-cards {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.analysis-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  background: var(--color-bg-card);
}

.analysis-card .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.analysis-card .card-label {
  font-weight: 600;
  font-size: var(--text-sm);
  color: var(--color-text);
}

.analysis-card.compliance-card {
  border-left: 4px solid var(--color-primary);
}

.analysis-card.strategy-card {
  border-left: 4px solid var(--color-warning);
  background: var(--color-bg);
}

.analysis-card .strategy-text {
  margin: 0;
  line-height: var(--leading-relaxed);
  color: var(--color-text);
  font-weight: 500;
  font-size: var(--text-sm);
}

.analysis-card .copy-content {
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  padding: var(--space-2);
}

.analysis-card .copy-content p {
  margin: 0;
  line-height: var(--leading-relaxed);
  color: var(--color-text);
  white-space: pre-wrap;
  font-size: var(--text-sm);
}
</style>
