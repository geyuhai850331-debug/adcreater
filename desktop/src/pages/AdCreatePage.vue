<template>
  <div class="ad-create-page">
    <h2 class="page-title">广告制作</h2>

    <el-steps :active="activeStep" align-center finish-status="success" class="steps-bar">
      <el-step title="Step 1" description="文案翻译" />
      <el-step title="Step 2" description="图片生成" />
      <el-step title="Step 3" description="视频生成" />
      <el-step title="Step 4" description="预览" />
    </el-steps>

    <div class="step-content">
      <!-- Step 1: 文案翻译 -->
      <div v-if="activeStep === 0" class="step-panel">
        <AdCopyPanel @next="(data) => { adCopy = data; activeStep = 1 }" />
      </div>

      <!-- Step 2: 图片生成 -->
      <div v-if="activeStep === 1" class="step-panel">
        <AdImagePanel
          :ad-copy="adCopy"
          @next="(data) => { adImage = data; activeStep = 2 }"
          @prev="activeStep = 0"
        />
      </div>

      <!-- Step 3: 视频生成 -->
      <div v-if="activeStep === 2" class="step-panel">
        <AdVideoPanel
          :ad-image="adImage"
          @next="(data) => { adVideo = data; activeStep = 3 }"
          @prev="activeStep = 1"
        />
      </div>

      <!-- Step 4: 预览 -->
      <div v-if="activeStep === 3" class="step-panel">
        <AdPreviewPanel
          :ad-copy="adCopy"
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
import AdCopyPanel from './components/AdCopyPanel.vue'
import AdImagePanel from './components/AdImagePanel.vue'
import AdVideoPanel from './components/AdVideoPanel.vue'
import AdPreviewPanel from './components/AdPreviewPanel.vue'

const activeStep = ref(0)
const adCopy = ref<any>(null)
const adImage = ref<any>(null)
const adVideo = ref<any>(null)

function handleReset() {
  activeStep.value = 0
  adCopy.value = null
  adImage.value = null
  adVideo.value = null
}
</script>

<style scoped>
.ad-create-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 24px 0;
  color: #303133;
}

.steps-bar {
  margin-bottom: 32px;
}

.step-panel {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  min-height: 420px;
}
</style>
