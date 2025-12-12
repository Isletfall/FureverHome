<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-slate-900/70 z-[9999] flex items-center justify-center p-4"
    @click.self="handleClose"
  >
    <div class="absolute inset-0 bg-slate-900/80 backdrop-blur-sm"></div>
    <div class="relative z-10 w-full max-w-sm">
      <div class="rounded-2xl bg-white dark:bg-[#1A1E26] shadow-2xl p-8 text-center">
        <div class="size-20 mx-auto rounded-full bg-red-100 text-red-500 flex items-center justify-center mb-5">
          <span class="material-symbols-outlined text-5xl" style="font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 48;">error</span>
        </div>
        <h2 class="text-xl font-bold text-slate-900 dark:text-white mb-2">操作失败</h2>
        <p class="text-sm text-slate-500 dark:text-slate-400 mb-6">{{ message || '操作失败，请稍后重试' }}</p>
        <button
          class="w-full px-4 py-2.5 rounded-lg bg-red-500 text-white font-medium hover:bg-red-600 transition"
          @click="handleConfirm"
        >
          确定
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  visible: boolean;
  message?: string;
}>();

const emit = defineEmits<{
  close: [];
  confirm: [];
}>();

function handleClose() {
  emit('close');
}

function handleConfirm() {
  emit('confirm');
  handleClose();
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 24;
}
</style>

