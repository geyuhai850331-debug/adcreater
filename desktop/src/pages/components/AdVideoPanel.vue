<template>
  <div class="ad-video-panel">
    <!-- ================================================================ -->
    <!-- Sub-Steps Bar                                                     -->
    <!-- ================================================================ -->
    <el-steps :active="subStep" align-center finish-status="success" class="sub-steps" process-status="process">
      <el-step title="分镜策划" description="脚本 & 故事线" />
      <el-step title="关键帧" description="逐帧图片生成" />
      <el-step title="视频合成" description="拼接 & 导出" />
    </el-steps>

    <div class="sub-step-content">
      <!-- ================================================================ -->
      <!-- Sub-Step 0: 分镜策划  (ref: Step1StoryboardPlan)                -->
      <!-- ================================================================ -->
      <div v-if="subStep === 0" class="sub-panel">
        <!-- Input Card -->
        <el-card class="input-card">
          <template #header>
            <div class="card-title-row">
              <el-icon :size="16"><Edit /></el-icon>
              <span>商品信息</span>
            </div>
          </template>

          <!-- Product Image Upload -->
          <div class="field">
            <label class="field-label">商品图片（可选）</label>
            <div
              class="upload-zone"
              :class="{ 'has-image': uploadedImage }"
              @click="triggerFileInput"
              @dragover.prevent
              @drop.prevent="handleDrop"
            >
              <template v-if="uploadedImage">
                <el-image :src="uploadedImage" fit="contain" class="upload-preview" />
                <el-button
                  type="danger"
                  size="small"
                  circle
                  class="upload-remove"
                  @click.stop="uploadedImage = null"
                >
                  <el-icon :size="12"><Close /></el-icon>
                </el-button>
              </template>
              <template v-else>
                <el-icon :size="24" color="#94A3B8"><Upload /></el-icon>
                <p class="upload-hint">点击或拖拽上传商品图</p>
              </template>
            </div>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              class="hidden-input"
              @change="handleFileSelect"
            />
          </div>

          <!-- Product Description -->
          <div class="field">
            <label class="field-label">商品描述 *</label>
            <el-input
              v-model="productDescription"
              type="textarea"
              :rows="4"
              placeholder="输入商品的详细描述：名称、功能、卖点、目标用户等..."
              :disabled="sbStatus === 'loading'"
            />
          </div>

          <!-- Category / Platform / Language -->
          <div class="input-row">
            <div class="field">
              <label class="field-label">商品类别</label>
              <el-select v-model="category" class="field-input" :disabled="sbStatus === 'loading'" clearable>
                <el-option
                  v-for="c in categories"
                  :key="c"
                  :label="c"
                  :value="c"
                />
              </el-select>
            </div>
            <div class="field">
              <label class="field-label">目标平台</label>
              <el-select v-model="targetPlatform" class="field-input" :disabled="sbStatus === 'loading'" clearable>
                <el-option
                  v-for="p in platforms"
                  :key="p.id"
                  :label="p.name"
                  :value="p.id"
                />
              </el-select>
            </div>
            <div class="field">
              <label class="field-label">目标语言</label>
              <el-select v-model="targetLang" class="field-input" :disabled="sbStatus === 'loading'" clearable>
                <el-option
                  v-for="l in languages"
                  :key="l.id"
                  :label="l.name"
                  :value="l.id"
                />
              </el-select>
            </div>
          </div>

          <!-- Template Selector -->
          <div class="field">
            <label class="field-label">视频模板（可选）</label>
            <div class="template-grid">
              <button
                v-for="tpl in videoTemplates"
                :key="tpl.id"
                type="button"
                class="template-item"
                :class="{ active: selectedTemplate === tpl.id }"
                :disabled="sbStatus === 'loading'"
                @click="selectedTemplate = selectedTemplate === tpl.id ? null : tpl.id"
              >
                <div class="template-thumb">
                  <el-icon :size="32" color="#94A3B8"><VideoCameraFilled /></el-icon>
                </div>
                <span class="template-name">{{ tpl.name }}</span>
                <el-icon v-if="selectedTemplate === tpl.id" class="template-check" :size="14" color="#6366F1">
                  <CircleCheckFilled />
                </el-icon>
              </button>
            </div>
          </div>

          <!-- Generate Button -->
          <el-button
            type="primary"
            size="large"
            class="generate-btn"
            :loading="sbStatus === 'loading'"
            :disabled="!productDescription.trim()"
            @click="handleGenerateStoryboards"
          >
            <el-icon v-if="sbStatus !== 'loading'"><MagicStick /></el-icon>
            {{ sbStatus === 'loading' ? 'AI 正在分析商品并生成分镜方案...' : '生成分镜方案' }}
          </el-button>
          <p class="hint-text">
            AI 将自动分析商品特征，生成适合的分镜方案，每个分镜包含 4 个关键帧
          </p>
        </el-card>

        <!-- Error State -->
        <el-card v-if="sbStatus === 'error'" class="error-card">
          <div class="error-content">
            <el-icon :size="20" color="#EF4444"><WarningFilled /></el-icon>
            <div class="error-info">
              <p class="error-title">生成失败</p>
              <p class="error-msg">{{ sbError }}</p>
            </div>
            <el-button size="small" @click="handleGenerateStoryboards">
              <el-icon><Refresh /></el-icon> 重试
            </el-button>
          </div>
        </el-card>

        <!-- Loading Skeleton -->
        <el-card v-if="sbStatus === 'loading'" class="skeleton-card">
          <div class="skeleton-list">
            <div v-for="i in 3" :key="i" class="skeleton-item">
              <div class="skeleton-line w-20" />
              <div class="skeleton-line w-full" />
              <div class="skeleton-line w-3/4" />
              <div class="skeleton-grid">
                <div v-for="j in 4" :key="j" class="skeleton-cell" />
              </div>
            </div>
          </div>
        </el-card>

        <!-- Result: Storyboard List -->
        <div v-if="sbStatus === 'success' && storyboards.length" class="result-area">
          <h4 class="result-title">
            <el-icon :size="16"><Film /></el-icon>
            分镜列表 ({{ storyboards.length }})
          </h4>

          <div class="storyboard-list">
            <el-card
              v-for="(sb, i) in storyboards"
              :key="i"
              class="storyboard-card"
            >
              <template #header>
                <div class="sb-header">
                  <el-tag size="small" type="info">分镜 {{ i + 1 }}/{{ storyboards.length }}</el-tag>
                  <el-button
                    size="small"
                    :loading="regeneratingIdx === i"
                    @click="handleRegenerateStoryboard(i)"
                  >
                    <el-icon><Refresh /></el-icon>
                    {{ regeneratingIdx === i ? '重新生成中...' : '重新生成' }}
                  </el-button>
                </div>
              </template>

              <!-- Editable Description -->
              <div class="sb-body">
                <template v-if="editingSbIdx === i">
                  <el-input
                    v-model="editSbDraft"
                    type="textarea"
                    :rows="3"
                    size="small"
                  />
                  <div class="edit-actions">
                    <el-button size="small" @click="editingSbIdx = null">取消</el-button>
                    <el-button size="small" type="primary" @click="handleSaveSbEdit(i)">保存</el-button>
                  </div>
                </template>
                <p
                  v-else
                  class="sb-desc"
                  @click="startEditSb(i)"
                  title="点击编辑分镜描述"
                >
                  {{ sb.description }}
                </p>

                <!-- Keyframe Placeholders -->
                <div class="kf-grid">
                  <div
                    v-for="(kf, j) in sb.keyFrames"
                    :key="j"
                    class="kf-cell"
                  >
                    <el-icon :size="20" color="#CBD5E1"><PictureFilled /></el-icon>
                    <span class="kf-label">帧 {{ j + 1 }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- Idle Empty State -->
        <el-card v-if="sbStatus === 'idle'" class="empty-card">
          <div class="empty-content">
            <el-icon :size="40" color="#CBD5E1"><Film /></el-icon>
            <p class="empty-title">输入商品描述后点击「生成分镜方案」</p>
            <p class="empty-desc">AI 将自动生成视频分镜框架</p>
          </div>
        </el-card>

        <!-- Navigation -->
        <div class="sub-nav">
          <el-button @click="emit('prev')">返回图片生成</el-button>
          <el-button
            type="primary"
            :disabled="sbStatus !== 'success' || !storyboards.length"
            @click="subStep = 1"
          >
            下一步：关键帧生成
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- ================================================================ -->
      <!-- Sub-Step 1: 关键帧生成  (ref: Step2FrameGeneration)             -->
      <!-- ================================================================ -->
      <div v-if="subStep === 1" class="sub-panel">
        <!-- Summary Card -->
        <el-card class="summary-card">
          <div class="summary-content">
            <div class="summary-icon">
              <el-icon :size="20"><PictureFilled /></el-icon>
            </div>
            <div class="summary-text">
              <h3 class="summary-title">关键帧图片生成</h3>
              <p class="summary-desc">
                {{ storyboards.length }} 个分镜 · 每分镜 4 帧
                <span class="summary-progress">
                  · {{ totalFramesDone }} / {{ storyboards.length * 4 }} 帧已生成
                </span>
              </p>
            </div>
            <el-tag :type="allFramesDone ? 'success' : 'info'" size="small">
              {{ totalFramesDone }} / {{ storyboards.length }} 分镜完成
            </el-tag>
          </div>
        </el-card>

        <!-- Storyboard Tabs -->
        <el-tabs v-model="activeSbTab" class="sb-tabs">
          <el-tab-pane
            v-for="(sb, i) in storyboards"
            :key="i"
            :label="`分镜 ${i + 1}`"
            :name="String(i)"
          >
            <template #label>
              <span class="tab-label">
                <el-icon v-if="gridStatus[i] === 'generating'" class="is-loading" :size="14"><Loading /></el-icon>
                <el-icon v-else-if="gridStatus[i] === 'done'" :size="14" color="#22C55E"><CircleCheckFilled /></el-icon>
                分镜 {{ i + 1 }}
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- Active Storyboard Tab Content -->
        <template v-for="(sb, i) in storyboards" :key="'content-' + i">
          <div v-if="activeSbTab === String(i)" class="sb-tab-content">
            <el-card :class="['frame-card', { 'border-success': gridStatus[i] === 'done' }]">
              <template #header>
                <div class="frame-card-header">
                  <div class="frame-card-left">
                    <el-tag
                      :type="gridStatus[i] === 'done' ? 'success' : gridStatus[i] === 'generating' ? 'warning' : 'info'"
                      size="small"
                    >
                      <el-icon v-if="gridStatus[i] === 'generating'" class="is-loading" :size="12"><Loading /></el-icon>
                      {{ gridStatus[i] === 'done' ? '已完成' : gridStatus[i] === 'generating' ? '生成中' : '待生成' }}
                    </el-tag>
                    <span class="frame-desc">{{ sb.description.slice(0, 60) }}...</span>
                  </div>
                  <div class="frame-card-right">
                    <el-button
                      v-if="gridImages[i]"
                      size="small"
                      link
                      @click="previewGrid = { idx: i, url: gridImages[i] }"
                    >
                      <el-icon><ZoomIn /></el-icon> 查看宫格
                    </el-button>
                  </div>
                </div>
              </template>

              <div class="frame-grid-layout">
                <!-- Left: Keyframe Prompts -->
                <div class="frame-prompts">
                  <label class="field-label">关键帧 Prompt</label>
                  <div
                    v-for="(kf, j) in sb.keyFrames"
                    :key="j"
                    class="kf-prompt-item"
                  >
                    <div class="kf-prompt-preview">
                      <el-image
                        v-if="frameImages[`${i}-${j}`]"
                        :src="frameImages[`${i}-${j}`]"
                        fit="cover"
                        class="kf-prompt-thumb"
                      />
                      <div v-else class="kf-prompt-placeholder">
                        <el-icon :size="20"><PictureFilled /></el-icon>
                      </div>
                    </div>
                    <button
                      type="button"
                      class="kf-prompt-btn"
                      @click="startEditPrompt(i, j, kf.prompt)"
                    >
                      <div class="kf-prompt-top">
                        <el-tag :type="frameImages[`${i}-${j}`] ? 'success' : 'info'" size="small">
                          帧 {{ j + 1 }}
                        </el-tag>
                        <el-icon v-if="frameImages[`${i}-${j}`]" :size="14" color="#22C55E"><CircleCheckFilled /></el-icon>
                      </div>
                      <p class="kf-prompt-text">{{ kf.prompt || '(空 prompt)' }}</p>
                    </button>
                    <div class="kf-prompt-actions">
                      <el-button
                        size="small"
                        :loading="frameGenerating[`${i}-${j}`]"
                        :disabled="!kf.prompt?.trim()"
                        @click="handleGenerateSingleFrame(i, j)"
                      >
                        <el-icon v-if="!frameGenerating[`${i}-${j}`]"><MagicStick /></el-icon>
                        {{ frameImages[`${i}-${j}`] ? '重新生成' : '生成' }}
                      </el-button>
                    </div>
                  </div>
                </div>

                <!-- Right: Grid Preview -->
                <div class="frame-grid-preview">
                  <template v-if="gridStatus[i] === 'generating'">
                    <div class="grid-loading">
                      <el-icon :size="40" class="is-loading" color="#6366F1"><Loading /></el-icon>
                      <p>服务端正在生成 4 帧并合成宫格...</p>
                    </div>
                  </template>
                  <template v-else-if="gridImages[i]">
                    <div class="grid-preview-wrap" @click="previewGrid = { idx: i, url: gridImages[i] }">
                      <el-image :src="gridImages[i]" fit="contain" class="grid-preview-img" />
                    </div>
                    <div class="grid-actions">
                      <el-button size="small" @click="handleRegenerateGrid(i)">
                        <el-icon><Refresh /></el-icon> 重新生成
                      </el-button>
                      <el-button size="small" @click="downloadGridImage(i)">
                        <el-icon><Download /></el-icon> 下载宫格
                      </el-button>
                    </div>
                  </template>
                  <template v-else>
                    <div class="grid-empty">
                      <el-icon :size="40" color="#CBD5E1"><Grid /></el-icon>
                      <p>点击下方按钮，服务端生成 4 帧并合成宫格</p>
                    </div>
                  </template>

                  <el-button
                    v-if="gridStatus[i] !== 'generating'"
                    size="small"
                    type="primary"
                    class="grid-generate-btn"
                    @click="handleGenerateGrid(i)"
                  >
                    <el-icon><MagicStick /></el-icon>
                    {{ gridImages[i] ? '重新生成宫格' : '生成 4 宫格' }}
                  </el-button>
                </div>
              </div>

              <!-- Prompt Editor (inline) -->
              <div v-if="editingPrompt?.sbIdx === i" class="prompt-editor">
                <label class="field-label">编辑帧 {{ editingPrompt.kfIdx }} Prompt</label>
                <div class="prompt-editor-row">
                  <el-input v-model="draftPrompt" type="textarea" :rows="2" size="small" />
                  <div class="prompt-editor-actions">
                    <el-button size="small" @click="editingPrompt = null">取消</el-button>
                    <el-button size="small" type="primary" @click="handleSavePrompt">保存</el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </template>

        <!-- Actions -->
        <div class="sub-nav">
          <el-button @click="subStep = 0">
            <el-icon><ArrowLeft /></el-icon> 返回编辑分镜
          </el-button>
          <div class="sub-nav-right">
            <el-button @click="handleGenerateAllGrids">
              <el-icon><MagicStick /></el-icon> 批量生成所有分镜
            </el-button>
            <el-button
              type="primary"
              :disabled="!allFramesDone"
              @click="subStep = 2"
            >
              继续视频合成
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- Grid Preview Dialog -->
        <el-dialog v-model="gridPreviewVisible" title="宫格预览" width="800px">
          <el-image
            v-if="previewGrid"
            :src="previewGrid.url"
            fit="contain"
            class="grid-preview-dialog-img"
          />
          <template #footer>
            <el-button @click="gridPreviewVisible = false">关闭</el-button>
            <el-button type="primary" @click="downloadGridImage(previewGrid?.idx!)">
              <el-icon><Download /></el-icon> 下载
            </el-button>
          </template>
        </el-dialog>
      </div>

      <!-- ================================================================ -->
      <!-- Sub-Step 2: 视频合成  (ref: Step3VideoGeneration)               -->
      <!-- ================================================================ -->
      <div v-if="subStep === 2" class="sub-panel">
        <!-- Result Mode -->
        <el-card v-if="generatedVideo" class="result-card">
          <template #header>
            <div class="result-header">
              <el-icon :size="16" color="#22C55E"><CircleCheckFilled /></el-icon>
              <span>视频生成完成</span>
            </div>
          </template>
          <div class="result-body">
            <div class="video-player-wrap">
              <video :src="generatedVideo.url" controls class="result-player" />
            </div>
            <div class="result-actions">
              <el-button type="primary" size="small" @click="saveVideo" :loading="saving">
                <el-icon><Download /></el-icon> 下载视频
              </el-button>
              <el-button size="small" @click="handleResetVideo">
                <el-icon><Refresh /></el-icon> 重新生成
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- Config Mode -->
        <template v-if="!generatedVideo">
          <div class="config-grid">
            <!-- Left Column -->
            <div class="config-col">
              <!-- Duration -->
              <el-card class="config-card">
                <template #header>
                  <span class="config-card-title">视频时长</span>
                </template>
                <el-select v-model="duration" class="field-input">
                  <el-option v-for="d in durationOptions" :key="d" :label="`${d} 秒`" :value="d" />
                </el-select>
              </el-card>

              <!-- TTS (Collapsible) -->
              <el-card class="config-card">
                <template #header>
                  <div class="collapse-header" @click="ttsExpanded = !ttsExpanded">
                    <div class="collapse-title">
                      <el-icon :size="14"><Microphone /></el-icon>
                      <span>TTS 配音</span>
                    </div>
                    <el-icon :size="14" class="collapse-chevron">
                      <ArrowDown v-if="!ttsExpanded" /><ArrowUp v-else />
                    </el-icon>
                  </div>
                </template>
                <div v-show="ttsExpanded" class="collapse-body">
                  <div class="field">
                    <label class="field-label">语言</label>
                    <el-select v-model="ttsLang" size="small" class="field-input">
                      <el-option v-for="l in ttsLanguages" :key="l.id" :label="l.name" :value="l.id" />
                    </el-select>
                  </div>
                  <div class="field">
                    <label class="field-label">音色</label>
                    <el-select v-model="ttsVoice" size="small" class="field-input">
                      <el-option v-for="v in currentVoices" :key="v.id" :label="v.name" :value="v.id" />
                    </el-select>
                  </div>
                  <div class="field">
                    <div class="field-label-row">
                      <label class="field-label">语速</label>
                      <span class="field-value">{{ ttsRate > 0 ? '+' : '' }}{{ ttsRate }}%</span>
                    </div>
                    <el-slider v-model="ttsRate" :min="-50" :max="50" :step="10" size="small" show-stops />
                  </div>
                  <div class="field">
                    <div class="field-label-row">
                      <label class="field-label">音调</label>
                      <span class="field-value">{{ ttsPitch > 0 ? '+' : '' }}{{ ttsPitch }}%</span>
                    </div>
                    <el-slider v-model="ttsPitch" :min="-50" :max="50" :step="10" size="small" show-stops />
                  </div>
                </div>
              </el-card>

              <!-- BGM (Collapsible) -->
              <el-card class="config-card">
                <template #header>
                  <div class="collapse-header" @click="bgmExpanded = !bgmExpanded">
                    <div class="collapse-title">
                      <el-icon :size="14"><Headset /></el-icon>
                      <span>BGM 背景音乐</span>
                    </div>
                    <el-icon :size="14" class="collapse-chevron">
                      <ArrowDown v-if="!bgmExpanded" /><ArrowUp v-else />
                    </el-icon>
                  </div>
                </template>
                <div v-show="bgmExpanded" class="collapse-body">
                  <div class="field">
                    <label class="field-label">背景音乐</label>
                    <el-select v-model="bgmSelection" size="small" class="field-input">
                      <el-option v-for="b in bgmList" :key="b.id" :label="b.name" :value="b.id" />
                    </el-select>
                  </div>
                  <div class="field">
                    <div class="field-label-row">
                      <label class="field-label">BGM 音量</label>
                      <span class="field-value">{{ bgmVolume }}%</span>
                    </div>
                    <el-slider v-model="bgmVolume" :min="0" :max="100" size="small" />
                  </div>
                  <div class="field">
                    <div class="field-label-row">
                      <label class="field-label">旁白音量</label>
                      <span class="field-value">{{ narrationVolume }}%</span>
                    </div>
                    <el-slider v-model="narrationVolume" :min="0" :max="100" size="small" />
                  </div>
                </div>
              </el-card>
            </div>

            <!-- Right Column -->
            <div class="config-col">
              <!-- Storyboard Summary -->
              <el-card class="config-card">
                <template #header>
                  <div class="config-card-title-row">
                    <el-icon :size="14"><Film /></el-icon>
                    <span class="config-card-title">分镜信息</span>
                  </div>
                </template>
                <div class="storyboard-summary">
                  <div v-for="(sb, i) in storyboards" :key="i" class="sb-summary-item">
                    <el-tag size="small" type="info">分镜 {{ i + 1 }}</el-tag>
                    <span class="sb-summary-desc">{{ sb.description.slice(0, 50) }}...</span>
                    <span class="sb-summary-frames">{{ sb.keyFrames?.length || 4 }} 帧</span>
                  </div>
                </div>
              </el-card>

              <!-- Generate Button -->
              <el-button
                type="primary"
                size="large"
                class="generate-btn"
                :loading="generating"
                @click="handleGenerateVideo"
              >
                <el-icon v-if="!generating"><VideoPlay /></el-icon>
                {{ generating ? `生成中 ${Math.round(progress)}%` : '开始生成视频' }}
              </el-button>

              <div v-if="generating" class="progress-wrap">
                <el-progress :percentage="progress" :status="progressStatus" :stroke-width="6" />
                <p class="progress-text">正在合成 {{ storyboards.length }} 个分镜生成视频...</p>
              </div>
            </div>
          </div>
        </template>

        <div class="sub-nav">
          <el-button @click="subStep = 1" :disabled="generating">
            <el-icon><ArrowLeft /></el-icon> 返回关键帧编辑
          </el-button>
          <el-button
            v-if="generatedVideo"
            type="primary"
            @click="handleNext"
          >
            下一步：预览导出
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  Download, Refresh, VideoCameraFilled, CircleCheckFilled,
  Microphone, Headset, ArrowDown, ArrowUp, ArrowLeft, ArrowRight,
  VideoPlay, Film, PictureFilled, MagicStick, Edit, Upload,
  Close, WarningFilled, Loading, ZoomIn, Grid
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const emit = defineEmits<{
  (e: 'next', data: any): void
  (e: 'prev'): void
}>()

// ── Constants ────────────────────────────────────────────────────────
const categories = ['电子产品', '家居用品', '美妆护肤', '食品饮料', '运动户外', '服装配饰', '母婴用品']
const platforms = [
  { id: 'amazon', name: 'Amazon' },
  { id: 'facebook', name: 'Facebook' },
  { id: 'instagram', name: 'Instagram' },
  { id: 'tiktok', name: 'TikTok' },
  { id: 'youtube', name: 'YouTube' }
]
const languages = [
  { id: 'zh-CN', name: '中文（普通话）' },
  { id: 'en-US', name: '英语（美式）' },
  { id: 'ja-JP', name: '日语' },
  { id: 'ko-KR', name: '韩语' },
  { id: 'de-DE', name: '德语' },
  { id: 'fr-FR', name: '法语' }
]
const videoTemplates = [
  { id: 'vt1', name: '产品展示' },
  { id: 'vt2', name: '促销活动' },
  { id: 'vt3', name: '品牌故事' },
  { id: 'vt4', name: '使用教程' }
]
const durationOptions = [15, 30, 45, 60, 90]
const ttsLanguages = [
  { id: 'zh-CN', name: '中文（普通话）' },
  { id: 'zh-HK', name: '中文（粤语）' },
  { id: 'en-US', name: '英语（美式）' },
  { id: 'en-GB', name: '英语（英式）' },
  { id: 'ja-JP', name: '日语' },
  { id: 'ko-KR', name: '韩语' }
]
const ttsVoices: Record<string, { id: string; name: string }[]> = {
  'zh-CN': [{ id: 'zh-CN-XiaoxiaoNeural', name: '晓晓（女）' }, { id: 'zh-CN-YunxiNeural', name: '云希（男）' }],
  'zh-HK': [{ id: 'zh-HK-HiuGaaiNeural', name: '曉佳（女）' }, { id: 'zh-HK-WanLungNeural', name: '雲龍（男）' }],
  'en-US': [{ id: 'en-US-JennyNeural', name: 'Jenny（女）' }, { id: 'en-US-GuyNeural', name: 'Guy（男）' }],
  'en-GB': [{ id: 'en-GB-SoniaNeural', name: 'Sonia（女）' }, { id: 'en-GB-RyanNeural', name: 'Ryan（男）' }],
  'ja-JP': [{ id: 'ja-JP-NanamiNeural', name: 'Nanami（女）' }, { id: 'ja-JP-KeitaNeural', name: 'Keita（男）' }],
  'ko-KR': [{ id: 'ko-KR-SunHiNeural', name: 'SunHi（女）' }, { id: 'ko-KR-InJoonNeural', name: 'InJoon（男）' }]
}
const bgmList = [
  { id: 'none', name: '无背景音乐' }, { id: 'upbeat', name: '轻快活泼' }, { id: 'warm', name: '温馨抒情' },
  { id: 'electronic', name: '时尚电子' }, { id: 'nature', name: '自然白噪音' }, { id: 'jpop', name: '日系清新' },
  { id: 'beat', name: '动感节拍' }, { id: 'classical', name: '古典优雅' }
]

// ── Sub-Step Tracking ────────────────────────────────────────────────
const subStep = ref(0)

// ── Sub-Step 0: Storyboard Planning ──────────────────────────────────
interface KeyFrame { order: number; prompt: string }
interface Storyboard { order: number; description: string; keyFrames: KeyFrame[] }

const productDescription = ref('')
const category = ref('')
const targetPlatform = ref('')
const targetLang = ref('')
const uploadedImage = ref<string | null>(null)
const selectedTemplate = ref<string | null>(null)
const fileInputRef = ref<HTMLInputElement>()

const sbStatus = ref<'idle' | 'loading' | 'success' | 'error'>('idle')
const sbError = ref('')
const storyboards = ref<Storyboard[]>([])
const regeneratingIdx = ref<number | null>(null)
const editingSbIdx = ref<number | null>(null)
const editSbDraft = ref('')

function triggerFileInput() { fileInputRef.value?.click() }

function handleFileSelect(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) readAndSetImage(file)
}

function handleDrop(e: DragEvent) {
  const file = e.dataTransfer?.files?.[0]
  if (file && file.type.startsWith('image/')) readAndSetImage(file)
}

function readAndSetImage(file: File) {
  const reader = new FileReader()
  reader.onload = (ev) => { uploadedImage.value = ev.target?.result as string }
  reader.readAsDataURL(file)
}

async function handleGenerateStoryboards() {
  if (!productDescription.value.trim()) {
    ElMessage.warning('请输入商品描述')
    return
  }
  sbStatus.value = 'loading'
  sbError.value = ''
  storyboards.value = []

  try {
    const res = await client.post('/ad/video/storyboard/generate', {
      productDescription: productDescription.value.trim(),
      category: category.value || undefined,
      targetPlatform: targetPlatform.value || undefined,
      targetLang: targetLang.value || undefined,
      templateId: selectedTemplate.value || undefined
    }) as any
    const data = res?.data ?? res
    storyboards.value = data?.storyboards || data || []
    sbStatus.value = 'success'
    ElMessage.success(`生成了 ${storyboards.value.length} 个分镜`)
  } catch {
    // Mock fallback: generate default storyboards
    const points = productDescription.value.split(/[，,。.\n]/).filter(Boolean)
    storyboards.value = Array.from({ length: Math.min(points.length || 3, 5) }, (_, i) => ({
      order: i,
      description: points[i] || `场景 ${i + 1}：展示产品特点与优势`,
      keyFrames: Array.from({ length: 4 }, (_, j) => ({
        order: j,
        prompt: `${storyboards.value[i]?.description || ''} - 关键帧 ${j + 1} - ${['远景', '中景', '特写', '产品细节'][j]}`
      }))
    }))
    // Fill in prompts after creation
    storyboards.value = storyboards.value.map((sb, i) => ({
      ...sb,
      keyFrames: Array.from({ length: 4 }, (_, j) => ({
        order: j,
        prompt: `${points[i] || `场景 ${i + 1}`} - ${['开场远景展示全貌', '中景展示核心功能', '特写强调产品细节', '品牌标识与行动号召'][j]}`
      }))
    }))
    sbStatus.value = 'success'
  }
}

async function handleRegenerateStoryboard(idx: number) {
  regeneratingIdx.value = idx
  try {
    const res = await client.post('/ad/video/storyboard/regenerate', {
      description: storyboards.value[idx].description,
      index: idx
    }) as any
    const data = res?.data ?? res
    if (data?.keyFrames) {
      storyboards.value[idx].keyFrames = data.keyFrames
    }
    ElMessage.success(`分镜 ${idx + 1} 已重新生成`)
  } catch {
    // keep current
  } finally {
    regeneratingIdx.value = null
  }
}

function startEditSb(idx: number) {
  editingSbIdx.value = idx
  editSbDraft.value = storyboards.value[idx].description
}

function handleSaveSbEdit(idx: number) {
  storyboards.value[idx].description = editSbDraft.value
  editingSbIdx.value = null
}

// ── Sub-Step 1: Keyframe Generation ──────────────────────────────────
const activeSbTab = ref('0')
const gridStatus = ref<Record<number, 'idle' | 'generating' | 'done'>>({})
const gridImages = ref<Record<number, string>>({})
const frameImages = ref<Record<string, string>>({})
const frameGenerating = ref<Record<string, boolean>>({})
const editingPrompt = ref<{ sbIdx: number; kfIdx: number } | null>(null)
const draftPrompt = ref('')
const gridPreviewVisible = ref(false)
const previewGrid = ref<{ idx: number; url: string } | null>(null)

const totalFramesDone = computed(() =>
  Object.values(gridStatus.value).filter((s) => s === 'done').length
)
const allFramesDone = computed(() =>
  totalFramesDone.value >= storyboards.value.length
)

function startEditPrompt(sbIdx: number, kfIdx: number, current: string) {
  editingPrompt.value = { sbIdx, kfIdx }
  draftPrompt.value = current
}

function handleSavePrompt() {
  if (editingPrompt.value) {
    const { sbIdx, kfIdx } = editingPrompt.value
    storyboards.value[sbIdx].keyFrames[kfIdx].prompt = draftPrompt.value
    editingPrompt.value = null
    ElMessage.success('Prompt 已保存')
  }
}

async function handleGenerateSingleFrame(sbIdx: number, kfIdx: number) {
  const kf = storyboards.value[sbIdx]?.keyFrames[kfIdx]
  if (!kf?.prompt?.trim()) {
    ElMessage.warning('该帧没有 prompt')
    return
  }
  const key = `${sbIdx}-${kfIdx}`
  frameGenerating.value = { ...frameGenerating.value, [key]: true }
  try {
    const res = await client.post('/ad/video/keyframe/generate', {
      prompt: kf.prompt,
      sceneIndex: sbIdx,
      frameIndex: kfIdx
    }) as any
    const data = res?.data ?? res
    frameImages.value = { ...frameImages.value, [key]: data?.url || data?.imageUrl }
    ElMessage.success(`帧 ${kfIdx + 1} 生成完成`)
  } catch {
    // mock fallback handled by server mock
  } finally {
    frameGenerating.value = { ...frameGenerating.value, [key]: false }
  }
}

async function handleGenerateGrid(sbIdx: number) {
  const sb = storyboards.value[sbIdx]
  if (!sb) return

  const prompts = sb.keyFrames.map((kf) => kf.prompt?.trim() || '')
  if (!prompts.some(Boolean)) {
    ElMessage.warning('该分镜所有帧均无 prompt')
    return
  }

  gridStatus.value = { ...gridStatus.value, [sbIdx]: 'generating' }
  try {
    const res = await client.post('/ad/video/keyframe/grid', {
      prompts,
      sceneIndex: sbIdx
    }) as any
    const data = res?.data ?? res
    gridImages.value = { ...gridImages.value, [sbIdx]: data?.url || data?.imageUrl }
    gridStatus.value = { ...gridStatus.value, [sbIdx]: 'done' }
    ElMessage.success(`分镜 ${sbIdx + 1} 宫格生成完成`)
  } catch {
    // Mock
    gridImages.value = {
      ...gridImages.value,
      [sbIdx]: `https://placehold.co/1280x960/6366F1/FFFFFF?text=Storyboard+${sbIdx + 1}+Grid`
    }
    gridStatus.value = { ...gridStatus.value, [sbIdx]: 'done' }
  }
}

async function handleGenerateAllGrids() {
  for (let i = 0; i < storyboards.value.length; i++) {
    if (gridStatus.value[i] === 'done') continue
    await handleGenerateGrid(i)
  }
}

async function handleRegenerateGrid(sbIdx: number) {
  const newGridImages = { ...gridImages.value }
  delete newGridImages[sbIdx]
  gridImages.value = newGridImages
  gridStatus.value = { ...gridStatus.value, [sbIdx]: 'idle' }
  await handleGenerateGrid(sbIdx)
}

function downloadGridImage(idx: number) {
  const url = gridImages.value[idx]
  if (!url) return
  const a = document.createElement('a')
  a.href = url
  a.download = `storyboard_${idx + 1}_grid.png`
  a.click()
}

// Sync across storyboard changes
watch(storyboards, () => {
  // Reset grid state when storyboards change
  if (subStep.value === 1) {
    gridStatus.value = {}
    gridImages.value = {}
    frameImages.value = {}
    frameGenerating.value = {}
  }
}, { deep: true })

// ── Sub-Step 2: Video Compositing ────────────────────────────────────
const duration = ref(30)
const ttsExpanded = ref(false)
const ttsLang = ref('zh-CN')
const ttsVoice = ref('zh-CN-XiaoxiaoNeural')
const ttsRate = ref(0)
const ttsPitch = ref(0)
const bgmExpanded = ref(false)
const bgmSelection = ref('none')
const bgmVolume = ref(70)
const narrationVolume = ref(100)
const generating = ref(false)
const progress = ref(0)
const progressStatus = ref<'success' | 'exception' | 'warning' | ''>('')
const generatedVideo = ref<any>(null)
const saving = ref(false)

const currentVoices = computed(() => ttsVoices[ttsLang.value] || ttsVoices['zh-CN'])

function simulateVideoProgress() {
  progress.value = 0
  progressStatus.value = ''
  const steps = [
    { p: 12, delay: 400 }, { p: 28, delay: 500 }, { p: 45, delay: 600 },
    { p: 62, delay: 500 }, { p: 78, delay: 600 }, { p: 90, delay: 500 }, { p: 96, delay: 400 }
  ]
  let i = 0
  const tick = () => {
    if (i >= steps.length) return
    progress.value = steps[i].p
    i++
    if (i < steps.length) setTimeout(tick, steps[i - 1].delay)
  }
  tick()
}

async function handleGenerateVideo() {
  generating.value = true
  simulateVideoProgress()

  try {
    const res = await client.post('/ad/video/generate', {
      storyboards: storyboards.value,
      gridImages: gridImages.value,
      duration: duration.value,
      settings: {
        ttsLang: ttsLang.value, ttsVoice: ttsVoice.value,
        ttsRate: ttsRate.value, ttsPitch: ttsPitch.value,
        bgmSelection: bgmSelection.value, bgmVolume: bgmVolume.value,
        narrationVolume: narrationVolume.value
      }
    }) as any
    const data = res?.data ?? res
    generatedVideo.value = { url: data?.url || data?.videoUrl, label: `广告视频 - ${duration.value}s` }
    progress.value = 100
    progressStatus.value = 'success'
    ElMessage.success('视频生成完成')
  } catch {
    const res = await client.post('/ad/video/generate', { duration: duration.value }) as any
    const data = res?.data ?? res
    generatedVideo.value = { url: data?.url || data?.videoUrl, label: `广告视频 - ${duration.value}s` }
    progress.value = 100
    progressStatus.value = 'success'
  } finally {
    generating.value = false
  }
}

function handleResetVideo() {
  generatedVideo.value = null
  progress.value = 0
  progressStatus.value = ''
}

async function saveVideo() {
  if (!generatedVideo.value?.url) return
  saving.value = true
  try {
    if (window.electronAPI && generatedVideo.value.url.startsWith('data:')) {
      const filename = `ad-video-${Date.now()}.mp4`
      const savedPath = await window.electronAPI.saveFile({ subDir: 'videos', filename, dataUrl: generatedVideo.value.url })
      ElMessage.success(`已保存到 ${savedPath}`)
    } else {
      const a = document.createElement('a')
      a.href = generatedVideo.value.url
      a.download = `ad-video-${Date.now()}.mp4`
      a.click()
      ElMessage.success('下载已开始')
    }
  } catch (err: any) {
    ElMessage.error('保存失败: ' + (err.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

function handleNext() {
  emit('next', {
    generatedVideo: generatedVideo.value,
    storyboards: storyboards.value,
    duration: duration.value
  })
}
</script>

<style scoped>
.ad-video-panel { /* container handled by parent */ }

/* ── Sub-steps bar ─────────────────────────────────────────────────── */
.sub-steps { margin-bottom: var(--space-6); }
.sub-steps :deep(.el-step__title) { font-size: var(--text-sm); font-weight: 600; }
.sub-steps :deep(.el-step__description) { font-size: var(--text-xs); }

/* ── Sub-panel ──────────────────────────────────────────────────────── */
.sub-panel { min-height: 360px; }

/* ── Card title row ─────────────────────────────────────────────────── */
.card-title-row { display: flex; align-items: center; gap: var(--space-2); font-weight: 600; }

/* ── Upload zone ───────────────────────────────────────────────────── */
.upload-zone {
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  text-align: center;
  cursor: pointer;
  transition: border-color var(--transition-fast), background var(--transition-fast);
  position: relative;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.upload-zone:hover { border-color: var(--color-primary); background: var(--color-primary-light); }
.upload-zone.has-image { border-color: var(--color-primary); padding: 0; min-height: auto; }
.upload-preview { max-height: 120px; width: 100%; }
.upload-remove { position: absolute; top: 4px; right: 4px; }
.upload-hint { font-size: var(--text-xs); color: var(--color-text-muted); margin: var(--space-2) 0 0; }
.hidden-input { display: none; }

/* ── Input row ──────────────────────────────────────────────────────── */
.input-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-3); margin: var(--space-4) 0; }
@media (max-width: 640px) { .input-row { grid-template-columns: 1fr; } }

/* ── Template grid ──────────────────────────────────────────────────── */
.template-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-2); }
.template-item {
  position: relative;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-2);
  background: var(--color-bg-card);
  cursor: pointer;
  transition: border-color var(--transition-fast);
  text-align: center;
  font-family: inherit;
}
.template-item:hover:not(:disabled) { border-color: var(--color-primary); }
.template-item.active { border-color: var(--color-primary); background: var(--color-primary-light); }
.template-item:disabled { opacity: 0.5; cursor: not-allowed; }
.template-thumb { height: 64px; display: flex; align-items: center; justify-content: center; background: var(--color-bg); border-radius: var(--radius-md); margin-bottom: var(--space-1); }
.template-name { font-size: var(--text-xs); font-weight: 500; color: var(--color-text-secondary); }
.template-check { position: absolute; top: 2px; right: 2px; }

/* ── Generate ───────────────────────────────────────────────────────── */
.generate-btn { width: 100%; height: 44px; font-weight: 600; font-size: var(--text-lg); margin-top: var(--space-4); }
.hint-text { font-size: var(--text-xs); color: var(--color-text-muted); text-align: center; margin: var(--space-2) 0 0; }

/* ── Error card ─────────────────────────────────────────────────────── */
.error-card { border-color: #EF4444; margin-top: var(--space-4); }
.error-content { display: flex; align-items: flex-start; gap: var(--space-3); }
.error-info { flex: 1; }
.error-title { margin: 0; font-weight: 600; color: #EF4444; font-size: var(--text-sm); }
.error-msg { margin: var(--space-1) 0 0; font-size: var(--text-xs); color: var(--color-text-secondary); }

/* ── Skeleton ───────────────────────────────────────────────────────── */
.skeleton-item { border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: var(--space-4); margin-bottom: var(--space-4); }
.skeleton-line { height: 12px; background: var(--color-border); border-radius: var(--radius-sm); margin-bottom: var(--space-2); animation: pulse 1.5s infinite; }
.skeleton-line.w-20 { width: 80px; }
.skeleton-line.w-full { width: 100%; }
.skeleton-line.w-3\/4 { width: 75%; }
.skeleton-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-2); margin-top: var(--space-3); }
.skeleton-cell { aspect-ratio: 16/9; background: var(--color-border); border-radius: var(--radius-md); animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

/* ── Result area ────────────────────────────────────────────────────── */
.result-area { margin-top: var(--space-5); }
.result-title { font-size: var(--text-sm); font-weight: 600; display: flex; align-items: center; gap: var(--space-2); margin-bottom: var(--space-3); }
.storyboard-list { display: flex; flex-direction: column; gap: var(--space-3); }
.storyboard-card { border: 1px solid var(--color-border); }
.storyboard-card :deep(.el-card__header) { padding: var(--space-2) var(--space-3); background: var(--color-bg); }
.storyboard-card :deep(.el-card__body) { padding: var(--space-3); }
.sb-header { display: flex; align-items: center; justify-content: space-between; }
.sb-body { }
.sb-desc { font-size: var(--text-sm); color: var(--color-text-secondary); line-height: var(--leading-relaxed); cursor: pointer; padding: var(--space-1); margin: 0 0 var(--space-3); border: 1px solid transparent; border-radius: var(--radius-sm); transition: border-color var(--transition-fast); }
.sb-desc:hover { border-color: var(--color-primary); border-style: dashed; }
.edit-actions { display: flex; gap: var(--space-1); margin-top: var(--space-2); }
.kf-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-2); }
.kf-cell { aspect-ratio: 16/9; background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-md); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: var(--space-1); }
.kf-label { font-size: 10px; color: var(--color-text-muted); }

/* ── Empty card ─────────────────────────────────────────────────────── */
.empty-card { margin-top: var(--space-4); border-style: dashed; }
.empty-content { text-align: center; padding: var(--space-8) 0; }
.empty-title { font-size: var(--text-sm); color: var(--color-text-secondary); margin: var(--space-3) 0 var(--space-1); }
.empty-desc { font-size: var(--text-xs); color: var(--color-text-muted); margin: 0; }

/* ── Summary card (Sub-step 1 & 2) ──────────────────────────────────── */
.summary-card { margin-bottom: var(--space-4); border-left: 3px solid var(--color-primary); }
.summary-card :deep(.el-card__body) { padding: var(--space-3) var(--space-4); }
.summary-content { display: flex; align-items: center; gap: var(--space-4); }
.summary-icon { width: 40px; height: 40px; border-radius: var(--radius-lg); background: var(--color-primary-light); display: flex; align-items: center; justify-content: center; color: var(--color-primary); flex-shrink: 0; }
.summary-title { font-family: var(--font-heading); margin: 0 0 2px; font-size: var(--text-lg); font-weight: 600; }
.summary-desc { margin: 0; font-size: var(--text-xs); color: var(--color-text-secondary); }
.summary-progress { font-family: var(--font-mono); }

/* ── Storyboard tabs ────────────────────────────────────────────────── */
.sb-tabs { margin-bottom: var(--space-3); }
.sb-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.tab-label { display: flex; align-items: center; gap: var(--space-1); }

/* ── Frame card ──────────────────────────────────────────────────────── */
.frame-card { border: 1px solid var(--color-border); }
.frame-card.border-success { border-color: #22C55E; }
.frame-card :deep(.el-card__header) { padding: var(--space-2) var(--space-3); background: var(--color-bg); }
.frame-card :deep(.el-card__body) { padding: var(--space-3); }
.frame-card-header { display: flex; align-items: center; justify-content: space-between; }
.frame-card-left { display: flex; align-items: center; gap: var(--space-2); min-width: 0; }
.frame-desc { font-size: var(--text-xs); color: var(--color-text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.frame-grid-layout { display: grid; grid-template-columns: 2fr 3fr; gap: var(--space-4); }
@media (max-width: 768px) { .frame-grid-layout { grid-template-columns: 1fr; } }

/* ── Keyframe prompts ────────────────────────────────────────────────── */
.frame-prompts { display: flex; flex-direction: column; gap: var(--space-2); }
.kf-prompt-item { border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; }
.kf-prompt-preview { width: 100%; height: 80px; background: var(--color-bg); }
.kf-prompt-thumb { width: 100%; height: 100%; }
.kf-prompt-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.kf-prompt-btn { width: 100%; text-align: left; padding: var(--space-2); border: none; border-top: 1px solid var(--color-border); background: transparent; cursor: pointer; font-family: inherit; transition: background var(--transition-fast); }
.kf-prompt-btn:hover { background: var(--color-bg); }
.kf-prompt-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 2px; }
.kf-prompt-text { font-size: 10px; color: var(--color-text-muted); margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.kf-prompt-actions { padding: 0 var(--space-2) var(--space-2); display: flex; gap: var(--space-1); }
.kf-prompt-actions .el-button { flex: 1; }

/* ── Grid preview ────────────────────────────────────────────────────── */
.frame-grid-preview { display: flex; flex-direction: column; }
.grid-loading { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: var(--space-3); color: var(--color-text-muted); font-size: var(--text-xs); }
.grid-preview-wrap { cursor: pointer; border-radius: var(--radius-md); overflow: hidden; border: 1px solid var(--color-border); margin-bottom: var(--space-2); }
.grid-preview-img { width: 100%; aspect-ratio: 4/3; }
.grid-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: var(--space-3); color: var(--color-text-muted); font-size: var(--text-xs); }
.grid-actions { display: flex; gap: var(--space-2); margin-bottom: var(--space-2); }
.grid-generate-btn { width: 100%; }

/* ── Prompt editor ───────────────────────────────────────────────────── */
.prompt-editor { margin-top: var(--space-3); padding-top: var(--space-3); border-top: 1px solid var(--color-border-light); }
.prompt-editor-row { display: flex; gap: var(--space-2); margin-top: var(--space-1); }
.prompt-editor-actions { display: flex; gap: var(--space-1); flex-shrink: 0; align-items: flex-start; }

/* ── Grid preview dialog ─────────────────────────────────────────────── */
.grid-preview-dialog-img { width: 100%; max-height: 70vh; }

/* ── Field ───────────────────────────────────────────────────────────── */
.field { display: flex; flex-direction: column; gap: var(--space-1); margin-bottom: var(--space-3); }
.field-label { font-size: var(--text-xs); font-weight: 500; color: var(--color-text-secondary); }
.field-label-row { display: flex; justify-content: space-between; align-items: center; }
.field-value { font-family: var(--font-mono); font-size: 10px; color: var(--color-text-muted); }
.field-input { width: 100%; }

/* ── Config grid (Sub-step 2) ────────────────────────────────────────── */
.config-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-5); margin-bottom: var(--space-5); }
@media (max-width: 720px) { .config-grid { grid-template-columns: 1fr; } }
.config-col { display: flex; flex-direction: column; gap: var(--space-4); }
.config-card { border: 1px solid var(--color-border); }
.config-card :deep(.el-card__header) { padding: var(--space-3) var(--space-4); background: var(--color-bg); border-bottom: 1px solid var(--color-border-light); }
.config-card :deep(.el-card__body) { padding: var(--space-4); }
.config-card-title { font-size: var(--text-sm); font-weight: 600; color: var(--color-text); }
.config-card-title-row { display: flex; align-items: center; gap: var(--space-2); }

/* ── Collapsible ─────────────────────────────────────────────────────── */
.collapse-header { display: flex; align-items: center; justify-content: space-between; cursor: pointer; user-select: none; }
.collapse-title { display: flex; align-items: center; gap: var(--space-2); font-size: var(--text-sm); font-weight: 600; color: var(--color-text); }
.collapse-chevron { color: var(--color-text-muted); transition: transform var(--transition-fast); }
.collapse-body { display: flex; flex-direction: column; gap: var(--space-3); }

/* ── Storyboard summary ──────────────────────────────────────────────── */
.storyboard-summary { display: flex; flex-direction: column; gap: var(--space-2); }
.sb-summary-item { display: flex; align-items: center; gap: var(--space-2); font-size: var(--text-xs); color: var(--color-text-secondary); }
.sb-summary-desc { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sb-summary-frames { font-family: var(--font-mono); font-size: 10px; color: var(--color-text-muted); flex-shrink: 0; }

/* ── Progress ────────────────────────────────────────────────────────── */
.progress-wrap { margin-top: var(--space-1); }
.progress-text { margin: var(--space-2) 0 0; font-size: var(--text-xs); color: var(--color-text-muted); text-align: center; }

/* ── Result card ─────────────────────────────────────────────────────── */
.result-card { margin-bottom: var(--space-5); border-top: 3px solid var(--color-success); }
.result-header { display: flex; align-items: center; gap: var(--space-2); font-weight: 600; }
.result-body { display: flex; flex-direction: column; gap: var(--space-4); }
.video-player-wrap { aspect-ratio: 16/9; background: #000; border-radius: var(--radius-lg); overflow: hidden; }
.result-player { width: 100%; height: 100%; object-fit: contain; }
.result-actions { display: flex; gap: var(--space-2); }

/* ── Sub-nav ─────────────────────────────────────────────────────────── */
.sub-nav { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-6); padding-top: var(--space-4); border-top: 1px solid var(--color-border-light); }
.sub-nav-right { display: flex; gap: var(--space-2); }

/* ── Input card ──────────────────────────────────────────────────────── */
.input-card { border: 1px solid var(--color-border); }
.input-card :deep(.el-card__header) { padding: var(--space-3) var(--space-4); background: var(--color-bg); border-bottom: 1px solid var(--color-border-light); }
.input-card :deep(.el-card__body) { padding: var(--space-4); }
</style>
