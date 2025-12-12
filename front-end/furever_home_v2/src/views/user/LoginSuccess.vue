<template>
  <div class="relative flex min-h-screen w-full flex-col items-center justify-center p-4 sm:p-6">
    <main class="relative z-10 flex w-full max-w-[420px] flex-col items-center justify-center bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-16 text-center">

      <div class="w-24 h-24 bg-success-bg dark:bg-green-900/30 rounded-full flex items-center justify-center mb-8">
        <svg class="w-12 h-12 text-success-text" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"></path>
        </svg>
      </div>

      <div class="space-y-3">
        <h1 class="text-3xl font-extrabold text-stone-900 dark:text-white tracking-tight">
          登录成功！
        </h1>
        <p class="text-stone-500 dark:text-stone-400 text-sm leading-relaxed">
          欢迎回来，<span style="color: #FF8C00 !important; font-weight: 700 !important;">
  {{ username }}
</span>！<br>
          正在为您跳转至主页...
        </p>
      </div>

    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const username = ref(route.query.username || '用户');

onMounted(() => {
  setTimeout(() => {
    router.push('/'); // 3 秒后跳首页
  }, 3000);
});
</script>

<style>
/* 成功页面颜色 */
.bg-success-bg {
  background-color: #DCFCE7 !important;
}
.text-success-text {
  color: #16A34A !important;
}
</style>
