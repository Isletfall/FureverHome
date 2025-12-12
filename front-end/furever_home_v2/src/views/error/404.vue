<template>
  <div class="min-h-[60vh] flex flex-col items-center justify-center text-center px-6">
    <div class="flex items-center justify-center w-24 h-24 rounded-full bg-orange-100 dark:bg-orange-900/40 mb-8">
      <span class="material-icons text-primary text-5xl">report_problem</span>
    </div>
    <h1 class="text-4xl font-bold text-gray-800 dark:text-gray-100 mb-4">404 未找到页面</h1>
    <p class="text-gray-600 dark:text-gray-400 max-w-xl leading-relaxed mb-6">
      你访问的页面不存在，可能已被移动或删除。请检查链接是否正确，或返回首页继续浏览。
    </p>
    <div class="flex gap-4">
      <RouterLink to="/home" class="px-6 py-3 rounded-md bg-primary text-white font-medium shadow hover:bg-orange-500 transition-colors">返回首页</RouterLink>
      <button type="button" @click="goBack" class="px-6 py-3 rounded-md bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors">上一页</button>
    </div>
    <div class="mt-10 text-xs text-gray-400 dark:text-gray-500">
      如果你认为这是一个错误，请稍后重试或联系管理员。
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
const router = useRouter();
function goBack(){ router.back(); }
</script>

<style scoped>
</style>
