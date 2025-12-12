<template>
  <div class="font-display bg-background-light dark:bg-background-dark text-stone-900 dark:text-stone-200">
    <div class="relative flex min-h-screen w-full flex-col items-center justify-center p-4 sm:p-6">
      <main
        class="relative z-10 flex w-full max-w-[420px] flex-col items-center bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-12"
      >
        <!-- 标题 -->
        <div class="flex flex-col items-center gap-2 text-center mb-8">
          <h1 class="text-stone-900 dark:text-white text-3xl font-extrabold leading-tight tracking-tight">
            邮箱验证码登录
          </h1>
          <p class="text-sm text-stone-500 dark:text-stone-400">
            先输入您的邮箱，我们将发送登录验证码
          </p>
        </div>

        <div class="w-full space-y-6">
          <!-- 邮箱输入 -->
          <div class="space-y-1">
            <label class="text-sm font-bold ml-1">邮箱地址</label>
            <div
              class="flex w-full items-center justify-between border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800 h-12 px-4 rounded-DEFAULT"
            >
              <input
                v-model="email"
                type="email"
                autocomplete="email"
                class="flex-1 bg-transparent outline-none text-sm text-stone-900 dark:text-white placeholder:text-stone-400"
                placeholder="请输入您的邮箱"
              />
            </div>
          </div>

          <!-- 下一步按钮：进入输入验证码页 -->
          <button
            class="flex h-12 w-full items-center justify-center rounded-DEFAULT bg-primary px-6 text-base font-bold text-white shadow-sm transition-all hover:bg-primary-hover focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            @click="goToCodePage"
          >
            下一步：输入验证码
          </button>

          <!-- 返回 -->
          <div class="text-center pt-2">
            <button
              type="button"
              class="text-sm font-bold text-primary hover:text-primary-hover"
              @click="goBack"
            >
              返回登录方式选择
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 如果从别处带了 email，就预填一下
const email = ref<string>((route.query.email as string) || '')

const goToCodePage = () => {
  if (!email.value || !email.value.includes('@')) {
    alert('请输入正确的邮箱地址')
    return
  }
  router.push({
    name: 'LoginEmail',
    query: { email: email.value }
  })
}

const goBack = () => {
  router.push({ name: 'Login' })
}
</script>


