<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black bg-opacity-75 z-[9999] flex items-center justify-center p-4"
    @click.self="handleCancel"
  >
    <div class="bg-white rounded-2xl max-w-4xl w-full max-h-[90vh] flex flex-col overflow-hidden">
      <!-- 标题栏 -->
      <div class="flex items-center justify-between p-6 border-b border-gray-200">
        <h3 class="text-xl font-bold text-gray-800">裁剪图片</h3>
        <button
          type="button"
          class="text-gray-400 hover:text-gray-600 text-2xl leading-none"
          @click="handleCancel"
        >
          ×
        </button>
      </div>

      <!-- 裁剪区域 -->
      <div class="flex-1 overflow-auto p-6">
        <div class="relative inline-block max-w-full">
          <canvas
            ref="canvasRef"
            class="max-w-full h-auto cursor-move border-2 border-gray-300 rounded-lg"
            @mousedown="startDrag"
            @mousemove="onDrag"
            @mouseup="endDrag"
            @mouseleave="endDrag"
            @wheel="handleWheel"
          ></canvas>
        </div>
        
        <!-- 操作提示 -->
        <div class="mt-4 text-sm text-gray-600 space-y-1">
          <p>• 拖动图片调整位置</p>
          <p>• 滚动鼠标滚轮缩放图片</p>
          <p>• 裁剪框固定为 {{ aspectRatioText }} 比例</p>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="flex items-center justify-end gap-3 p-6 border-t border-gray-200">
        <button
          type="button"
          class="px-6 py-2.5 rounded-xl border-2 border-gray-300 text-gray-700 font-medium hover:bg-gray-50 transition-colors"
          @click="handleCancel"
        >
          取消
        </button>
        <button
          type="button"
          class="px-6 py-2.5 rounded-xl bg-[#FF8C42] text-white font-medium hover:bg-[#E67A2A] transition-colors"
          @click="handleConfirm"
        >
          确认裁剪
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'

interface Props {
  visible: boolean
  imageFile: File | null
  aspectRatio?: number
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', file: File): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  aspectRatio: 4 / 3
})
const emit = defineEmits<Emits>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const image = ref<HTMLImageElement | null>(null)

// 图片位置和缩放
const offsetX = ref(0)
const offsetY = ref(0)
const scale = ref(1)

// 拖拽状态
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragStartOffsetX = ref(0)
const dragStartOffsetY = ref(0)

// 裁剪框尺寸（根据比例计算）
const cropWidth = ref(600)
const cropHeight = ref(450)
const minScale = ref(1) // 最小缩放比例，确保图片始终覆盖裁剪框

const aspectRatioText = computed(() => {
  const r = props.aspectRatio
  if (Math.abs(r - 1) < 0.01) return '1:1'
  if (Math.abs(r - 4/3) < 0.01) return '4:3'
  if (Math.abs(r - 16/9) < 0.01) return '16:9'
  return '固定'
})

const loadImage = async () => {
  if (!props.imageFile || !canvasRef.value) return

  const img = new Image()
  const reader = new FileReader()

  reader.onload = (e) => {
    img.onload = () => {
      image.value = img
      initCanvas()
    }
    img.src = e.target?.result as string
  }

  reader.readAsDataURL(props.imageFile)
}

const initCanvas = () => {
  if (!canvasRef.value || !image.value) return

  const canvas = canvasRef.value
  const img = image.value

  // 设置裁剪框尺寸（最大 800，根据比例计算高度）
  const maxWidth = Math.min(800, window.innerWidth - 100)
  const maxHeight = maxWidth / props.aspectRatio

  cropWidth.value = maxWidth
  cropHeight.value = maxHeight

  canvas.width = cropWidth.value
  canvas.height = cropHeight.value

  // 计算初始缩放，让图片完全覆盖裁剪框
  const scaleX = cropWidth.value / img.width
  const scaleY = cropHeight.value / img.height
  minScale.value = Math.max(scaleX, scaleY) // 记录最小缩放比例
  scale.value = minScale.value

  // 居中图片
  offsetX.value = (cropWidth.value - img.width * scale.value) / 2
  offsetY.value = (cropHeight.value - img.height * scale.value) / 2

  drawCanvas()
}

const drawCanvas = () => {
  if (!canvasRef.value || !image.value) return

  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // 绘制图片
  ctx.drawImage(
    image.value,
    offsetX.value,
    offsetY.value,
    image.value.width * scale.value,
    image.value.height * scale.value
  )

  // 绘制裁剪框边框
  ctx.strokeStyle = '#FF8C42'
  ctx.lineWidth = 3
  ctx.strokeRect(0, 0, canvas.width, canvas.height)

  // 绘制网格线（九宫格）
  ctx.strokeStyle = 'rgba(255, 140, 66, 0.3)'
  ctx.lineWidth = 1
  
  // 垂直线
  ctx.beginPath()
  ctx.moveTo(canvas.width / 3, 0)
  ctx.lineTo(canvas.width / 3, canvas.height)
  ctx.moveTo((canvas.width / 3) * 2, 0)
  ctx.lineTo((canvas.width / 3) * 2, canvas.height)
  ctx.stroke()

  // 水平线
  ctx.beginPath()
  ctx.moveTo(0, canvas.height / 3)
  ctx.lineTo(canvas.width, canvas.height / 3)
  ctx.moveTo(0, (canvas.height / 3) * 2)
  ctx.lineTo(canvas.width, (canvas.height / 3) * 2)
  ctx.stroke()
}

const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  dragStartX.value = e.clientX
  dragStartY.value = e.clientY
  dragStartOffsetX.value = offsetX.value
  dragStartOffsetY.value = offsetY.value
}

const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return

  const dx = e.clientX - dragStartX.value
  const dy = e.clientY - dragStartY.value

  offsetX.value = dragStartOffsetX.value + dx
  offsetY.value = dragStartOffsetY.value + dy

  drawCanvas()
}

const endDrag = () => {
  isDragging.value = false
}

const handleWheel = (e: WheelEvent) => {
  e.preventDefault()

  const delta = e.deltaY > 0 ? 0.9 : 1.1
  const newScale = scale.value * delta

  // 限制缩放范围，不能小于最小缩放比例，确保图片始终覆盖裁剪框
  if (newScale < minScale.value || newScale > 5) return

  // 以鼠标位置为中心缩放
  const rect = canvasRef.value!.getBoundingClientRect()
  const mouseX = e.clientX - rect.left
  const mouseY = e.clientY - rect.top

  offsetX.value = mouseX - (mouseX - offsetX.value) * (newScale / scale.value)
  offsetY.value = mouseY - (mouseY - offsetY.value) * (newScale / scale.value)

  scale.value = newScale
  drawCanvas()
}

const getCroppedImage = (): Promise<File> => {
  return new Promise((resolve, reject) => {
    if (!canvasRef.value || !image.value) {
      reject(new Error('Canvas or image not ready'))
      return
    }

    // 创建一个新的 canvas 用于输出裁剪结果
    const outputCanvas = document.createElement('canvas')
    outputCanvas.width = cropWidth.value
    outputCanvas.height = cropHeight.value
    const ctx = outputCanvas.getContext('2d')

    if (!ctx) {
      reject(new Error('Failed to get canvas context'))
      return
    }

    // 先填充白色背景，防止透明区域显示为黑色
    ctx.fillStyle = '#FFFFFF'
    ctx.fillRect(0, 0, outputCanvas.width, outputCanvas.height)

    // 绘制裁剪后的图片
    ctx.drawImage(
      image.value,
      offsetX.value,
      offsetY.value,
      image.value.width * scale.value,
      image.value.height * scale.value
    )

    // 转换为 Blob 然后转为 File
    outputCanvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error('Failed to create blob'))
        return
      }

      const fileName = props.imageFile?.name || 'cropped-image.jpg'
      const file = new File([blob], fileName, { type: 'image/jpeg' })
      resolve(file)
    }, 'image/jpeg', 0.95)
  })
}

const handleConfirm = async () => {
  try {
    const croppedFile = await getCroppedImage()
    emit('confirm', croppedFile)
    emit('update:visible', false)
  } catch (error) {
    console.error('裁剪失败:', error)
  }
}

const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
}

watch(() => props.visible, async (newVal) => {
  if (newVal) {
    await nextTick()
    loadImage()
  }
})
</script>

<style scoped>
canvas {
  display: block;
  user-select: none;
}
</style>
