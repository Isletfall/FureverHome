<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black/90 z-[4000] flex items-center justify-center p-4"
    @click="handleClose"
  >
    <div class="relative max-w-[90vw] max-h-[90vh] flex items-center justify-center" @click.stop>
      <!-- 关闭按钮 -->
      <button
        class="absolute top-4 right-4 z-10 w-10 h-10 flex items-center justify-center bg-white/20 hover:bg-white/30 rounded-full text-white transition-colors"
        @click="handleClose"
      >
        <i class="fa-solid fa-times text-xl"></i>
      </button>

      <!-- 上一张按钮 -->
      <button
        v-if="images.length > 1 && currentIndex > 0"
        class="absolute left-4 z-10 w-12 h-12 flex items-center justify-center bg-white/20 hover:bg-white/30 rounded-full text-white transition-colors"
        @click.stop="prevImage"
      >
        <i class="fa-solid fa-chevron-left text-xl"></i>
      </button>

      <!-- 下一张按钮 -->
      <button
        v-if="images.length > 1 && currentIndex < images.length - 1"
        class="absolute right-4 z-10 w-12 h-12 flex items-center justify-center bg-white/20 hover:bg-white/30 rounded-full text-white transition-colors"
        @click.stop="nextImage"
      >
        <i class="fa-solid fa-chevron-right text-xl"></i>
      </button>

      <!-- 图片 -->
      <img
        :src="images[currentIndex]"
        :alt="`图片 ${currentIndex + 1}`"
        class="max-w-full max-h-[90vh] object-contain rounded-lg"
        @click.stop
      />

      <!-- 图片计数器 -->
      <div
        v-if="images.length > 1"
        class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-black/50 text-white px-4 py-2 rounded-full text-sm"
      >
        {{ currentIndex + 1 }} / {{ images.length }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'

interface Props {
  visible: boolean
  images: string[]
  initialIndex?: number
}

const props = withDefaults(defineProps<Props>(), {
  initialIndex: 0
})

const emit = defineEmits<{
  close: []
}>()

const currentIndex = ref(props.initialIndex)

watch(() => props.initialIndex, (newIndex) => {
  currentIndex.value = newIndex
})

watch(() => props.visible, (newVisible) => {
  if (newVisible) {
    currentIndex.value = props.initialIndex
    // 禁用 body 滚动
    document.body.style.overflow = 'hidden'
  } else {
    // 恢复 body 滚动
    document.body.style.overflow = ''
  }
})

const handleClose = () => {
  emit('close')
  document.body.style.overflow = ''
}

const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
  }
}

// 键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  if (!props.visible) return
  
  if (e.key === 'Escape') {
    handleClose()
  } else if (e.key === 'ArrowLeft') {
    prevImage()
  } else if (e.key === 'ArrowRight') {
    nextImage()
  }
}

// 监听键盘事件
onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('keydown', handleKeydown)
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('keydown', handleKeydown)
    document.body.style.overflow = ''
  }
})
</script>

