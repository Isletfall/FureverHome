<template>
  <div class="font-display bg-background-light dark:bg-background-dark text-stone-900 dark:text-stone-200">
    <div class="relative flex min-h-screen w-full flex-col items-center justify-center p-4 sm:p-6">
      <main
        class="relative z-10 flex w-full max-w-[420px] flex-col items-center bg-white dark:bg-stone-800 rounded-xl shadow-xl px-10 py-12"
      >
        <!-- 标题 -->
        <div class="flex flex-col items-center gap-2 text-center mb-8">
          <h1 class="text-stone-900 dark:text-white text-3xl font-extrabold leading-tight tracking-tight">
            输入验证码
          </h1>
          <p class="text-sm text-stone-500 dark:text-stone-400 mt-2">
            验证码已发送至
            <span class="font-bold text-stone-900 dark:text-white">{{ email }}</span>
          </p>
        </div>

        <div class="w-full space-y-8">
          <!-- 6 位验证码输入框 -->
          <div class="flex justify-between gap-2">
            <input
              v-for="(item, index) in codes"
              :key="index"
              ref="codeInputs"
              v-model="codes[index]"
              type="text"
              maxlength="1"
              pattern="[0-9]"
              inputmode="numeric"
              class="w-12 h-14 text-center text-2xl font-bold bg-white dark:bg-stone-800 border border-stone-200 dark:border-stone-600 rounded-DEFAULT focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all text-stone-900 dark:text-white placeholder:text-stone-300"
              @input="handleInput($event, index)"
              @keydown.backspace.prevent="handleBackspace(index)"
            />
          </div>

          <div class="text-center text-sm text-stone-400 dark:text-stone-500">
            {{
              countdown > 0
                ? `重新发送验证码（${countdown}s）`
                : '如未收到邮件，可点击下方重新发送'
            }}
          </div>

          <div class="text-center">
            <button
              type="button"
              class="text-sm font-bold text-primary hover:text-primary-hover disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="countdown > 0 || sendingCode"
              @click="resendCode"
            >
              {{ countdown > 0 ? '请稍后再试' : '收不到验证码？' }}
            </button>
          </div>

          <button
            class="flex h-12 w-full items-center justify-center rounded-DEFAULT bg-primary px-6 text-base font-bold text-white shadow-sm transition-all hover:bg-primary-hover focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="submitting"
            @click="handleConfirmLogin"
          >
            {{ submitting ? '登录中...' : '完成登录' }}
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { sendLoginCode, loginByCode } from '@/api/authApi';
import { processLoginSuccess } from '@/utils/authHelpers';

const route = useRoute();
const router = useRouter();

const email = ref<string>((route.query.email as string) || 'user@example.com');

const codes = ref<string[]>(['', '', '', '', '', '']);
const codeInputs = ref<HTMLInputElement[]>([] as unknown as HTMLInputElement[]);
const countdown = ref(0);
const sendingCode = ref(false);
const submitting = ref(false);
let timer: number | undefined;

const focusFirstInput = () => {
  if (codeInputs.value && codeInputs.value[0]) {
    codeInputs.value[0].focus();
  }
};

const startCountdown = (seconds = 60) => {
  countdown.value = seconds;
  timer && window.clearInterval(timer);
  timer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      timer && window.clearInterval(timer);
      timer = undefined;
    }
  }, 1000);
};

const sendCode = async (showAlert = true) => {
  if (!email.value || !email.value.includes('@')) {
    alert('请输入正确的邮箱地址');
    return;
  }
  try {
    sendingCode.value = true;
    await sendLoginCode({ email: email.value });
    if (showAlert) {
      alert('验证码已发送，请检查邮箱');
    }
    startCountdown();
  } catch (error: any) {
    alert(error?.message || '验证码发送失败，请稍后重试');
  } finally {
    sendingCode.value = false;
  }
};

onMounted(() => {
  focusFirstInput();
  sendCode(false);
});

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer);
  }
});

function handleInput(e: Event, index: number) {
  const target = e.target as HTMLInputElement;
  const value = target.value.replace(/\D/g, '');
  codes.value[index] = value;

  if (value && index < codes.value.length - 1) {
    const next = codeInputs.value[index + 1];
    next && next.focus();
  }
}

function handleBackspace(index: number) {
  if (codes.value[index]) {
    codes.value[index] = '';
    return;
  }
  if (index > 0) {
    const prev = codeInputs.value[index - 1];
    prev && prev.focus();
    codes.value[index - 1] = '';
  }
}

const resendCode = async () => {
  if (countdown.value > 0 || sendingCode.value) return;
  await sendCode();
};

const handleConfirmLogin = async () => {
  const code = codes.value.join('');
  if (code.length !== 6) {
    alert('请输入完整的 6 位验证码');
    return;
  }

  submitting.value = true;
  try {
    const res = await loginByCode({ email: email.value, code });
    const { targetRouteName } = await processLoginSuccess(res, email.value);
    await router.push({ name: targetRouteName });
  } catch (error: any) {
    alert(error?.message || '登录失败，请稍后再试');
  } finally {
    submitting.value = false;
  }
};
</script>
