<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black/50 z-[3000] flex items-center justify-center"
    @click="handleCancel"
  >
    <div
      class="bg-white rounded-xl p-8 max-w-[400px] w-[90%] text-center shadow-lg"
      @click.stop
    >
      <h3 class="text-[#FF8C00] mb-4 text-xl font-bold">{{ title }}</h3>
      <p class="mb-5 leading-relaxed text-[#333333]">{{ message }}</p>
      <div class="flex justify-center gap-2.5">
        <button
          class="px-5 py-2.5 bg-white text-[#666666] border border-gray-300 rounded-lg font-semibold transition-all hover:bg-gray-50"
          @click="handleCancel"
        >
          取消
        </button>
        <button
          class="px-5 py-2.5 bg-[#FF8C00] text-white rounded-lg font-semibold transition-all hover:bg-[#e6722a]"
          @click="handleConfirm"
        >
          确定
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  visible: boolean
  title: string
  message: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}
</script>

