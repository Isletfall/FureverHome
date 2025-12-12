<template>
  <div class="font-display bg-background-light dark:bg-background-dark text-stone-900 dark:text-stone-200">
    <div class="relative flex min-h-screen w-full flex-col items-center justify-center p-4 sm:p-6">
      <main
        class="relative z-10 flex w-full max-w-[420px] flex-col items-center bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-12"
      >
        <!-- 返回按钮 -->
        <div class="w-full flex justify-start mb-2">
          <button 
            @click="router.back()" 
            class="flex items-center gap-1 text-[#FF8C00] hover:text-[#e6722a] transition-colors"
            title="返回"
          >
            <i class="fa-solid fa-arrow-left text-lg"></i>
            <span class="font-medium">返回</span>
          </button>
        </div>

        <!-- 标题 -->
        <div class="flex flex-col items-center gap-2 text-center mb-8">
          <h1 class="text-stone-900 dark:text-white text-3xl font-extrabold leading-tight tracking-tight">
            重置您的密码
          </h1>
          <p class="text-sm text-stone-500 dark:text-stone-400">
            我们将向您的邮箱发送重置链接
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

          <p class="text-center text-sm text-stone-500 dark:text-stone-400 px-4">
            输入您注册时使用的邮箱地址，我们会向该邮箱发送重置密码的链接。
          </p>

          <!-- 发送重置邮件 -->
          <button
            class="flex h-12 w-full items-center justify-center rounded-DEFAULT bg-primary px-6 text-base font-bold text-white shadow-sm transition-all hover:bg-primary-hover focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="submitting"
            @click="handleSendResetEmail"
          >
            {{ submitting ? '发送中...' : '发送重置邮件' }}
          </button>

          <!-- 返回登录 -->
          <!-- <div class="text-center pt-2">
            <button
              type="button"
              class="text-sm font-bold text-primary hover:text-primary-hover flex items-center justify-center gap-1"
              @click="goBackToLogin"
            >
              <span class="material-symbols-outlined text-base">arrow_back</span>
              返回登录
            </button>
          </div> -->
        </div>

        <div class="mt-8 text-center">
          <p class="text-xs text-stone-400 dark:text-stone-500 transform scale-90">
            继续即表示您同意我们的
            <a class="underline hover:text-stone-600 dark:hover:text-stone-300" href="javascript:void(0)">《用户协议》</a>
            和
            <a class="underline hover:text-stone-600 dark:hover:text-stone-300" href="javascript:void(0)">《隐私政策》</a>
          </p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { sendResetPasswordCode } from '@/api/authApi'

const router = useRouter()

const email = ref('')
const submitting = ref(false)

const handleSendResetEmail = async () => {
  if (!email.value || !email.value.includes('@')) {
    alert('请输入正确的邮箱地址')
    return
  }

  submitting.value = true
  try {
    await sendResetPasswordCode({ email: email.value })
    alert('验证码已发送，请查收邮箱')
    router.push({
      name: 'ResetPasswordNew',
      query: { email: email.value }
    })
  } catch (e: any) {
    console.error('发送重置邮件失败', e)
    alert(e?.message || '发送重置邮件失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

const goBackToLogin = () => {
  router.push('/login/password')
}
</script>


