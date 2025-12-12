<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-slate-900/70 z-[9999] flex items-center justify-center p-4"
    @click.self="handleClose"
  >
    <div class="absolute inset-0 bg-slate-900/80 backdrop-blur-sm"></div>
    <div class="relative z-10 w-full max-w-4xl">
      <div class="rounded-2xl bg-white shadow-2xl dark:bg-[#1A1E26]">
        <div class="flex items-start justify-between border-b border-slate-100 dark:border-slate-800 px-6 py-5 rounded-t-2xl">
          <div>
            <h2 class="text-xl font-bold text-slate-900 dark:text-white">宠物详情</h2>
          </div>
          <button
            class="text-slate-400 hover:text-slate-600 dark:hover:text-white transition rounded-lg p-1 hover:bg-slate-100 dark:hover:bg-slate-800"
            @click="handleClose"
          >
            <span class="material-symbols-outlined text-xl">close</span>
          </button>
        </div>
        <div class="px-6 py-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div class="rounded-xl overflow-hidden h-64 bg-slate-100 dark:bg-slate-800">
              <img
                v-if="petData?.mainImage"
                :src="petData.mainImage"
                alt="宠物头像"
                class="h-full w-full object-cover"
              />
              <div v-else class="h-full w-full flex items-center justify-center text-slate-500 text-sm">
                {{ petData?.name || '宠物' }}的照片
              </div>
            </div>
            <div class="grid grid-cols-4 gap-3">
              <img
                v-for="(img, index) in petData?.galleryImages?.slice(0, 3)"
                :key="index"
                class="h-20 rounded-lg object-cover"
                :src="img"
                :alt="`pet gallery ${index + 1}`"
              />
              <div
                v-if="petData?.galleryImages && petData.galleryImages.length > 3"
                class="h-20 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-slate-500 text-xs"
              >
                +{{ petData.galleryImages.length - 3 }}
              </div>
              <div
                v-else-if="!petData?.galleryImages || petData.galleryImages.length === 0"
                class="h-20 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-slate-500 text-xs"
              >
                暂无图片
              </div>
            </div>
          </div>
          <div class="space-y-5">
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
                {{ petData?.name || '未知' }} · {{ petData?.breed || '未知品种' }}
              </h3>
            </div>
            <div class="flex items-center gap-3 pb-4 border-b border-slate-100 dark:border-slate-800">
              <img
                v-if="petData?.adopterAvatar"
                :src="petData.adopterAvatar"
                :alt="petData.adopterName"
                class="size-10 rounded-full object-cover shrink-0"
                @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
              />
              <div
                v-if="!petData?.adopterAvatar || !petData.adopterAvatar.trim()"
                class="size-10 rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-300 flex items-center justify-center text-sm font-medium shrink-0"
              >
                {{ petData?.adopterName?.charAt(0) || '领' }}
              </div>
              <div>
                <p class="text-sm text-slate-400 dark:text-slate-500 mb-0.5">领养者</p>
                <p class="text-base font-semibold text-slate-900 dark:text-white">{{ petData?.adopterName || '未知领养者' }}</p>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4 text-sm text-slate-600 dark:text-slate-300">
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">类别</p>
                <p class="font-medium text-slate-900 dark:text-white">{{ petData?.category || '未知' }}</p>
              </div>
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">品种</p>
                <p class="font-medium text-slate-900 dark:text-white">{{ petData?.breed || '未知' }}</p>
              </div>
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">性别</p>
                <p class="font-medium text-slate-900 dark:text-white">{{ petData?.gender || '未知' }}</p>
              </div>
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">年龄</p>
                <p class="font-medium text-slate-900 dark:text-white">{{ petData?.age || '未知' }}</p>
              </div>
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">绝育</p>
                <p class="font-medium text-slate-900 dark:text-white">{{ petData?.neutered || '未知' }}</p>
              </div>
              <div class="col-span-2">
                <p class="text-xs uppercase tracking-wide text-slate-400 mb-1.5">所在地</p>
                <p class="font-medium text-slate-900 dark:text-white">
                  {{
                    [petData?.province, petData?.city, petData?.currentLocation]
                      .filter(Boolean)
                      .join(' · ') || '未知'
                  }}
                </p>
              </div>
            </div>
            <div class="space-y-2">
              <p class="text-xs uppercase tracking-wide text-slate-400">宠物简介</p>
              <p class="text-sm leading-relaxed text-slate-600 dark:text-slate-300">
                {{ petData?.description || '暂无简介' }}
              </p>
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
import { ref, watch } from 'vue';

interface PetData {
  id?: number;
  name?: string;
  breed?: string;
  category?: string;
  gender?: string;
  age?: string;
  neutered?: string;
  description?: string;
  mainImage?: string;
  galleryImages?: string[];
  adopterName?: string;
  adopterAvatar?: string;
  province?: string;
  city?: string;
  currentLocation?: string;
}

const props = defineProps<{
  visible: boolean;
  petData?: PetData;
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

