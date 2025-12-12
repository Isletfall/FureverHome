<template>
  <transition name="fade-slide">
    <div
      v-if="showToast"
      class="fixed top-4 left-1/2 transform -translate-x-1/2 z-[4000] bg-white shadow-xl rounded-xl px-4 py-3 flex items-start gap-3 border cursor-pointer transition-all hover:shadow-2xl"
      style="min-width: 280px; max-width: 500px; border-color: #E5E7EB;"
      @click="handleClick"
    >
      <div class="w-9 h-9 rounded-full flex items-center justify-center text-lg flex-shrink-0" style="background-color: #E5F3FF; color: #2563EB;">
        <i class="fa-solid fa-bell"></i>
      </div>
      <div class="flex-1">
        <div class="text-sm font-semibold" style="color: #111;">{{ title }}</div>
        <div class="text-xs mt-1" style="color: #4B5563;">{{ content }}</div>
      </div>
      <button 
        class="text-gray-400 hover:text-gray-600 text-xl leading-none font-bold w-6 h-6 flex items-center justify-center transition-colors" 
        @click.stop="hideToast"
        title="关闭"
      >×</button>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

interface Props {
  visible: boolean
  title: string
  content: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'close': []
  'click': []
}>()

const router = useRouter()

// 直接使用 props，确保响应式
const showToast = computed(() => props.visible)

const hideToast = () => {
  emit('update:visible', false)
  emit('close')
}

const handleClick = () => {
  // 跳转到用户中心首页（我的主页）
  router.push({ path: '/user-center', query: { menu: 'home' } })
  // 关闭弹窗
  hideToast()
  // 触发点击事件
  emit('click')
}
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translate(-50%, -20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}
</style>

