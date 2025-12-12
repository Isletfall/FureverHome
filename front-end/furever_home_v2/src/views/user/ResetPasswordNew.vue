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
            设置新密码
          </h1>
          <p class="text-sm text-stone-500 dark:text-stone-400">
            请设置一个安全且易于记忆的密码
          </p>
        </div>

        <div class="w-full space-y-6">
          <!-- 验证码 -->
          <div class="space-y-1">
            <label class="text-sm font-bold text-stone-700 dark:text-stone-300 ml-1">邮箱验证码</label>
            <input
              v-model="code"
              type="text"
              maxlength="6"
              class="form-input w-full border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800 h-12 px-4 rounded-DEFAULT text-stone-900 dark:text-white placeholder:text-stone-400 focus:outline-none focus:ring-1 focus:ring-primary/50 focus:border-primary transition-all text-sm"
              placeholder="请输入 6 位验证码"
            />
          </div>
          
          <!-- 新密码 -->
          <div class="space-y-1">
            <label class="text-sm font-bold text-stone-700 dark:text-stone-300 ml-1">新密码</label>
            <div class="relative flex w-full">
              <input
                :type="showPassword1 ? 'text' : 'password'"
                v-model="password"
                class="form-input flex w-full border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800 h-12 px-4 rounded-DEFAULT text-stone-900 dark:text-white placeholder:text-stone-400 focus:outline-none focus:ring-1 focus:ring-primary/50 focus:border-primary transition-all text-sm pr-10"
                placeholder="请输入您的新密码"
              />
              <button
                type="button"
                class="absolute right-0 top-0 h-full px-3 text-stone-400 hover:text-stone-500 dark:hover:text-stone-200 flex items-center justify-center"
                @click="showPassword1 = !showPassword1"
              >
                <span class="material-symbols-outlined text-[20px]">
                  {{ showPassword1 ? 'visibility_off' : 'visibility' }}
                </span>
              </button>
            </div>
            <p class="text-xs text-stone-400 dark:text-stone-500 ml-1">
              密码需至少 6 位，并包含字母和数字。
            </p>
          </div>

          <!-- 确认新密码 -->
          <div class="space-y-1">
            <label class="text-sm font-bold text-stone-700 dark:text-stone-300 ml-1">确认新密码</label>
            <div class="relative flex w-full">
              <input
                :type="showPassword2 ? 'text' : 'password'"
                v-model="password2"
                class="form-input flex w-full border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800 h-12 px-4 rounded-DEFAULT text-stone-900 dark:text-white placeholder:text-stone-400 focus:outline-none focus:ring-1 focus:ring-primary/50 focus:border-primary transition-all text-sm pr-10"
                placeholder="请再次输入新密码"
              />
              <button
                type="button"
                class="absolute right-0 top-0 h-full px-3 text-stone-400 hover:text-stone-500 dark:hover:text-stone-200 flex items-center justify-center"
                @click="showPassword2 = !showPassword2"
              >
                <span class="material-symbols-outlined text-[20px]">
                  {{ showPassword2 ? 'visibility_off' : 'visibility' }}
                </span>
              </button>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="pt-2">
            <button
              class="flex h-12 w-full items-center justify-center rounded-DEFAULT bg-primary px-6 text-base font-bold text-white shadow-sm transition-all hover:bg-primary-hover focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="submitting"
              @click="handleConfirmReset"
            >
              {{ submitting ? '提交中...' : '确认并更新密码' }}
            </button>
          </div>

          <!-- 返回登录 -->
          <!-- <div class="text-center pt-2 border-t border-stone-100 dark:border-stone-700 mt-4">
            <button
              type="button"
              class="text-sm font-bold text-stone-500 hover:text-primary dark:text-stone-400 flex items-center justify-center gap-1 transition-colors"
              @click="goBackToLogin"
            >
              <span class="material-symbols-outlined text-base">arrow_back</span>
              返回登录
            </button>
          </div> -->
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { confirmResetPassword } from '@/api/authApi'

const router = useRouter()
const route = useRoute()

const email = ref<string>((route.query.email as string) || '')
const code = ref('')
const password = ref('')
const password2 = ref('')
const showPassword1 = ref(false)
const showPassword2 = ref(false)
const submitting = ref(false)

const isStrongPassword = (value: string) => {
  if (value.length < 6) return false
  const hasLetter = /[A-Za-z]/.test(value)
  const hasNumber = /[0-9]/.test(value)
  return hasLetter && hasNumber
}

const handleConfirmReset = async () => {
  if (!email.value || !email.value.includes('@')) {
    alert('邮箱信息缺失，请重新提交重置请求')
    return
  }
  if (!code.value || code.value.length < 4) {
    alert('请输入正确的邮箱验证码')
    return
  }
  if (!password.value || !password2.value) {
    alert('请输入并确认新密码')
    return
  }
  if (!isStrongPassword(password.value)) {
    alert('密码需至少 6 位，并包含字母和数字')
    return
  }
  if (password.value !== password2.value) {
    alert('两次输入的密码不一致')
    return
  }

  submitting.value = true
  try {
    await confirmResetPassword({
      email: email.value,
      code: code.value,
      newPassword: password.value
    })
    router.push({ name: 'ResetPasswordSuccess' })
  } catch (e: any) {
    console.error('重置密码失败', e)
    alert(e?.message || '重置密码失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

const goBackToLogin = () => {
  router.push('/login/password')
}
</script>


