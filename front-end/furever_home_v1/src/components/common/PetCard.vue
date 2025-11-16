<template>
  <div :class="wrapperClass">
    <div class="relative" :class="imageWrapperClass">
      <div :class="['h-40 flex items-center justify-center text-sm font-medium', bgClass]">
        <span>{{ name }}的照片</span>
      </div>
      <span
        v-if="statusLabel"
        :class="['absolute top-2 left-2 text-xs font-semibold px-2 py-0.5 rounded-full', statusClass]"
        @click="$emit('badge-click')"
      >[{{ statusLabel }}]</span>
    </div>
    <div class="p-4 flex flex-col flex-grow">
      <h3 :class="['text-xl font-bold', titleClass]">{{ name }}</h3>
      <p v-if="desc" class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ desc }}</p>
      <div class="flex justify-between mt-3 text-sm">
        <div>
          <p class="text-gray-400 dark:text-gray-500">性别</p>
          <p class="font-medium text-gray-700 dark:text-gray-300">{{ gender || '未知' }}</p>
        </div>
        <div>
          <p class="text-gray-400 dark:text-gray-500">状态</p>
          <p class="font-medium" :class="statusColorClass">{{ statusText }}</p>
        </div>
      </div>
      <div v-if="days !== undefined" :class="bottomBarClass">{{ variant==='short' ? '已短期领养 ' : '已长期领养 ' }}{{ days }} 天</div>
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  name: string;
  desc?: string;
  gender?: string;
  status?: string; // approved / pending / rejected / available / adopted
  statusLabel?: string; // 已通过 / 审核中 / 已拒绝 等
  days?: number;
  variant?: 'short' | 'long' | 'list';
  titleClass?: string;
  bgClass?: string; // 背景色样式（上方图片占位）
}

const props = defineProps<Props>();
const emit = defineEmits<{ (e:'badge-click'):void }>();

const statusText = computed(() => {
  if (props.statusLabel) return props.statusLabel;
  switch (props.status) {
    case 'available': return '可领养';
    case 'pending': return '待审核';
    case 'adopted': return '已领养';
    case 'approved': return '已通过';
    case 'rejected': return '已拒绝';
    default: return '未知';
  }
});

const statusClass = computed(() => {
  switch (props.status) {
    case 'approved': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
    case 'pending': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
    case 'rejected': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300 cursor-pointer';
    case 'available': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
    case 'adopted': return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
    default: return 'bg-gray-100 text-gray-800';
  }
});

const statusColorClass = computed(() => {
  switch (props.status) {
    case 'approved': return 'text-green-600 dark:text-green-400';
    case 'pending': return 'text-blue-600 dark:text-blue-400';
    case 'rejected': return 'text-red-600 dark:text-red-400';
    case 'available': return 'text-green-600 dark:text-green-400';
    case 'adopted': return 'text-gray-600 dark:text-gray-400';
    default: return 'text-gray-600 dark:text-gray-400';
  }
});

const wrapperClass = computed(() => 'bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden flex flex-col');
const imageWrapperClass = computed(() => 'relative');
const bottomBarClass = computed(() => {
  if (props.variant === 'short') return 'mt-4 bg-orange-50 dark:bg-gray-700/50 text-center py-2 text-sm text-orange-700 dark:text-orange-300 font-medium';
  if (props.variant === 'long') return 'mt-4 bg-pink-50 dark:bg-gray-700/50 text-center py-2 text-sm text-pink-700 dark:text-pink-300 font-medium';
  return 'mt-4';
});

const titleClass = computed(() => props.titleClass || 'text-gray-800 dark:text-gray-100');
const bgClass = computed(() => props.bgClass || 'bg-gray-200 dark:bg-gray-700 text-gray-500 dark:text-gray-400');
</script>

<style scoped>
</style>
