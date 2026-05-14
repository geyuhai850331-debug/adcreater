<template>
  <button
    class="theme-toggle"
    :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
    @click="store.toggleTheme()"
  >
    <el-icon :size="18">
      <Sunny v-if="isDark" />
      <Moon v-else />
    </el-icon>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Sunny, Moon } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const store = useThemeStore()
const isDark = computed(() => store.theme === 'dark')
</script>

<style scoped>
.theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-sidebar);
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.theme-toggle:hover {
  background: var(--color-bg-sidebar-hover);
  color: var(--color-text-sidebar-active);
}
</style>
