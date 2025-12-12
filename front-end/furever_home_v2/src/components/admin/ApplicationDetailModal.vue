<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-slate-900/70 z-[9999] flex items-center justify-center p-4"
    @click.self="handleClose"
  >
    <div class="absolute inset-0 bg-slate-900/80 backdrop-blur-sm"></div>
    <div class="relative z-10 w-full max-w-2xl">
      <div class="rounded-2xl bg-white dark:bg-[#1A1E26] shadow-2xl">
        <div class="flex items-start justify-between border-b border-slate-100 dark:border-slate-800 px-6 py-5 rounded-t-2xl">
          <div>
            <h2 class="text-xl font-bold text-slate-900 dark:text-white">领养申请详情</h2>
          </div>
          <button
            class="text-slate-400 hover:text-slate-600 dark:hover:text-white transition rounded-lg p-1 hover:bg-slate-100 dark:hover:bg-slate-800"
            @click="handleClose"
          >
            <span class="material-symbols-outlined text-xl">close</span>
          </button>
        </div>
        <div class="px-6 py-6 space-y-5 text-sm text-slate-600 dark:text-slate-300">
          <div class="flex items-center gap-3">
            <img
              v-if="applicationData?.applicantAvatar"
              :src="applicationData.applicantAvatar"
              :alt="applicationData.applicant"
              class="size-12 rounded-full object-cover shrink-0"
              @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
            />
            <div
              v-if="!applicationData?.applicantAvatar || !applicationData.applicantAvatar.trim()"
              class="size-12 rounded-full bg-primary/10 text-primary flex items-center justify-center text-sm font-medium shrink-0"
            >
              {{ applicationData?.applicant?.charAt(0) || '头' }}
            </div>
            <div>
              <p class="text-base font-semibold text-slate-900 dark:text-white">{{ applicationData?.applicant || '未知用户' }}</p>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">提交时间：{{ applicationData?.time || '未知时间' }}</p>
            </div>
          </div>
          <div class="space-y-2">
            <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">联系信息</p>
            <p class="text-slate-900 dark:text-white">手机号：{{ applicationData?.phone || '未提供' }}</p>
            <p class="text-slate-900 dark:text-white">邮箱：{{ applicationData?.email || '未提供' }}</p>
          </div>
          <div class="space-y-2">
            <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">居住地址</p>
            <p class="text-slate-900 dark:text-white">{{ applicationData?.address || '未提供' }}</p>
          </div>
          <div class="space-y-2">
            <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">申请理由</p>
            <p class="leading-relaxed text-slate-900 dark:text-white">
              {{ applicationData?.reason || '未提供申请理由' }}
            </p>
          </div>
          <div v-if="applicationData?.petName" class="space-y-2">
            <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">申请的宠物</p>
            <p class="text-base font-medium text-slate-900 dark:text-white">{{ applicationData.petName }}</p>
          </div>
          <div v-if="applicationData?.targetUser" class="space-y-2">
            <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">被申请用户</p>
            <div class="flex items-center gap-3">
              <img
                v-if="applicationData.targetUserAvatar"
                :src="applicationData.targetUserAvatar"
                :alt="applicationData.targetUser"
                class="size-10 rounded-full object-cover shrink-0"
                @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
              />
              <div
                v-if="!applicationData.targetUserAvatar || !applicationData.targetUserAvatar.trim()"
                class="size-10 rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-300 flex items-center justify-center text-xs font-medium shrink-0"
              >
                {{ applicationData.targetUser?.charAt(0) || '用' }}
              </div>
              <p class="text-base font-medium text-slate-900 dark:text-white">{{ applicationData.targetUser }}</p>
            </div>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 border-t border-slate-100 dark:border-slate-800 px-6 py-4 rounded-b-2xl">
          <button
            class="px-5 py-2.5 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition"
            @click="handleClose"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface ApplicationData {
  id?: number;
  applicant?: string;
  applicantAvatar?: string;
  petName?: string;
  targetUser?: string;
  targetUserAvatar?: string;
  time?: string;
  phone?: string;
  email?: string;
  address?: string;
  reason?: string;
}

const props = defineProps<{
  visible: boolean;
  applicationData?: ApplicationData;
}>();

const emit = defineEmits<{
  close: [];
}>();

function handleClose() {
  emit('close');
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 24;
}
</style>

