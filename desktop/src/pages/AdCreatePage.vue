<template>
  <div class="ad-create-page">
    <h2 class="page-title">广告制作</h2>

    <nav class="steps-bar">
      <button
        v-for="(step, idx) in steps"
        :key="idx"
        class="step-item"
        :class="{
          active: activeStep === idx,
          done: activeStep > idx
        }"
        @click="activeStep = idx"
      >
        <span class="step-num">{{ idx + 1 }}</span>
        <span class="step-info">
          <span class="step-title">{{ step.title }}</span>
          <span class="step-desc">{{ step.desc }}</span>
        </span>
      </button>
    </nav>

    <div class="step-content">
      <!-- Step 1: 营销策划 -->
      <div v-if="activeStep === 0" class="step-panel">
        <MarketingPanel @next="(data) => { marketingData = data; activeStep = 1 }" />
      </div>

      <!-- Step 2: 图片生成 -->
      <div v-if="activeStep === 1" class="step-panel">
        <AdImagePanel
          :ad-copy="marketingData"
          @next="(data) => { adImage = data; activeStep = 2 }"
          @prev="activeStep = 0"
        />
      </div>

      <!-- Step 3: 视频生成 -->
      <div v-if="activeStep === 2" class="step-panel">
        <AdVideoPanel
          @next="(data) => { adVideo = data; activeStep = 3 }"
          @prev="activeStep = 1"
        />
      </div>

      <!-- Step 4: 预览 -->
      <div v-if="activeStep === 3" class="step-panel">
        <AdPreviewPanel
          :ad-copy="marketingData"
          :ad-image="adImage"
          :ad-video="adVideo"
          @prev="activeStep = 2"
          @reset="handleReset"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MarketingPanel from './components/MarketingPanel.vue'
import AdImagePanel from './components/AdImagePanel.vue'
import AdVideoPanel from './components/AdVideoPanel.vue'
import AdPreviewPanel from './components/AdPreviewPanel.vue'

const activeStep = ref(0)
const marketingData = ref<any>(null)
const adImage = ref<any>(null)
const adVideo = ref<any>(null)

const steps = [
  { title: '策划', desc: '营销分析 & 文案生成' },
  { title: '图片', desc: '多尺寸生成' },
  { title: '视频', desc: '动态素材' },
  { title: '完成', desc: '预览导出' }
]

function handleReset() {
  activeStep.value = 0
  marketingData.value = null
  adImage.value = null
  adVideo.value = null
}
</script>

<style scoped>
.ad-create-page {
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

.steps-bar {
  display: flex;
  gap: 0;
  margin-bottom: var(--space-8);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.step-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border: none;
  border-right: 1px solid var(--color-border);
  background: var(--color-bg-card);
  cursor: pointer;
  transition: background var(--transition-fast);
  font-family: inherit;
  text-align: left;
  position: relative;
}

.step-item:last-child {
  border-right: none;
}

.step-item:hover {
  background: var(--color-bg);
}

.step-item.active {
  background: var(--color-primary-light);
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-text-secondary);
  flex-shrink: 0;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.step-item.active .step-num {
  background: var(--color-primary);
  color: #fff;
}

.step-item.done .step-num {
  background: var(--color-success);
  color: #fff;
}

.step-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.step-title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  transition: color var(--transition-fast);
}

.step-item.active .step-title {
  color: var(--color-primary);
}

.step-item.done .step-title {
  color: var(--color-text);
}

.step-desc {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  line-height: 1.3;
}

.step-panel {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  min-height: 420px;
}
</style>
