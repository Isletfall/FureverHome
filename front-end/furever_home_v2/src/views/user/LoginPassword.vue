<template>
  <div class="font-display bg-background-light dark:bg-background-dark text-stone-900 dark:text-stone-200">
    <div class="relative flex min-h-screen w-full flex-col items-center justify-center p-4 sm:p-6">
      <main
        class="relative z-10 flex w-full max-w-[420px] flex-col items-center bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-12"
      >
        <div class="flex flex-col items-center gap-2 text-center mb-8">
          <h1 class="text-stone-900 dark:text-white text-3xl font-extrabold leading-tight tracking-tight">
            欢迎使用<br/>FUREVERHOME!
          </h1>
        </div>

        <div class="w-full space-y-5">
          <!-- 账号/邮箱输入行 -->
          <div
            class="flex w-full items-center justify-between border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800 h-12 px-4 rounded-DEFAULT"
          >
            <input
              v-model="account"
              type="text"
              autocomplete="username"
              class="flex-1 bg-transparent outline-none text-sm text-stone-900 dark:text-white placeholder:text-stone-400"
              placeholder="请输入您的账号或邮箱"
            />
          </div>

          <!-- 密码输入 -->
          <div class="relative flex w-full">
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="password"
              class="form-input flex w-full border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800 h-12 px-4 rounded-DEFAULT text-stone-900 dark:text-white placeholder:text-stone-400 focus:outline-none focus:ring-1 focus:ring-primary/50 focus:border-primary transition-all text-sm pr-10"
              placeholder="请输入您的密码"
            />
            <button
              type="button"
              class="absolute right-0 top-0 h-full px-3 text-stone-400 hover:text-stone-500 dark:hover:text-stone-200 flex items-center justify-center"
              @click="togglePassword"
            >
              <!-- <span class="material-symbols-outlined text-[20px]">
                {{ showPassword ? 'visibility_off' : 'visibility' }}
              </span> -->
            </button>
          </div>

          <!-- 忘记密码 -->
          <div class="flex justify-end pt-0">
            <button
              type="button"
              class="text-sm font-bold text-primary hover:text-primary-hover"
              @click="goResetPassword"
            >
              忘记密码?
            </button>
          </div>

          <!-- 登录按钮 -->
          <button
            class="flex h-12 w-full items-center justify-center rounded-DEFAULT px-6 text-base font-bold text-white shadow-sm transition-all mt-2 disabled:opacity-60 disabled:cursor-not-allowed"
            :style="{
              backgroundColor: loading ? '#e67e00' : '#FF8C00'
            }"
            :disabled="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
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

          <!-- 底部注册链接 -->
          <div class="text-center text-sm text-stone-500 dark:text-stone-400 mt-6">
            还没有账户?
            <a class="font-bold text-primary hover:text-primary-hover ml-1" href="javascript:void(0)" @click="goRegister">
              立即注册
            </a>
          </div>
        </div>

        <!-- 协议文案 -->
        <div class="mt-8 text-center">
          <p class="text-xs text-stone-400 dark:text-stone-500 transform scale-90">
            继续即表示您同意我们的
            <a class="underline hover:text-stone-600 dark:hover:text-stone-300" href="javascript:void(0)">《用户协议》</a>
            和
            <a class="underline hover:text-stone-600 dark:hover:text-stone-300" href="javascript:void(0)">《隐私政策》</a>
          </p>
        </div>
      </main>

      <!-- 登录成功弹窗 -->
      <div
        v-if="showSuccess"
        class="fixed inset-0 bg-black/40 z-[3000] flex items-center justify-center p-4 sm:p-6"
      >
        <main
          class="relative z-10 flex w-full max-w-[420px] flex-col items-center justify-center bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-16 text-center"
        >
          <div class="w-24 h-24 bg-[#DCFCE7] dark:bg-green-900/30 rounded-full flex items-center justify-center mb-8">
            <svg
              class="w-12 h-12 text-[#16A34A]"
              fill="none"
              stroke="currentColor"
              stroke-width="3"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"></path>
            </svg>
          </div>

          <div class="space-y-3">
            <h1 class="text-3xl font-extrabold text-stone-900 dark:text-white tracking-tight">
              {{ isAdminLogin ? '管理员登录成功！' : '登录成功！' }}
            </h1>
            <p class="text-stone-500 dark:text-stone-400 text-sm leading-relaxed">
              欢迎回来，<span class="font-bold text-primary">{{ successUserName }}</span>！<br />
              正在为您跳转至{{ isAdminLogin ? '后台首页' : '主页' }}...
            </p>
          </div>
        </main>
      </div>

      <!-- 错误提示 -->
      <div
        v-if="errorMessage"
        class="fixed bottom-6 left-1/2 -translate-x-1/2 bg-red-500 text-white text-sm px-4 py-2 rounded-full shadow-lg z-[3100]"
      >
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { userLogin } from '../../api/authApi';
import { processLoginSuccess } from '@/utils/authHelpers';

const route = useRoute();
const router = useRouter();

// 账号（可以是用户名或邮箱），尝试从 query 中预填
// 优先使用 account，其次回退到 email
const account = ref<string>(
  ((route.query.account as string) || (route.query.email as string) || '') as string
);
const password = ref('');
const showPassword = ref(false);
const loading = ref(false);
const errorMessage = ref('');

const showSuccess = ref(false);
const successUserName = ref('');
const isAdminLogin = ref(false); // 是否为管理员登录成功

function togglePassword() {
  showPassword.value = !showPassword.value;
}

function goRegister() {
  router.push({ name: 'Register' });
}

function goResetPassword() {
  router.push({ name: 'ResetPasswordRequest', query: { email: account.value } });
}

async function handleLogin() {
  if (!account.value || !password.value) {
    errorMessage.value = '请输入账号和密码';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const res: any = await userLogin({
      account: account.value,
      password: password.value
    });

    const { targetRouteName, successUserName: userName, isAdminLogin: isAdmin } =
      await processLoginSuccess(res, account.value);

    successUserName.value = userName;
    isAdminLogin.value = isAdmin;
    showSuccess.value = true;

    window.setTimeout(async () => {
      try {
        await router.push({ name: targetRouteName });
      } catch (e) {
        console.error('路由跳转失败，使用浏览器跳转兜底', e);
        const fallbackPath = targetRouteName === 'AdminDashboard' ? '/admin' : '/home';
        window.location.href = fallbackPath;
      }
    }, 2000);
  } catch (error: any) {
    errorMessage.value = error?.message || '登录失败，请稍后重试';
  } finally {
    loading.value = false;
  }
}

const goBack = () => {
  router.push({ name: 'Login' })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* 强制灰色 placeholder */
.placeholder-text {
  color: #9CA3AF !important;
}
</style>
