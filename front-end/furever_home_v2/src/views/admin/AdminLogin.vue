<template>
  <div
    class="relative flex min-h-screen w-full flex-col items-center justify-center bg-[#F6F6F8] dark:bg-[#101622] overflow-x-hidden"
  >
    <div class="w-full max-w-md px-6">
      <div
        class="flex flex-col gap-8 rounded-xl border border-slate-200/60 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900/60"
      >
        <div class="flex flex-col items-center gap-4">
          <img src="@/assets/icons/logo.svg" alt="FUREVER HOME" class="h-20 w-auto" />
          <h1
            class="text-[#111318] dark:text-white text-[24px] font-bold leading-tight text-center tracking-tight"
          >
            管理后台
          </h1>
        </div>

        <form class="flex flex-col gap-6" @submit.prevent="handleSubmit">
          <div class="flex flex-col gap-4">
            <label class="flex flex-col">
              <p class="text-[#111318] dark:text-slate-300 text-sm font-medium leading-normal pb-2">
                绠＄悊鍛樿处鍙?              </p>
              <div class="relative flex items-center">
                <span class="material-symbols-outlined absolute left-4 text-slate-500 dark:text-slate-400">
                  person
                </span>
                <input
                  v-model="form.username"
                  autocomplete="username"
                  class="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg text-[#111318] dark:text-white focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-[#dbdfe6] dark:border-slate-700 bg-white dark:bg-slate-800 focus:border-primary h-14 placeholder:text-[#616f89] dark:placeholder:text-slate-500 pl-12 pr-4 py-3 text-base font-normal leading-normal"
                  placeholder="璇疯緭鍏ユ偍鐨勮处鍙?
                />
              </div>
            </label>

            <label class="flex flex-col">
              <p class="text-[#111318] dark:text-slate-300 text-sm font-medium leading-normal pb-2">
                瀵嗙爜
              </p>
              <div class="relative flex itemscenter">
                <span class="material-symbols-outlined absolute left-4 text-slate-500 dark:text-slate-400">
                  lock
                </span>
                <input
                  v-model="form.password"
                  autocomplete="current-password"
                  type="password"
                  class="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg text-[#111318] dark:text-white focus:outline-0 focus:ring-2 focus:ring-primary/50 border border-[#dbdfe6] dark:border-slate-700 bg-white dark:bg-slate-800 focus:border-primary h-14 placeholder:text-[#616f89] dark:placeholder:text-slate-500 pl-12 pr-4 py-3 text-base font-normal leading-normal"
                  placeholder="璇疯緭鍏ユ偍鐨勫瘑鐮?
                />
              </div>
            </label>
          </div>

          <button
            type="submit"
            class="flex min-w-[84px] w-full cursor-pointer items-center justify-center overflow-hidden rounded-lg h-12 px-5 text-white text-base font-bold leading-normal tracking-[0.015em] focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
            :style="{ backgroundColor: loading ? '#4b7cff' : '#1152d4' }"
            :disabled="loading"
          >
            <span class="truncate">
              {{ loading ? '鐧诲綍涓?..' : '鐧?褰? }}
            </span>
          </button>

          <p v-if="errorMessage" class="text-sm text-red-500 text-center">
            {{ errorMessage }}
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin, type AdminLoginRequest } from '../../api/adminAuthApi'

const router = useRouter()

const form = reactive<AdminLoginRequest>({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  if (!form.username || !form.password) {
    errorMessage.value = '璇疯緭鍏ヨ处鍙峰拰瀵嗙爜'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const res = await adminLogin({
      username: form.username,
      password: form.password
    })

    if (res.code === 0 && res.data) {
      const tokenValue = (res.data as any).tokenValue || res.data.token || (res.data as any)?.tokenInfo?.tokenValue
      const tokenName = (res.data as any).tokenName || (res.data as any)?.tokenInfo?.tokenName || 'satoken'
      const cleanToken = tokenValue?.toString().replace(/^Bearer\s+/i, '')

      if (cleanToken) {
        localStorage.setItem('token', cleanToken)
        localStorage.setItem('Authorization', `Bearer ${cleanToken}`)
        if (tokenName) {
          localStorage.setItem('saTokenName', tokenName)
          localStorage.setItem('saTokenValue', cleanToken)
        }
        router.push({ name: 'AdminDashboard' })
      } else {
        errorMessage.value = res.message || '鐧诲綍澶辫触锛屾湭鑾峰彇鍒版湁鏁?token'
      }
    } else {
      errorMessage.value = res.message || '鐧诲綍澶辫触锛岃妫€鏌ヨ处鍙锋垨瀵嗙爜'
    }
  } catch (error: any) {
    errorMessage.value = error?.message || '鐧诲綍澶辫触锛岃绋嶅悗閲嶈瘯'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}
</style>
