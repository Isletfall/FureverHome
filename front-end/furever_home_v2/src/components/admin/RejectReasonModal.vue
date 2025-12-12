<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-slate-900/70 z-[9999] flex items-center justify-center p-4"
    @click.self="handleCancel"
  >
    <div class="absolute inset-0 bg-slate-900/80 backdrop-blur-sm"></div>
    <div class="relative z-10 w-full max-w-md">
      <div class="rounded-2xl bg-white dark:bg-[#1A1E26] shadow-2xl p-6">
        <div class="flex items-start justify-between mb-4">
          <h2 class="text-lg font-bold text-slate-900 dark:text-white">{{ title || '请输入拒绝原因' }}</h2>
          <button
            class="text-slate-400 hover:text-slate-600 dark:hover:text-white transition rounded-lg p-1 hover:bg-slate-100 dark:hover:bg-slate-800"
            @click="handleCancel"
          >
            <span class="material-symbols-outlined text-xl">close</span>
          </button>
        </div>
        <p class="text-sm text-slate-500 dark:text-slate-400 mb-4">{{ message || '请输入拒绝原因（可选）:' }}</p>
        <textarea
          v-model="reason"
          class="w-full px-4 py-2.5 rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-[#1A1E26] text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
          rows="4"
          placeholder="请输入拒绝原因..."
        ></textarea>
        <div class="flex gap-3 mt-6">
          <button
            type="button"
            class="flex-1 px-4 py-2.5 rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-[#1A1E26] text-gray-700 dark:text-gray-300 font-medium hover:bg-gray-50 dark:hover:bg-gray-800 transition"
            @click="handleCancel"
          >
            取消
          </button>
          <button
            type="button"
            class="flex-1 px-4 py-2.5 rounded-lg bg-blue-500 text-white font-medium hover:bg-blue-600 transition"
            @click="handleConfirm"
          >
            确定
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  visible: boolean;
  title?: string;
  message?: string;
}>();

const emit = defineEmits<{
  close: [];
  confirm: [reason: string];
  cancel: [];
}>();

const reason = ref('');

watch(() => props.visible, (newVal) => {
  if (newVal) {
    reason.value = '';
  }
});

function handleCancel() {
  emit('cancel');
  emit('close');
}

function handleConfirm() {
  // 只触发 confirm，让父组件自行关闭弹窗，避免 confirm 和 close 同时触发导致状态被重置
  console.log('[RejectReasonModal] handleConfirm 被调用', { reason: reason.value });
  console.log('[RejectReasonModal] 准备触发 confirm 事件');
  try {
    emit('confirm', reason.value);
    console.log('[RejectReasonModal] confirm 事件已触发');
  } catch (error) {
    console.error('[RejectReasonModal] 触发 confirm 事件时出错', error);
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 24;
}
</style>

