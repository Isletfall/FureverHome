<script setup lang="ts">
interface Pet {
  id: number
  name: string
  photoText: string
  description: string
  status: 'urgent' | 'waiting'
  fosterer: string
  days: number
}

const props = defineProps<{
  pet: Pet
}>()
</script>

<template>
  <article class="bg-white dark:bg-zinc-900 rounded-lg shadow-sm overflow-hidden flex flex-col">
    <div class="bg-orange-100 dark:bg-orange-900/20 h-56 flex items-center justify-center">
      <span class="text-gray-400 dark:text-gray-500">
        {{ props.pet.photoText }}
      </span>
    </div>

    <div class="p-6 flex-grow flex flex-col">
      <h3 class="text-xl font-bold text-gray-800 dark:text-gray-100">
        {{ props.pet.name }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
        {{ props.pet.description }}
      </p>

      <div class="my-4">
        <span
          v-if="props.pet.status === 'urgent'"
          class="text-xs font-semibold inline-block py-1 px-2.5 uppercase rounded-full text-red-600 bg-red-200 dark:bg-red-900/50 dark:text-red-400"
        >
          急需领养
        </span>
        <span
          v-else
          class="text-xs font-semibold inline-block py-1 px-2.5 uppercase rounded-full text-yellow-800 dark:text-yellow-300 bg-yellow-200 dark:bg-yellow-900/50"
        >
          等待领养
        </span>
      </div>

      <p class="text-sm text-gray-600 dark:text-gray-300">
        临时收养者: {{ props.pet.fosterer }}
      </p>

      <div class="mt-auto pt-4 flex justify-between items-center">
        <span class="text-sm text-gray-400 dark:text-gray-500">
          {{ props.pet.days }}天
        </span>
        <button
          class="flex items-center justify-center gap-2 px-4 py-2 text-sm rounded-md bg-primary text-white hover:bg-orange-500 transition-colors"
        >
          <span class="material-icons text-base">chat_bubble_outline</span>
          <span>联系TA</span>
        </button>
      </div>
    </div>
  </article>
</template>
