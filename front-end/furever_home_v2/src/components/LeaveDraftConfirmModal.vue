<template>
  <div
    v-if="visible"
    class="fixed inset-0 z-50 flex h-full w-full items-center justify-center bg-black/60 px-4 py-5"
  >
    <div class="flex h-full max-h-[320px] w-full max-w-md grow flex-col">
      <div class="flex flex-1 justify-center">
        <div
          class="flex w-full flex-col items-center rounded-xl bg-white dark:bg-background-dark p-6 shadow-2xl"
        >
          <div class="flex flex-col items-center gap-4 text-center w-full">
            <p class="text-xl font-bold text-gray-900 dark:text-gray-100">
              返回前要保存草稿吗？
            </p>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              如果不保存草稿，当前已填写的信息将在离开本页后丢失。
            </p>

            <div class="mt-4 flex w-full flex-col gap-3 sm:flex-row">
              <button
                type="button"
                class="flex-1 h-11 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-100 text-sm font-medium"
                @click="$emit('leave-without-save')"
              >
                不保存，直接返回
              </button>
              <button
                type="button"
                class="flex-1 h-11 rounded-lg bg-orange-500 hover:bg-orange-600 text-white text-sm font-semibold shadow-sm"
                @click="$emit('save-and-leave')"
              >
                保存草稿并返回
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  visible: boolean
}

defineProps<Props>()
</script>
