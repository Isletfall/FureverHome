<template>
  <div class="relative inline-block text-left">
    <!-- 触发按钮 -->
    <button
      type="button"
      class="px-3 py-1.5 border border-[#ddd] rounded-full text-sm bg-white cursor-pointer transition-colors hover:border-[#FF8C42] focus:outline-none focus:border-[#FF8C42] min-w-[140px] text-left"
      @click="toggleDropdown"
    >
      <span v-if="displayLabel">{{ displayLabel }}</span>
      <span v-else class="text-[#9CA3AF]">请选择地区</span>
      <i class="fa-solid fa-chevron-down ml-2 text-[10px]"></i>
    </button>

    <!-- 下拉面板 -->
    <div
      v-if="open"
      class="absolute mt-2 w-[320px] bg-white border border-[#E5E7EB] rounded-xl shadow-lg z-50 overflow-hidden"
    >
      <div class="grid grid-cols-2 divide-x divide-[#E5E7EB]">
        <!-- 省份列表 -->
        <div class="max-h-56 overflow-auto">
          <div
            class="px-3 py-2 text-sm cursor-pointer hover:bg-[#FFF7ED] text-[#6B7280] border-b border-[#F3F4F6]"
            @click="clearSelection"
          >
            全部地区
          </div>
          <div
            v-for="p in provinceCityOptions"
            :key="p.value"
            class="px-3 py-2 text-sm cursor-pointer hover:bg-[#FFF7ED]"
            :class="p.value === currentProvince ? 'bg-[#FFF7ED] text-[#FF8C42]' : 'text-[#374151]'"
            @click="selectProvince(p.value)"
          >
            {{ p.label }}
          </div>
        </div>
        <!-- 城市列表 -->
        <div class="max-h-56 overflow-auto">
          <div
            v-if="currentCities.length"
            class="px-3 py-2 text-sm cursor-pointer hover:bg-[#FFF7ED] text-[#6B7280] border-b border-[#F3F4F6]"
            @click="clearCity"
          >
            全部城市
          </div>
          <div
            v-for="c in currentCities"
            :key="c.value"
            class="px-3 py-2 text-sm cursor-pointer hover:bg-[#FFF7ED]"
            :class="c.value === modelCity ? 'bg-[#FFF7ED] text-[#FF8C42]' : 'text-[#374151]'"
            @click="selectCity(c.value)"
          >
            {{ c.label }}
          </div>
          <div
            v-if="!currentCities.length"
            class="px-3 py-2 text-xs text-[#9CA3AF]"
          >
            请先选择省份
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { provinceCityOptions } from '@/constants/regions'

interface Props {
  province?: string
  city?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:province', value: string): void
  (e: 'update:city', value: string): void
}>()

const open = ref(false)

const modelProvince = computed({
  get: () => props.province || '',
  set: (val: string) => emit('update:province', val)
})

const modelCity = computed({
  get: () => props.city || '',
  set: (val: string) => emit('update:city', val)
})

const currentProvince = computed(() => modelProvince.value)

const currentCities = computed(() => {
  const p = provinceCityOptions.find(p => p.value === currentProvince.value)
  return p ? p.cities : []
})

const displayLabel = computed(() => {
  if (modelCity.value && modelProvince.value) {
    return `${modelProvince.value} · ${modelCity.value}`
  }
  if (modelProvince.value) {
    return modelProvince.value
  }
  return ''
})

function toggleDropdown() {
  open.value = !open.value
}

function selectProvince(value: string) {
  modelProvince.value = value
  modelCity.value = ''
}

function selectCity(value: string) {
  modelCity.value = value
  open.value = false
}

function clearSelection() {
  modelProvince.value = ''
  modelCity.value = ''
  open.value = false
}

function clearCity() {
  modelCity.value = ''
  // 只清空城市但保留已选省份，同时收起下拉
  open.value = false
}

watch(
  () => props.province,
  newVal => {
    if (!newVal) {
      modelCity.value = ''
    }
  }
)
</script>

<style scoped>
</style>
