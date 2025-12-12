<template>
  <div class="font-display bg-background-light dark:bg-background-dark text-stone-900 dark:text-stone-200">
    <div class="relative flex min-h-screen w-full flex-col items-center justify-center p-4 sm:p-6">
      <main
        class="relative z-10 flex w-full max-w-[420px] flex-col items-center justify-center bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-16 text-center"
      >
        <div class="w-24 h-24 bg-[#DCFCE7] dark:bg-green-900/30 rounded-full flex items-center justify-center mb-8">
          <span class="material-symbols-outlined text-5xl text-[#16A34A] font-bold">check</span>
        </div>

        <div class="space-y-4 mb-10">
          <h1 class="text-3xl font-extrabold text-stone-900 dark:text-white tracking-tight">
            密码已重置！
          </h1>
          <p class="text-stone-500 dark:text-stone-400 text-sm leading-relaxed px-4">
            您的密码已更新成功。<br />
            现在可以使用新密码登录您的账户了。
          </p>
        </div>

        <button
          class="flex h-12 w-full items-center justify-center rounded-DEFAULT bg-primary px-6 text-base font-bold text-white shadow-sm transition-all hover:bg-primary-hover focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
          @click="goLogin"
        >
          立即登录
        </button>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goLogin = () => {
  router.push('/login/password')
}
</script>


