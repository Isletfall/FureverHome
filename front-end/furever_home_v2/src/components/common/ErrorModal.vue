<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black/50 z-[3000] flex items-center justify-center"
    @click="handleClose"
  >
    <div
      class="bg-white rounded-xl p-8 max-w-[400px] w-[90%] text-center shadow-lg"
      @click.stop
    >
      <h3 class="text-[#FF8C00] mb-4 text-xl font-bold">{{ title }}</h3>
      <p class="mb-5 leading-relaxed text-[#333333]">{{ message }}</p>
      <button
        class="px-5 py-2.5 bg-[#FF8C00] text-white rounded-lg font-semibold transition-all hover:bg-[#e6722a]"
        @click="handleClose"
      >
        确定
      </button>
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
  close: []
}>()

const handleClose = () => {
  emit('close')
}
</script>

