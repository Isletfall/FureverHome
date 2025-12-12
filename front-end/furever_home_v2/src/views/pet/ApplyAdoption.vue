<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SuccessModal from '../../components/common/SuccessModal.vue'
import ErrorModal from '../../components/common/ErrorModal.vue'
import ConfirmModal from '../../components/common/ConfirmModal.vue'
import RegionCascader from '../../components/common/RegionCascader.vue'
import { submitAdopt } from '@/api/adoptApi'
import { getAnimalDetail } from '@/api/animalApi'

const route = useRoute()
const router = useRouter()

const petId = computed(() => Number(route.params.id))

// 表单数据
const form = ref({
  fullName: '',
  address: '',
  reason: '',
  contactPhone: '',
  contactEmail: '',
  province: '',
  city: ''
})

const agreedToTerms = ref(false)
const showSuccessModal = ref(false)
const showErrorModal = ref(false)
const showCancelConfirmModal = ref(false)
const errorMessage = ref('')

// 宠物信息（从后端接口获取）
// 月龄转换为“X岁Y个月”展示
function formatAge(ageInMonths: number | null | undefined): string {
  if (ageInMonths == null || isNaN(ageInMonths)) return ''
  const months = Math.max(0, Math.floor(ageInMonths))
  const years = Math.floor(months / 12)
  const restMonths = months % 12
  if (years === 0) return `${restMonths}个月`
  if (restMonths === 0) return `${years}岁`
  return `${years}岁${restMonths}个月`
}

const petInfo = ref({
  name: '',
  breed: '',
  species: '',
  age: '',
  gender: ''
})

const loadPetInfo = async () => {
  try {
    if (!petId.value) return
    const res = await getAnimalDetail(petId.value)
    if (res.code === 200 && res.data) {
      const item: any = res.data
      petInfo.value = {
        name: item.animalName || '',
        breed: item.breed || '',
        species: item.species || '',
        // 接口返回 animalAge 为月龄，这里统一展示为“X岁Y个月”
        age: formatAge(item.animalAge),
        gender: item.gender || ''
      }
    } else {
      console.error('获取动物详情失败', res)
    }
  } catch (err) {
    console.error('获取动物详情接口异常', err)
  }
}

onMounted(() => {
  loadPetInfo()
  // 检查是否有重新申请的数据
  const reapplyDataStr = sessionStorage.getItem('reapplyData')
  if (reapplyDataStr) {
    try {
      const reapplyData = JSON.parse(reapplyDataStr)
      if (reapplyData.animalId === petId.value) {
        // 自动填充表单
        form.value.fullName = reapplyData.fullName || ''
        form.value.address = reapplyData.address || ''
        form.value.reason = reapplyData.reason || ''
        form.value.contactPhone = reapplyData.contactPhone || ''
        form.value.contactEmail = reapplyData.contactEmail || ''
        form.value.province = reapplyData.province || ''
        form.value.city = reapplyData.city || ''
        // 清除 sessionStorage 中的数据
        sessionStorage.removeItem('reapplyData')
      }
    } catch (e) {
      console.error('解析重新申请数据失败', e)
      sessionStorage.removeItem('reapplyData')
    }
  }
})

const goBack = () => {
  router.back()
}

const cancel = () => {
  showCancelConfirmModal.value = true
}

const confirmCancel = () => {
  router.back()
  showCancelConfirmModal.value = false
}

const closeCancelConfirm = () => {
  showCancelConfirmModal.value = false
}

const validateForm = (): boolean => {
  const requiredFields = [
    { key: 'fullName', label: '姓名' },
    { key: 'address', label: '居住地址' },
    { key: 'reason', label: '申请领养理由' },
    { key: 'contactPhone', label: '联系电话' },
    { key: 'contactEmail', label: '电子邮箱' },
    { key: 'province', label: '所在省份' },
    { key: 'city', label: '所在城市' },
  ]

  const missingFields: string[] = []
  requiredFields.forEach(field => {
    if (!form.value[field.key as keyof typeof form.value]?.trim()) {
      missingFields.push(field.label)
    }
  })

  if (missingFields.length > 0) {
    errorMessage.value = `请填写以下信息：${missingFields.join('、')}`
    showErrorModal.value = true
    return false
  }

  if (!agreedToTerms.value) {
    errorMessage.value = '请阅读并同意领养协议'
    showErrorModal.value = true
    return false
  }

  return true
}

const submitApplication = async () => {
  if (!validateForm()) {
    return
  }

  try {
    const reqBody = {
      adoptReason: form.value.reason,
      animalId: petId.value,
      city: form.value.city,
      email: form.value.contactEmail,
      livingLocation: form.value.address,
      phone: form.value.contactPhone,
      province: form.value.province,
      userName: form.value.fullName,
    }

    await submitAdopt(reqBody)
    showSuccessModal.value = true
  } catch (err: any) {
    errorMessage.value = err?.message || '提交领养申请失败，请稍后重试'
    showErrorModal.value = true
  }
}

const closeSuccessModal = () => {
  showSuccessModal.value = false
  // 跳转到用户中心我的申请页面，并添加刷新参数
  router.push({ path: '/user-center', query: { menu: 'applications', refresh: 'true' } })
}

const closeErrorModal = () => {
  showErrorModal.value = false
}
</script>

<template>
  <div class="min-h-screen bg-[#F8F9FB]">
    <div class="max-w-[1200px] mx-auto py-8 px-5">
      <!-- 返回按钮 -->
      <button
        class="inline-flex items-center gap-2 px-4 py-2.5 bg-[#FF8C00] text-white rounded-lg text-sm font-medium mb-5 transition-all hover:bg-[#e6722a] hover:-translate-y-0.5 hover:shadow-lg"
        @click="goBack"
      >
        <i class="fa-solid fa-arrow-left"></i> 返回宠物详情
      </button>

      <div class="bg-white rounded-xl shadow-sm p-8">
        <h1 class="text-[28px] font-bold mb-2 text-[#333333] text-center">领养申请确认</h1>
        <p class="text-[#666666] mb-8 text-center text-sm">请仔细阅读并确认以下领养须知</p>

        <!-- 信息核对 -->
        <div class="text-xl font-bold mb-5 text-[#333333] pb-2.5 border-b-2 border-[#FFE4CF]">
          信息核对
        </div>
        <div class="bg-white p-5 rounded-xl shadow-sm flex items-center gap-5 mb-5">
          <div class="w-20 h-20 rounded-full bg-[#FF8C00] flex items-center justify-center text-[28px] font-bold text-white">
            <i class="fa-solid fa-cat"></i>
          </div>
          <div>
            <h3 class="text-xl mb-1.5 text-[#333333] font-semibold">
              您正在申请领养{{ petInfo.name }}
            </h3>
            <p class="text-[#666666] text-sm">
              {{ petInfo.breed }} · {{ petInfo.species }} · {{ petInfo.age }} · {{ petInfo.gender }}
            </p>
          </div>
        </div>

        <!-- 领养须知 -->
        <div class="text-xl font-bold mb-5 text-[#333333] pb-2.5 border-b-2 border-[#FFE4CF]">
          领养须知
        </div>
        <div class="bg-white rounded-xl p-5 mb-5 shadow-sm border-l-4 border-[#FF8C00]">
          <div class="font-semibold mb-3 text-[#333333] text-base">领养流程：</div>
          <ol class="ml-5 mb-4 list-decimal">
            <li class="mb-2 leading-relaxed">提交申请：在线填写并提交您的领养申请。</li>
            <li class="mb-2 leading-relaxed">签订协议：双方达成一致后，签订正式的领养协议。</li>
          </ol>

          <div class="font-semibold mb-3 text-[#333333] text-base">领养条件：</div>
          <ul class="ml-5 mb-4 list-disc">
            <li class="mb-2 leading-relaxed">年满18周岁，有稳定的收入来源</li>
            <li class="mb-2 leading-relaxed">有固定的住所，允许饲养宠物</li>
            <li class="mb-2 leading-relaxed">家庭成员同意领养宠物</li>
            <li class="mb-2 leading-relaxed">承诺科学喂养，定期接种疫苗</li>
            <li class="mb-2 leading-relaxed">同意接受领养后的回访</li>
          </ul>

          <div class="flex items-start gap-2.5 my-5 p-4 bg-white rounded-lg border border-[#e0e0e0]">
            <input
              id="agreement"
              v-model="agreedToTerms"
              type="checkbox"
              class="mt-1 scale-125"
            />
            <label for="agreement" class="leading-relaxed">
              我已阅读并同意<a href="#" class="text-[#FF8C00] font-medium hover:underline">《领养协议》</a>中的所有条款
            </label>
          </div>
        </div>

        <!-- 领养人信息 -->
        <div class="text-xl font-bold mb-5 text-[#333333] pb-2.5 border-b-2 border-[#FFE4CF]">
          领养人信息
        </div>
        <div class="mb-6">
          <label for="fullName" class="block mb-2 font-semibold text-[#333333]">姓名</label>
          <input
            id="fullName"
            v-model="form.fullName"
            type="text"
            placeholder="请输入您的真实姓名"
            class="w-full px-4 py-3 border border-[#e0e0e0] rounded-lg text-base transition-all focus:outline-none focus:border-[#FF8C00] focus:shadow-[0_0_0_3px_rgba(255,140,0,0.1)]"
          />
        </div>

        <div class="mb-6">
          <label class="block mb-2 font-semibold text-[#333333]">所在地区</label>
          <RegionCascader
            v-model:province="form.province"
            v-model:city="form.city"
          />
        </div>

        <div class="mb-6">
          <label for="address" class="block mb-2 font-semibold text-[#333333]">居住地址</label>
          <input
            id="address"
            v-model="form.address"
            type="text"
            placeholder="请输入您的详细居住地址"
            class="w-full px-4 py-3 border border-[#e0e0e0] rounded-lg text-base transition-all focus:outline-none focus:border-[#FF8C00] focus:shadow-[0_0_0_3px_rgba(255,140,0,0.1)]"
          />
        </div>

        <!-- 申请理由 -->
        <div class="mb-6">
          <label for="reason" class="block mb-2 font-semibold text-[#333333]">申请领养理由</label>
          <textarea
            id="reason"
            v-model="form.reason"
            rows="4"
            placeholder="请简单介绍一下您的养宠经验、生活环境以及为什么想要领养这只小动物"
            class="w-full px-4 py-3 border border-[#e0e0e0] rounded-lg text-base transition-all resize-none focus:outline-none focus:border-[#FF8C00] focus:shadow-[0_0_0_3px_rgba(255,140,0,0.1)]"
          />
        </div>

        <!-- 联系方式 -->
        <div class="text-xl font-bold mb-5 text-[#333333] pb-2.5 border-b-2 border-[#FFE4CF]">
          联系方式
        </div>
        <div class="bg-white rounded-xl p-5 mb-5 shadow-sm border-l-4 border-[#4A90E2]">
          <div class="mb-6">
            <label for="contactPhone" class="block mb-2 font-semibold text-[#333333]">联系电话</label>
            <input
              id="contactPhone"
              v-model="form.contactPhone"
              type="tel"
              placeholder="请输入您的联系电话"
              class="w-full px-4 py-3 border border-[#e0e0e0] rounded-lg text-base transition-all focus:outline-none focus:border-[#FF8C00] focus:shadow-[0_0_0_3px_rgba(255,140,0,0.1)]"
            />
          </div>

          <div class="mb-0">
            <label for="contactEmail" class="block mb-2 font-semibold text-[#333333]">电子邮箱</label>
            <input
              id="contactEmail"
              v-model="form.contactEmail"
              type="email"
              placeholder="请输入您的电子邮箱"
              class="w-full px-4 py-3 border border-[#e0e0e0] rounded-lg text-base transition-all focus:outline-none focus:border-[#FF8C00] focus:shadow-[0_0_0_3px_rgba(255,140,0,0.1)]"
            />
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex justify-between mt-8 gap-4">
          <button
            type="button"
            class="flex-1 flex items-center justify-center px-7 py-3.5 rounded-lg font-semibold transition-all text-center text-base cursor-pointer bg-[#f0f0f0] text-[#666666] border border-[#e0e0e0] hover:bg-[#e0e0e0] hover:-translate-y-0.5 hover:shadow-lg"
            @click="cancel"
          >
            再想想
          </button>
          <button
            type="button"
            class="flex-1 flex items-center justify-center px-7 py-3.5 rounded-lg font-semibold transition-all text-center text-base cursor-pointer bg-[#FF8C00] text-white shadow-sm hover:bg-[#e6722a] hover:-translate-y-0.5 hover:shadow-lg"
            @click="submitApplication"
          >
            确认提交申请
          </button>
        </div>
      </div>
    </div>

    <!-- 成功模态框 -->
    <SuccessModal
      :visible="showSuccessModal"
      title="提交申请成功！"
      message="您的领养申请已提交成功！请等待管理员审核通过后，再由被申请者确认是否同意领养。"
      @close="closeSuccessModal"
    />

    <!-- 错误模态框 -->
    <ErrorModal
      :visible="showErrorModal"
      title="申请失败"
      :message="errorMessage"
      @close="closeErrorModal"
    />

    <!-- 取消确认弹窗 -->
    <ConfirmModal
      :visible="showCancelConfirmModal"
      title="确认操作"
      message="确定要取消领养申请吗？您填写的信息将不会被保存。"
      @confirm="confirmCancel"
      @cancel="closeCancelConfirm"
    />

    <!-- Footer -->
    <footer class="bg-[#1E293B] text-white py-10 px-[5%] mt-16">
      <div class="max-w-[1200px] mx-auto flex flex-wrap gap-10">
        <div class="flex-1 min-w-[260px]">
          <h3 class="mb-4 text-lg font-semibold">数据统计</h3>
          <p class="text-sm leading-relaxed mb-3">
            我们与多家救助站建立了长期合作关系，致力于为流浪动物提供更好的临时安置和长期领养服务。
          </p>
          <a href="#" class="text-[#FF8C00] text-sm hover:underline">查看合作救助站 →</a>
        </div>
        <div class="flex-1 min-w-[260px]">
          <h3 class="mb-4 text-lg font-semibold">联系开发团队</h3>
          <p class="text-sm leading-relaxed mb-3">
            如果您有任何问题、建议或合作意向，请随时联系我们的开发团队。
          </p>
          <div class="flex gap-3 mt-2.5">
            <i class="fa-brands fa-weixin text-2xl hover:text-[#FF8C00] cursor-pointer transition-colors"></i>
            <i class="fa-brands fa-qq text-2xl hover:text-[#FF8C00] cursor-pointer transition-colors"></i>
            <i class="fa-brands fa-weibo text-2xl hover:text-[#FF8C00] cursor-pointer transition-colors"></i>
          </div>
        </div>
      </div>
      <div class="text-center pt-6 mt-6 border-t border-white/10 text-sm">
        2025 FUREVERHOME 流浪动物领养平台 - 让每个生命都有温暖的家
      </div>
    </footer>
  </div>
</template>
