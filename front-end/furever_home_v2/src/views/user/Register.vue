<template>
  <div class="font-display bg-background-light dark:bg-background-dark text-stone-900 dark:text-stone-200 min-h-screen flex items-center justify-center p-4">

    <main
      class="w-full max-w-[420px] bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-12"
    >
      <!-- 标题 -->
      <div class="flex flex-col items-center gap-2 text-center mb-8">
        <h1 class="text-stone-900 dark:text-white text-3xl font-extrabold">
          创建账户
        </h1>
        <p class="text-sm text-stone-500 dark:text-stone-400">
          欢迎加入 FUREVER HOME 大家庭
        </p>
      </div>

      <div class="w-full space-y-5">

        <!-- 邮箱 -->
        <div class="space-y-1">
          <label class="text-sm font-bold ml-1">邮箱地址</label>
          <div class="relative">
            <input
              v-model="email"
              type="email"
              class="form-input border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800
              h-12 px-4 rounded text-stone-900 dark:text-white text-sm w-full pr-10"
              placeholder="请输入您的邮箱"
            />
            <!-- <span
              class="material-symbols-outlined text-[20px] text-stone-400 absolute right-3 top-1/2 -translate-y-1/2"
            >
              mail
            </span> -->
          </div>
        </div>

        <!-- 验证码：输入框 + 获取按钮 -->
        <div class="space-y-1">
          <label class="text-sm font-bold ml-1">邮箱验证码</label>
          <div class="flex gap-3">
            <input
              v-model="code"
              type="text"
              maxlength="6"
              class="form-input flex-1 border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800
              h-12 px-4 rounded text-stone-900 dark:text-white text-sm"
              placeholder="请输入验证码"
            />
            <button
              type="button"
              class="h-12 px-4 whitespace-nowrap rounded text-sm font-bold transition-colors"
              :class="(sendingCode || countdown > 0)
                ? 'bg-stone-200 text-stone-400 cursor-not-allowed'
                : 'bg-primary text-white hover:bg-primary-hover'"
              :disabled="sendingCode || countdown > 0"
              @click="handleSendCode"
            >
              {{ sendCodeText }}
            </button>
          </div>
        </div>

        <!-- 密码 -->
        <div class="space-y-1">
          <label class="text-sm font-bold ml-1">设置密码</label>
          <input
            :type="showPassword1 ? 'text' : 'password'"
            v-model="password"
            class="form-input border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800
            h-12 px-4 rounded text-stone-900 dark:text-white text-sm w-full pr-10"
            placeholder="请输入您的密码"
          />
          <!-- <div class="relative">
            <input
              :type="showPassword1 ? 'text' : 'password'"
              v-model="password"
              class="form-input border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800
              h-12 px-4 rounded text-stone-900 dark:text-white text-sm w-full pr-10"
              placeholder="请输入您的密码"
            />
            <button
              type="button"
              @click="showPassword1 = !showPassword1"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-stone-400"
            >
              <span class="material-symbols-outlined text-[20px]">
                {{ showPassword1 ? 'visibility_off' : 'visibility' }}
              </span>
            </button>
          </div> -->
        </div>

        <!-- 再次输入密码 -->
        <div class="space-y-1">
          <label class="text-sm font-bold ml-1">确认密码</label>
          <div class="relative">
            <input
              :type="showPassword2 ? 'text' : 'password'"
              v-model="password2"
              class="form-input border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800
              h-12 px-4 rounded text-stone-900 dark:text-white text-sm w-full pr-10"
              placeholder="请再次输入您的密码"
            />
            <!-- <button
              type="button"
              @click="showPassword2 = !showPassword2"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-stone-400"
            >
              <span class="material-symbols-outlined text-[20px]">
                {{ showPassword2 ? 'visibility_off' : 'visibility' }}
              </span>
            </button> -->
          </div>
        </div>

        <!-- 昵称 -->
        <div class="space-y-1">
          <label class="text-sm font-bold ml-1">您的昵称</label>
          <input
            v-model="nickname"
            type="text"
            class="form-input border border-stone-200 dark:border-stone-700 bg-white dark:bg-stone-800
            h-12 px-4 rounded text-stone-900 dark:text-white text-sm w-full"
            placeholder="请输入您的昵称"
          />
        </div>

        <!-- 注册按钮 -->
        <div class="pt-2">
          <button
            class="h-12 w-full bg-primary text-white font-bold rounded hover:bg-primary-hover transition-all disabled:opacity-60 disabled:cursor-not-allowed"
            @click="handleRegister"
            :disabled="submitting"
          >
            {{ submitting ? '提交中...' : '完成注册' }}
          </button>

          <p class="text-center text-xs text-stone-400 dark:text-stone-500 mt-3">
            点击“完成注册”即表示您同意
            <a class="text-primary hover:underline" href="#">《用户协议》</a>
            和
            <a class="text-primary hover:underline" href="#">《隐私政策》</a>
          </p>
        </div>

        <!-- 已有账号 -->
        <div class="text-center text-sm text-stone-500 dark:text-stone-400 pt-4 border-t border-stone-200 dark:border-stone-700">
          已有账户？
          <span
            class="font-bold text-primary cursor-pointer"
            @click="goLogin"
          >
            返回登录
          </span>
        </div>

      </div>
    </main>

    <!-- 注册成功弹窗 -->
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
            注册成功！
          </h1>
          <p class="text-stone-500 dark:text-stone-400 text-sm leading-relaxed">
            欢迎加入 FUREVER HOME 大家庭～<br />
            正在为您跳转回登录页面...
          </p>
        </div>
      </main>
    </div>

  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { confirmRegister, sendRegisterCode, type ConfirmRegisterRequest } from '../../api/authApi';

const router = useRouter();

// 表单数据
const email = ref('');
const code = ref('');
const password = ref('');
const password2 = ref('');
const nickname = ref('');

// 密码可见切换
const showPassword1 = ref(false);
const showPassword2 = ref(false);

// 发送验证码相关状态
const sendingCode = ref(false);
const sendCodeText = ref('获取验证码');
// 当前倒计时秒数（>0 时按钮禁用）
let countdown = 0;
let timer: number | undefined;

// 提交注册状态
const submitting = ref(false);
// 注册成功弹窗
const showSuccess = ref(false);

// 发送注册验证码
const handleSendCode = async () => {
  if (!email.value || !email.value.includes('@')) {
    alert('请先输入正确的邮箱地址！');
    return;
  }
  if (sendingCode.value || countdown > 0) return;

  try {
    sendingCode.value = true;
    sendCodeText.value = '发送中...';

    const res = await sendRegisterCode({ email: email.value });
    if ((res.code === 0 || res.code === 200)) {
      alert('验证码已发送，请检查邮箱');
      // 开始 60 秒倒计时
      countdown = 60;
      sendCodeText.value = `重新获取验证码（${countdown}）`;
      timer = window.setInterval(() => {
        countdown -= 1;
        if (countdown <= 0) {
          if (timer) window.clearInterval(timer);
          timer = undefined;
          sendCodeText.value = '获取验证码';
        } else {
          sendCodeText.value = `重新获取验证码（${countdown}）`;
        }
      }, 1000);
    } else {
      alert(res.message || '验证码发送失败，请稍后重试');
      sendCodeText.value = '获取验证码';
    }
  } catch (e: any) {
    console.error('发送注册验证码失败', e);
    alert(e?.message || '验证码发送失败，请稍后重试');
    sendCodeText.value = '获取验证码';
  } finally {
    sendingCode.value = false;
  }
};

// 注册逻辑：调用确认注册接口
const handleRegister = async () => {
  if (!email.value.includes('@')) {
    alert('请输入正确的邮箱！');
    return;
  }
  if (!code.value) {
    alert('请输入邮箱验证码！');
    return;
  }
  if (!password.value) {
    alert('请输入密码！');
    return;
  }
  if (password.value !== password2.value) {
    alert('两次密码不一致！');
    return;
  }
  if (!nickname.value) {
    alert('请输入昵称！');
    return;
  }

  const payload: ConfirmRegisterRequest = {
    email: email.value,
    code: code.value,
    password: password.value,
    userName: nickname.value
  };

  try {
    submitting.value = true;
    const res = await confirmRegister(payload);
    if ((res.code === 0 || res.code === 200) && res.data) {
      showSuccess.value = true;
      // 2 秒后自动跳回最初的登录入口页
      setTimeout(() => {
        showSuccess.value = false;
        router.push('/login');
      }, 2000);
    } else {
      alert(res.message || '注册失败，请稍后重试');
    }
  } catch (e: any) {
    console.error('确认注册失败', e);
    alert(e?.message || '注册失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

const goLogin = () => {
  router.push('/login');
};

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer);
  }
});
</script>

<style>
/* 保证 tailwind 的 primary 能用 */
:root {
  --tw-primary: #ff8c00;
}
.text-primary {
  color: #ff8c00 !important;
}

/* 让按钮 hover 的时候也强制橙色 */
.bg-primary {
  background-color: #ff8c00 !important;
}

.bg-primary-hover:hover {
  background-color: #e67e00 !important;
}

/* 链接使用橙色 */
a.text-primary {
  color: #ff8c00 !important;
}

.font-orange {
  color: #ff8c00 !important;
}
</style>
