<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black/50 z-[3000] flex items-center justify-center"
    @click="handleClose"
  >
    <div
      class="bg-white rounded-xl p-8 max-w-[400px] w-[90%] text-center shadow-lg"
      @click.stop
    >
      <h3 class="text-[#FF8C00] mb-4 text-xl font-bold">信息已成功提交！</h3>
      <p class="mb-5 leading-relaxed text-[#333333]">您的宠物信息已提交，平台将在24小时内完成审核。审核结果将通过站内信通知您。您也可以随时在【个人主页】-【我的内容】中查看进度。</p>
      <button
        class="px-5 py-2.5 bg-[#FF8C00] text-white rounded-lg font-semibold transition-all hover:bg-[#e6722a]"
        @click="handleClose"
      >
        确定
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  visible: boolean
}

defineProps<Props>()

const router = useRouter()

const handleClose = () => {
  router.push('/')
}
</script>

