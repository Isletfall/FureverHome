<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-black/50 z-[3000] flex items-center justify-center p-4"
    @click="handleClose"
  >
    <div
      class="bg-white rounded-xl w-full max-w-[600px] max-h-[90vh] overflow-y-auto shadow-lg"
      @click.stop
    >
      <!-- 头部 -->
      <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
        <h2 class="text-xl font-bold" style="color: #FF8C00;">申请详情</h2>
        <button
          class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
          @click="handleClose"
        >
          <i class="fa-solid fa-times text-gray-500"></i>
        </button>
      </div>

      <!-- 内容 -->
      <div class="p-6">
        <!-- 申请人信息 -->
        <div class="mb-6">
          <div class="flex items-center gap-4 mb-4">
            <!-- 头像：优先显示图片，其次显示昵称首字 -->
            <div class="w-12 h-12 rounded-full overflow-hidden flex items-center justify-center text-white text-lg font-bold" style="background-color: #F3C697;">
              <img
                v-if="application.avatar"
                :src="application.avatar"
                alt="avatar"
                class="w-full h-full object-cover"
              />
              <span v-else>
                {{ application.requester?.charAt(0) || '用' }}
              </span>
            </div>
            <div>
              <h3 class="text-xl font-bold mb-1" style="color: #333333;">{{ application.requester }}</h3>
              <p class="text-sm text-gray-500">提交时间: {{ application.date }}</p>
            </div>
          </div>
        </div>

        <!-- 联系信息 -->
        <div class="mb-6">
          <h4 class="text-base font-bold mb-3" style="color: #333333;">联系信息</h4>
          <div class="space-y-2">
            <div class="flex">
              <span class="text-sm text-gray-600 w-20">手机号:</span>
              <span class="text-sm" style="color: #333333;">{{ application.phone || '未提供' }}</span>
            </div>
            <div class="flex">
              <span class="text-sm text-gray-600 w-20">邮箱:</span>
              <span class="text-sm" style="color: #333333;">{{ application.email || '未提供' }}</span>
            </div>
          </div>
        </div>

        <!-- 居住地址 -->
        <div class="mb-6">
          <h4 class="text-base font-bold mb-3" style="color: #333333;">居住地址</h4>
          <p class="text-sm" style="color: #333333;">{{ application.address || '未提供' }}</p>
        </div>

        <!-- 申请理由 -->
        <div class="mb-6">
          <h4 class="text-base font-bold mb-3" style="color: #333333;">申请理由</h4>
          <p class="text-sm leading-relaxed" style="color: #333333;">{{ application.reason }}</p>
        </div>

        <!-- 申请的宠物 -->
        <div class="mb-6">
          <h4 class="text-base font-bold mb-3" style="color: #333333;">申请的宠物</h4>
          <p class="text-sm" style="color: #333333;">{{ application.petName || application.title?.replace('对"', '').replace('"的领养申请', '') || '未知' }}</p>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="sticky bottom-0 bg-white border-t border-gray-200 px-6 py-4 flex justify-end gap-3">
        <button
          class="px-6 py-2.5 bg-[#FF8C00] text-white rounded-lg font-semibold transition-all hover:bg-[#e6722a]"
          @click="handleClose"
        >
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  visible: boolean
  application: {
    avatar?: string
    requester?: string
    date?: string
    phone?: string
    email?: string
    address?: string
    reason?: string
    petName?: string
    title?: string
  }
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
}>()

const handleClose = () => {
  emit('close')
}
</script>

