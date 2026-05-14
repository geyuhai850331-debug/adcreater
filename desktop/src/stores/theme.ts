import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type Theme = 'dark' | 'light'

const STORAGE_KEY = 'adcreater-theme'
const DEFAULT_THEME: Theme = 'dark'

function getInitialTheme(): Theme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'dark' || stored === 'light') return stored
  } catch { /* localStorage blocked */ }
  return DEFAULT_THEME
}

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<Theme>(getInitialTheme())

  function applyTheme(t: Theme) {
    document.documentElement.setAttribute('data-theme', t)
  }

  function toggleTheme() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  function setTheme(t: Theme) {
    theme.value = t
  }

  // Persist to localStorage + apply to DOM on every change
  watch(theme, (t) => {
    try { localStorage.setItem(STORAGE_KEY, t) } catch { /* ignore */ }
    applyTheme(t)
  }, { immediate: true })

  return { theme, toggleTheme, setTheme }
})
