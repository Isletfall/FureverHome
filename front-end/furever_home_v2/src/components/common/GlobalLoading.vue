<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="fixed inset-0 z-[9998] flex items-center justify-center bg-slate-900/40 pointer-events-none"
    >
      <div
        class="pointer-events-auto flex items-center gap-3 rounded-2xl bg-white/90 dark:bg-[#1A1E26] px-5 py-3 shadow-xl border border-slate-200/80 dark:border-slate-700"
      >
        <span
          class="inline-block h-5 w-5 animate-spin rounded-full border-2 border-blue-500 border-t-transparent"
        ></span>
        <span class="text-sm text-slate-700 dark:text-slate-200">
          正在请求中，请稍候...
        </span>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'

const visible = ref(false)
let timer: number | null = null
let pendingCount = 0

const showWithDelay = () => {
  // 延迟 300ms 再显示，避免闪烁
  if (timer != null) return
  timer = window.setTimeout(() => {
    visible.value = pendingCount > 0
    timer = null
  }, 300)
}

const hideNow = () => {
  if (timer != null) {
    clearTimeout(timer)
    timer = null
  }
  visible.value = false
}

function handleStart(event: Event) {
  const detail = (event as CustomEvent).detail as { pending?: number } | undefined
  pendingCount = typeof detail?.pending === 'number' ? detail.pending : pendingCount + 1
  showWithDelay()
}

function handleEnd(event: Event) {
  const detail = (event as CustomEvent).detail as { pending?: number } | undefined
  pendingCount = typeof detail?.pending === 'number' ? detail.pending : Math.max(0, pendingCount - 1)
  if (pendingCount <= 0) {
    hideNow()
  }
}

onMounted(() => {
  window.addEventListener('http-request-start', handleStart)
  window.addEventListener('http-request-end', handleEnd)
})

onBeforeUnmount(() => {
  window.removeEventListener('http-request-start', handleStart)
  window.removeEventListener('http-request-end', handleEnd)
  hideNow()
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease-out;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>


