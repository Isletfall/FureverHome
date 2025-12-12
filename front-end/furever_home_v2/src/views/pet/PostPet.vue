<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import SuccessModal from '../../components/common/SuccessModal.vue'
import ErrorModal from '../../components/common/ErrorModal.vue'
import ImageViewer from '../../components/common/ImageViewer.vue'
import ImageCropper from '../../components/common/ImageCropper.vue'
import { provinceCityOptions } from '@/constants/regions'
import { createAnimal, AdoptionStatus, Gender, IsSterilized, Species } from '@/api/animalApi'
import { uploadImage } from '@/api/storageApi'

interface PetForm {
  name: string
  age: string
  gender: string
  species: string
  breed: string
  sterilized: string
  province: string
  city: string
  detailAddress: string
  location: string
  healthStatus: string
  story: string
  phone: string
  email: string
}

interface LocalUploadFile {
  file: File
  preview: string
}

const router = useRouter()

const form = ref<PetForm>({
  name: '',
  age: '',
  gender: '',
  species: '',
  breed: '',
  sterilized: '',
  province: '',
  city: '',
  detailAddress: '',
  location: '',
  healthStatus: '',
  story: '',
  phone: '',
  email: ''
})

const cityOptions = computed(() => {
  const province = provinceCityOptions.find(
    p => p.value === form.value.province
  )
  return province ? province.cities : []
})

const uploadedFiles = ref<LocalUploadFile[]>([])
const showSuccessModal = ref(false)
const showErrorModal = ref(false)
const showFileLimitModal = ref(false)
const errorMessage = ref('')

// 图片裁剪相关
const showCropper = ref(false)
const currentCropFile = ref<File | null>(null)

watch(
  () => form.value.province,
  () => {
    form.value.city = ''
  }
)

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (!target.files || target.files.length === 0) return
  
  const file = target.files[0]
  if (!file) return
  
  // 检查文件数量限制
  if (uploadedFiles.value.length >= 5) {
    showFileLimitModal.value = true
    target.value = '' // 清空 input
    return
  }
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    errorMessage.value = '请选择图片文件'
    showErrorModal.value = true
    target.value = ''
    return
  }
  
  // 打开裁剪弹窗
  currentCropFile.value = file
  showCropper.value = true
  target.value = '' // 清空 input，允许重复选择同一文件
}

const handleCropConfirm = (croppedFile: File) => {
  const preview = URL.createObjectURL(croppedFile)
  uploadedFiles.value.push({ file: croppedFile, preview })
  currentCropFile.value = null
}

const handleCropCancel = () => {
  currentCropFile.value = null
}

const removeFile = (index: number) => {
  uploadedFiles.value.splice(index, 1)
}

const getFileUrl = (item: LocalUploadFile) => {
  return item.preview
}

const showImageViewer = ref(false)
const imageViewerIndex = ref(0)

const openImageViewer = (index: number) => {
  imageViewerIndex.value = index
  showImageViewer.value = true
}

const closeImageViewer = () => {
  showImageViewer.value = false
}

const getImageUrls = () => {
  return uploadedFiles.value.map(item => item.preview)
}

const handleAgeInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const numericValue = target.value.replace(/\D+/g, '')
  form.value.age = numericValue
}

const uploadAllImages = async (): Promise<string[]> => {
  const urls: string[] = []
  for (const item of uploadedFiles.value) {
    try {
      const res = await uploadImage(item.file)
      if ((res.code === 0 || res.code === 200) && res.data) {
        urls.push(res.data)
      } else {
        throw new Error(res.message || '图片上传失败，请稍后重试')
      }
    } catch (error: any) {
      throw new Error(error?.message || '图片上传失败，请稍后重试')
    }
  }
  return urls
}

const triggerFileInput = () => {
  const input = document.getElementById('fileInput') as HTMLInputElement
  input?.click()
}

const validateForm = (): boolean => {
  const requiredFields = [
    { key: 'name', label: '动物名称' },
    { key: 'age', label: '年龄' },
    { key: 'gender', label: '性别' },
    { key: 'species', label: '动物种类' },
    { key: 'breed', label: '动物品种' },
    { key: 'sterilized', label: '是否绝育' },
    { key: 'province', label: '所在省份' },
    { key: 'city', label: '所在城市' },
    { key: 'detailAddress', label: '详细地址' },
    { key: 'phone', label: '联系电话' },
    { key: 'email', label: '电子邮箱' }
  ]

  const missingFields: string[] = []
  requiredFields.forEach(field => {
    if (!form.value[field.key as keyof PetForm]?.trim()) {
      missingFields.push(field.label)
    }
  })

  if (missingFields.length > 0) {
    errorMessage.value = `请完善以下必填信息：${missingFields.join('、')}`
    showErrorModal.value = true
    return false
  }

  // 验证年龄是否为阿拉伯数字
  const ageValue = form.value.age.trim()
  if (ageValue && !/^\d+$/.test(ageValue)) {
    errorMessage.value = '年龄必须为阿拉伯数字，请输入正确的年龄（以月为单位）'
    showErrorModal.value = true
    return false
  }

  // 验证联系电话为 11 位数字
  const phoneValue = form.value.phone.trim()
  if (!/^\d{11}$/.test(phoneValue)) {
    errorMessage.value = '联系电话需为11位数字'
    showErrorModal.value = true
    return false
  }

  return true
}

const submitForm = async () => {
  if (!validateForm()) {
    return
  }

  // 只用详细地址存入 currentLocation，省/市单独存储，避免重复拼接
  const currentLocation = (form.value.detailAddress || '').trim()

  try {
    const animalAge = Number(form.value.age)
    const photoUrls = await uploadAllImages()

    const reqBody = {
      adoptionStatus: AdoptionStatus.短期领养,
      animalAge: Number.isNaN(animalAge) ? undefined : animalAge,
      animalName: form.value.name,
      breed: form.value.breed || undefined,
      city: form.value.city,
      contactEmail: form.value.email,
      contactPhone: form.value.phone,
      currentLocation,
      gender: form.value.gender as Gender,
      healthStatus: form.value.healthStatus || '健康',
      isSterilized: form.value.sterilized as IsSterilized,
      photoUrls,
      province: form.value.province,
      shortDescription: form.value.story ? form.value.story.slice(0, 100) : undefined,
      species: form.value.species as Species,
    }

    await createAnimal(reqBody)
    showSuccessModal.value = true
  } catch (err: any) {
    errorMessage.value = err?.message || '发布动物信息失败，请稍后重试'
    showErrorModal.value = true
  }
}

const closeSuccessModal = () => {
  showSuccessModal.value = false
  // 跳转到用户中心我的宠物页面，并添加刷新参数
  router.push({ path: '/user-center', query: { menu: 'pets', refresh: 'true' } })
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
        @click="router.back()" 
        class="mb-6 flex items-center gap-2 text-[#FF8C00] hover:text-[#e6722a] transition-colors"
        title="返回"
      >
        <i class="fa-solid fa-arrow-left text-lg"></i>
        <span class="font-medium">返回</span>
      </button>

      <main class="bg-white rounded-xl p-8 shadow-sm">
        <h1 class="text-[28px] font-bold text-center mb-8 text-[#333333]">
          发布待领养动物信息
        </h1>

        <form @submit.prevent="submitForm">
          <!-- 宠物基本信息 -->
          <div class="mb-8 pb-5 border-b border-[#E5E7EB]">
            <h2 class="text-xl font-bold mb-5 text-[#333333] flex items-center gap-2.5">
              <i class="fa-solid fa-paw text-[#FF8C00]"></i>
              宠物基本信息
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  动物名称 <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="form.name"
                  type="text"
                  placeholder="例如：豆豆"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                />
              </div>

              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  年龄(以月为单位) <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="form.age"
                  type="text"
                  placeholder="例如：5"
                  inputmode="numeric"
                  pattern="[0-9]*"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                  @input="handleAgeInput"
                />
              </div>

              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  性别 <span class="text-[#EF4444]">*</span>
                </label>
                <select
                  v-model="form.gender"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                >
                  <option value="">请选择</option>
                  <option value="公">公</option>
                  <option value="母">母</option>
                  <option value="未知">未知</option>
                </select>
              </div>

              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  动物种类 <span class="text-[#EF4444]">*</span>
                </label>
                <select
                  v-model="form.species"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                >
                  <option value="">请选择</option>
                  <option value="狗">狗</option>
                  <option value="猫">猫</option>
                  <option value="兔子">兔子</option>
                  <option value="仓鼠">仓鼠</option>
                  <option value="鸟类">鸟类</option>
                  <option value="鱼类">鱼类</option>
                  <option value="龟类">龟类</option>
                  <option value="其他">其他</option>
                </select>
              </div>

              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  动物品种 <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="form.breed"
                  type="text"
                  placeholder="例如：拉布拉多、布偶猫"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                />
              </div>

              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  是否绝育 <span class="text-[#EF4444]">*</span>
                </label>
                <select
                  v-model="form.sterilized"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                >
                  <option value="">请选择</option>
                  <option value="是">是</option>
                  <option value="否">否</option>
                  <option value="未知">未知</option>
                </select>
              </div>

              <div class="mb-5 md:col-span-2">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  目前位置 <span class="text-[#EF4444]">*</span>
                </label>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
                  <select
                    v-model="form.province"
                    class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                  >
                    <option value="">请选择省份</option>
                    <option
                      v-for="p in provinceCityOptions"
                      :key="p.value"
                      :value="p.value"
                    >
                      {{ p.label }}
                    </option>
                  </select>
                  <select
                    v-model="form.city"
                    class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                  >
                    <option value="">请选择城市</option>
                    <option
                      v-for="c in cityOptions"
                      :key="c.value"
                      :value="c.value"
                    >
                      {{ c.label }}
                    </option>
                  </select>
                  <input
                    v-model="form.detailAddress"
                    type="text"
                    placeholder="详细地址，如街道、小区、楼栋、门牌号等"
                    class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 照片/视频上传 -->
          <div class="mb-8 pb-5 border-b border-[#E5E7EB]">
            <h2 class="text-xl font-bold mb-5 text-[#333333] flex items-center gap-2.5">
              <i class="fa-solid fa-image text-[#FF8C00]"></i>
              照片上传
            </h2>
            <div
              class="border-2 border-dashed border-[#E5E7EB] rounded-xl p-10 text-center cursor-pointer transition-all hover:border-[#FF8C00] hover:bg-[rgba(255,140,0,0.05)]"
              @click="triggerFileInput"
            >
              <input
                id="fileInput"
                type="file"
                multiple
                accept=".jpg,.jpeg,.png,.webp"
                class="hidden"
                @change="handleFileUpload"
              />
              <div class="text-[40px] mb-2.5 text-[#666666]">
                <i class="fa-solid fa-cloud-arrow-up"></i>
              </div>
              <div class="text-[#666666] mb-2.5">点击上传文件到这里</div>
              <div class="text-sm text-[#9CA3AF]">支持 JPG、JPEG、PNG、WEBP 格式，最多5张照片</div>
            </div>
            <div v-if="uploadedFiles.length > 0" class="mt-4 flex flex-wrap gap-3">
              <div
                v-for="(file, index) in uploadedFiles"
                :key="index"
                class="relative w-32 aspect-[4/3] rounded-lg overflow-hidden border border-[#E5E7EB] cursor-pointer"
                @click="openImageViewer(index)"
              >
                <img
                  :src="getFileUrl(file)"
                  :alt="file.file.name"
                  class="absolute inset-0 w-full h-full object-cover"
                />
                <button
                  type="button"
                  class="absolute top-1 right-1 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center text-xs hover:bg-red-600 z-10"
                  @click.stop="removeFile(index)"
                >
                  ×
                </button>
              </div>
            </div>
          </div>

          <!-- 详细描述 -->
          <div class="mb-8 pb-5 border-b border-[#E5E7EB]">
            <h2 class="text-xl font-bold mb-5 text-[#333333] flex items-center gap-2.5">
              <i class="fa-solid fa-book text-[#FF8C00]"></i>
              详细描述（救助故事与宠物的健康状态）
            </h2>
            <div class="mb-5">
              <textarea
                v-model="form.story"
                placeholder="请详细描述救助的背景、故事和任何特殊需求。"
                class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm min-h-[120px] resize-y transition-colors focus:border-[#FF8C00] focus:outline-none"
              ></textarea>
            </div>
          </div>

          <!-- 联系信息 -->
          <div class="mb-8 pb-5 border-b border-[#E5E7EB]">
            <h2 class="text-xl font-bold mb-5 text-[#333333] flex items-center gap-2.5">
              <i class="fa-solid fa-address-book text-[#FF8C00]"></i>
              联系信息
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  联系电话 <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="form.phone"
                  type="tel"
                  placeholder="13812345678"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                />
              </div>

              <div class="mb-5">
                <label class="block mb-2 font-semibold text-[#666666] text-sm">
                  电子邮箱 <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="form.email"
                  type="email"
                  placeholder="user@example.com"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm transition-colors focus:border-[#FF8C00] focus:outline-none"
                />
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex justify-center gap-5 mt-8">
            <button
              type="submit"
              class="px-8 py-3 rounded-lg text-base font-semibold cursor-pointer transition-all border-none bg-[#FF8C00] text-white hover:bg-[#E67A2A] hover:-translate-y-0.5 hover:shadow-[0_4px_8px_rgba(255,140,0,0.3)]"
            >
              提交审核
            </button>
          </div>
        </form>
      </main>
    </div>

    <!-- 成功模态框 -->
    <SuccessModal
      :visible="showSuccessModal"
      title="提交成功！"
      message="您发布的宠物信息将送往管理员审核，审核通过后将展示在平台上。"
      @close="closeSuccessModal"
    />

    <!-- 失败模态框 -->
    <ErrorModal
      :visible="showErrorModal"
      title="提交失败"
      :message="errorMessage"
      @close="closeErrorModal"
    />

    <!-- 文件数量限制提示 -->
    <ErrorModal
      :visible="showFileLimitModal"
      title="提示"
      message="最多只能上传5张照片"
      @close="showFileLimitModal = false"
    />

    <!-- 图片查看器 -->
    <ImageViewer
      :visible="showImageViewer"
      :images="getImageUrls()"
      :initial-index="imageViewerIndex"
      @close="closeImageViewer"
    />

    <!-- 图片裁剪器 -->
    <ImageCropper
      :visible="showCropper"
      :image-file="currentCropFile"
      @confirm="handleCropConfirm"
      @cancel="handleCropCancel"
      @update:visible="showCropper = $event"
    />
  </div>
</template>
