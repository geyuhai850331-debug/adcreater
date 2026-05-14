<template>
  <router-view />
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'

// Initialize theme store early — applies data-theme attribute on boot
useThemeStore()
</script>

<style>
@import '@/styles/design-tokens.css';
</style>
