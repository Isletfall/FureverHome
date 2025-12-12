<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ImageViewer from '../../components/common/ImageViewer.vue'
import { getAnimalDetail } from '@/api/animalApi'
import ErrorModal from '../../components/common/ErrorModal.vue'

const route = useRoute()
const router = useRouter()

const petId = computed(() => Number(route.params.id))

interface Publisher {
  user_id: string
  username: string
  avatar_url: string
}

interface PetDetail {
  id: number
  animal_name: string
  photo_urls: string[]
  species: string
  breed: string
  gender: string
  animal_age: number
  health_status: string
  is_sterilized: string
  animal_location: string
  adoption_status: string
  short_description: string
  detail_info: string
  publisher: Publisher
}

const currentPet = ref<PetDetail | null>(null)
const currentUserId = ref<number | null>(null)
const showSelfContactModal = ref(false)
const showSelfAdoptModal = ref(false)

const mainImage = ref(0)
const showImageViewer = ref(false)
const imageViewerIndex = ref(0)

const publisherUserId = computed(() => {
  const id = Number(currentPet.value?.publisher.user_id)
  return Number.isNaN(id) ? null : id
})

const isSelfPublisher = computed(() => {
  return currentUserId.value != null && publisherUserId.value != null && currentUserId.value === publisherUserId.value
})

const goToFostererProfile = () => {
  // 跳转到临时收养者的个人主页
  const userId = Number(currentPet.value?.publisher.user_id) || 1
  router.push({ name: 'UserProfile', params: { userId } })
}

const contactFosterer = () => {
  if (isSelfPublisher.value) {
    showSelfContactModal.value = true
    return
  }
  // 跳转到沟通对接页面，并传递临时收养者的用户信息
  const userId = publisherUserId.value
  if (!userId) return
  router.push({
    name: 'Communication',
    query: {
      userId,
      userName: currentPet.value?.publisher.username || '',
      userAvatar: normalizeImageUrl(currentPet.value?.publisher.avatar_url)
    }
  })
}

const requestAdoption = () => {
  if (isSelfPublisher.value) {
    showSelfAdoptModal.value = true
    return
  }
  // 跳转到申请领养页面
  router.push({ name: 'ApplyAdoption', params: { id: petId.value } })
}

const openImageViewer = (index: number) => {
  imageViewerIndex.value = index
  showImageViewer.value = true
}

const closeImageViewer = () => {
  showImageViewer.value = false
}

// 处理头像加载错误
const handleAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement
  // 如果图片加载失败，隐藏图片，显示占位符
  if (img && img.parentElement) {
    img.style.display = 'none'
  }
}

// 规范化图片URL，确保相对路径正确添加前缀
const normalizeImageUrl = (url: string | undefined | null): string => {
  if (!url) return ''
  // 如果已经是完整的URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果已经以/api开头，直接返回
  if (url.startsWith('/api/')) {
    return url
  }
  // 否则添加/api/storage/image/前缀
  return `/api/storage/image/${url.replace(/^\/+/, '')}`
}

const loadPetDetail = async () => {
  try {
    if (!petId.value) return
    const res = await getAnimalDetail(petId.value)
    if (res.code === 200 && res.data) {
      const item: any = res.data
      // 处理 photoUrls 既可能是数组，也可能是 JSON 字符串的情况
      let photos: string[] = []
      if (Array.isArray(item.photoUrls)) {
        photos = item.photoUrls.map((url: string) => normalizeImageUrl(url))
      } else if (typeof item.photoUrls === 'string' && item.photoUrls.trim()) {
        try {
          const parsed = JSON.parse(item.photoUrls)
          if (Array.isArray(parsed)) {
            photos = parsed.map((url: string) => normalizeImageUrl(url))
          }
        } catch (e) {
          // ignore parse error
        }
      }
      
      currentPet.value = {
        id: item.animalId ?? petId.value,
        animal_name: item.animalName || '',
        photo_urls: photos,
        species: item.species || '',
        breed: item.breed || '',
        gender: item.gender || '',
        animal_age: item.animalAge ?? 0,
        health_status: item.healthStatus || '',
        is_sterilized: item.isSterilized || '',
        animal_location: item.currentLocation || '',
        adoption_status: item.adoptionStatus || '',
        short_description: item.shortDescription || '',
        detail_info: item.shortDescription || '',
        publisher: {
          user_id: String(item.userId ?? ''),
          // 使用后端返回的 userName 显示领养人昵称
          username: item.userName || '',
          // 使用后端返回的 userAvatar 作为头像地址，规范化处理
          avatar_url: normalizeImageUrl(item.userAvatar)
        }
      }
    } else {
      console.error('获取动物详情失败', res)
    }
  } catch (err) {
    console.error('获取动物详情接口异常', err)
  }
}

onMounted(() => {
  try {
    const cached = localStorage.getItem('currentUser')
    if (cached) {
      const parsed = JSON.parse(cached)
      if (parsed && typeof parsed.userId === 'number') {
        currentUserId.value = parsed.userId
      }
    }
  } catch (error) {
    console.warn('解析 currentUser 失败', error)
  }
  loadPetDetail()
})
</script>

<template>
  <div class="min-h-screen bg-[#F8F9FB]">
    <main v-if="currentPet" class="max-w-[1200px] mx-auto py-8 px-5">
      <!-- 返回按钮 -->
      <button 
        @click="router.back()" 
        class="mb-6 flex items-center gap-2 text-[#FF8C00] hover:text-[#e6722a] transition-colors"
        title="返回"
      >
        <i class="fa-solid fa-arrow-left text-lg"></i>
        <span class="font-medium">返回</span>
      </button>

      <!-- 宠物档案卡片 -->
      <div class="bg-white rounded-xl shadow-sm overflow-hidden mb-8 flex flex-col lg:flex-row">
        <!-- 左侧图片区域 -->
        <div class="w-full lg:w-[480px] flex flex-col items-center justify-center bg-white p-8">
          <!-- 图片容器 -->
          <div 
            class="relative w-full aspect-[4/3] bg-[#FFE4B5] cursor-pointer overflow-hidden rounded-xl"
            @click="openImageViewer(mainImage)"
          >
            <img
              v-if="currentPet.photo_urls[mainImage]"
              :src="currentPet.photo_urls[mainImage]"
              :alt="currentPet.animal_name"
              class="absolute inset-0 w-full h-full object-cover"
            />
            <div v-else class="absolute inset-0 flex items-center justify-center text-[#999] text-center">
              <div>
                <i class="fa-solid fa-image text-6xl mb-4"></i>
                <p>暂无照片</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧信息区域 -->
        <div class="flex-1 lg:min-w-[380px] p-8">
          <h1 class="text-[28px] text-[#FF8C00] mb-1.5 font-bold">{{ currentPet.animal_name }}</h1>
          <p class="text-[#666666] mb-5">{{ currentPet.species }} · {{ currentPet.breed }}</p>

          <!-- 信息网格 -->
          <div class="grid grid-cols-2 gap-4 mb-6">
            <div class="py-2.5 border-b border-[#eee]">
              <div class="font-semibold text-[#666666] mb-1 text-sm">性别</div>
              <div class="text-[#333333]">{{ currentPet.gender }}</div>
            </div>
            <div class="py-2.5 border-b border-[#eee]">
              <div class="font-semibold text-[#666666] mb-1 text-sm">年龄</div>
              <div class="text-[#333333]">{{ currentPet.animal_age }}个月</div>
            </div>
            <div class="py-2.5 border-b border-[#eee]">
              <div class="font-semibold text-[#666666] mb-1 text-sm">绝育状况</div>
              <div class="text-[#333333]">{{ currentPet.is_sterilized }}</div>
            </div>
            <div class="py-2.5 border-b border-[#eee]">
              <div class="font-semibold text-[#666666] mb-1 text-sm">目前位置</div>
              <div class="text-[#333333]">{{ currentPet.animal_location }}</div>
            </div>
          </div>

          <!-- 领养状态卡片 -->
          <div class="bg-[#fffaf0] p-4 rounded-xl my-5">
            <div class="flex items-center gap-3 mb-3">
              <p class="text-sm mb-0">当前领养状态：</p>
              <span class="inline-block px-2.5 py-1 rounded-full text-[13px] font-semibold bg-[#fff3cd] text-[#856404]">
                {{ currentPet.adoption_status }}
              </span>
            </div>
            <div class="flex items-center gap-3 mt-3">
              <div class="w-[46px] h-[46px] rounded-full bg-[#F3C697] flex items-center justify-center font-semibold text-white overflow-hidden">
                <img
                  v-if="currentPet.publisher.avatar_url"
                  :src="currentPet.publisher.avatar_url"
                  alt="用户头像"
                  class="w-full h-full object-cover rounded-full"
                  @error="handleAvatarError"
                />
                <span v-else class="w-full h-full flex items-center justify-center">
                  {{ currentPet.publisher.username.charAt(0) }}
                </span>
              </div>
              <div class="flex-1">
                <div class="font-semibold text-[#333333]">{{ currentPet.publisher.username }}</div>
              </div>
              <button
                type="button"
                class="inline-block px-5 py-2.5 bg-[#4A90E2] text-white rounded-xl font-semibold transition-all hover:bg-[#3a7bc8] hover:-translate-y-0.5 hover:shadow-lg"
                @click="goToFostererProfile"
              >
                查看主页
              </button>
              <button
                type="button"
                class="inline-block px-5 py-2.5 bg-[#4A90E2] text-white rounded-xl font-semibold transition-all hover:bg-[#3a7bc8] hover:-translate-y-0.5 hover:shadow-lg"
                @click="contactFosterer"
              >
                联系TA
              </button>
            </div>
          </div>

          <button
            v-if="!isSelfPublisher && currentPet.adoption_status !== '长期领养'"
            type="button"
            class="inline-block px-6 py-2.5 bg-[#FF8C00] text-white rounded-xl font-semibold transition-all hover:bg-[#e6722a] hover:-translate-y-0.5 hover:shadow-lg"
            @click="requestAdoption"
          >
            请求领养
          </button>
        </div>
      </div>

      <!-- 详细描述卡片 -->
      <div class="bg-white rounded-xl shadow-sm p-8">
        <h2 class="text-xl font-bold mb-4 text-[#333333]">关于{{ currentPet.animal_name }}</h2>
        <div class="text-[#333333] leading-relaxed space-y-4">
          <p>{{ currentPet.detail_info }}</p>
        </div>

        <h2 class="text-xl font-bold mb-4 mt-8 text-[#333333]">生活照片</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div
            v-for="(url, index) in currentPet.photo_urls"
            :key="index"
            class="bg-[#f0f0f0] rounded-xl overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
            @click="openImageViewer(index)"
          >
            <div
              v-if="url"
              class="relative w-full aspect-[4/3] overflow-hidden"
            >
              <img
                :src="url"
                :alt="`照片${index + 1}`"
                class="absolute inset-0 w-full h-full object-cover"
              />
            </div>
            <div v-else class="relative w-full aspect-[4/3] flex items-center justify-center overflow-hidden">
              <span class="text-[#999] text-sm">照片{{ index + 1 }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <ErrorModal
      :visible="showSelfContactModal"
      title="无法联系自己"
      message="您正在查看的是自己发布的宠物，无法与自己发起沟通。"
      @close="showSelfContactModal = false"
    />
    <ErrorModal
      :visible="showSelfAdoptModal"
      title="无法向自己领养"
      message="您无法向自己发布的宠物发起领养申请。"
      @close="showSelfAdoptModal = false"
    />

    <!-- 图片查看器 -->
    <ImageViewer
      v-if="currentPet"
      :visible="showImageViewer"
      :images="currentPet.photo_urls"
      :initial-index="imageViewerIndex"
      @close="closeImageViewer"
    />

    <!-- 未找到宠物 -->
    <main v-else class="max-w-[1200px] mx-auto py-16 px-5">
      <div class="bg-white rounded-xl shadow-sm p-10 text-center">
        <p class="text-xl font-semibold mb-4">未找到该宠物信息</p>
        <button
          type="button"
          class="mt-2 inline-flex items-center justify-center px-6 py-3 rounded-lg bg-[#FF8C00] hover:bg-[#e6722a] text-white font-medium"
          @click="() => $router.push('/pets')"
        >
          返回宠物列表
        </button>
      </div>
    </main>

    <!-- Footer -->
    <!-- <footer class="bg-[#1E293B] text-white py-10 px-[5%] mt-16">
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
    </footer> -->
  </div>
</template>
