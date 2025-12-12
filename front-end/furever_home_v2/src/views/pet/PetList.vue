<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

defineOptions({
  name: 'PetList'
})

import { useRouter } from 'vue-router'
import { getAnimalList, type AdoptionStatus, type Gender, type Species } from '@/api/animalApi'
import RegionCascader from '../../components/common/RegionCascader.vue'

interface Pet {
  id: number
  name: string
  photo_url: string
  fosterer: string
  location: string
  species: string
  age: number // 年龄（月）
  gender: string
  adoption_status: string // '短期领养' | '长期领养'
  breed: string
  health_status: string
  days_adopted: number // 领养天数
}

const router = useRouter()

const pets = ref<Pet[]>([])

const total = ref(0)

// 筛选条件
const provinceFilter = ref('')
const cityFilter = ref('')
const speciesFilter = ref('')
const genderFilter = ref('')
const ageFilter = ref('')
const adoptionStatusFilter = ref('')

// 分页
const currentPage = ref(1)
const pageSize = 6

watch(
  () => provinceFilter.value,
  () => {
    cityFilter.value = ''
  }
)

watch(
  [provinceFilter, cityFilter, speciesFilter, genderFilter, ageFilter, adoptionStatusFilter],
  () => {
    currentPage.value = 1
    loadPets()
  }
)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const paginatedPets = computed(() => {
  return pets.value
})

const loadPets = async () => {
  try {
    // 构建请求参数，未选择的条件不传递（undefined）
    const params: {
      page?: number
      pageSize?: number
      province?: string
      city?: string
      species?: Species
      gender?: Gender
      age?: string
      adoptionStatus?: AdoptionStatus
    } = {
      page: currentPage.value,
      pageSize,
    }
    
    // 只传递有值的筛选条件
    if (provinceFilter.value) {
      params.province = provinceFilter.value
    }
    if (cityFilter.value) {
      params.city = cityFilter.value
    }
    if (speciesFilter.value) {
      params.species = speciesFilter.value as Species
    }
    if (genderFilter.value) {
      params.gender = genderFilter.value as Gender
    }
    if (ageFilter.value) {
      // 将 "84+" 转换为后端可接受的格式 "84-999"（表示 84 个月及以上）
      if (ageFilter.value === '84+') {
        params.age = '84-999'
      } else {
        params.age = ageFilter.value
      }
    }
    if (adoptionStatusFilter.value) {
      params.adoptionStatus = adoptionStatusFilter.value as AdoptionStatus
    }
    
    const res = await getAnimalList(params)
    if (res.code === 200 && res.data) {
      total.value = res.data.total ?? 0
      const records = res.data.records ?? []
      pets.value = records.map((item: any) => {
        // 处理 photoUrls 既可能是数组，也可能是 JSON 字符串的情况
        let photos: string[] = []
        if (Array.isArray(item.photoUrls)) {
          photos = item.photoUrls
        } else if (typeof item.photoUrls === 'string' && item.photoUrls.trim()) {
          try {
            const parsed = JSON.parse(item.photoUrls)
            if (Array.isArray(parsed)) {
              photos = parsed
            }
          } catch (e) {
            // ignore parse error
          }
        }
        const photoUrl: string =
          (item.animalPhoto as string | undefined) ||
          (photos.length > 0 ? (photos[0] as string) : '') ||
          ''

        return {
          id: item.animalId,
          name: item.animalName as string,
          // 兼容后端两种字段：photoUrls(string[]) / animalPhoto(string)
          photo_url: photoUrl,
          fosterer: (item.adopterName as string) || '',
          location: (item.location as string) || '',
          species: (item.species as string) || '',
          age: (item.animalAge as number) ?? 0,
          gender: (item.gender as string) || '',
          adoption_status: (item.adoptionStatus as string) || '',
          breed: (item.breed as string) || '',
          health_status: (item.healthStatus as string) || '',
          days_adopted: (item.adoptionDays as number) ?? 0,
        }
      })
    } else {
      console.error('获取动物列表失败', res)
    }
  } catch (err) {
    console.error('获取动物列表接口异常', err)
  }
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    window.scrollTo({ top: 0, behavior: 'smooth' })
    loadPets()
  }
}

const goToPetDetail = (id: number) => {
  router.push({ name: 'PetDetail', params: { id } })
}

const getDisplayedPages = (current: number, total: number): (number | string)[] => {
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1)

  if (current <= 3) {
    return [1, 2, 3, 4, '...', total]
  } else if (current >= total - 2) {
    return [1, '...', total - 3, total - 2, total - 1, total]
  } else {
    return [1, '...', current - 1, current, current + 1, '...', total]
  }
}

onMounted(() => {
  loadPets()
})
</script>

<template>
  <div class="min-h-screen bg-[#f5f5f5]">
    <!-- Hero Section -->
    <section class="text-center pt-24 pb-10">
      <h1 class="text-[28px] text-[#FF8C42] mb-2.5 font-bold">
        为这些可爱的小生命寻找一个永久的家
      </h1>
      <p class="text-base text-[#666]">领养代替购买，给流浪动物一个温暖的家</p>
    </section>

    <!-- Main Container -->
    <div class="max-w-[1200px] mx-auto px-[5%] pb-8">
      <!-- Filter Bar -->
      <div class="bg-white rounded-2xl p-5 mb-8 shadow-[0_8px_24px_rgba(0,0,0,0.06)]">
        <div class="flex flex-wrap items-center gap-3">
          <span class="text-sm text-[#666] whitespace-nowrap">筛选条件：</span>

          <!-- 地区：级联选择（省 + 市） -->
          <span class="text-sm text-[#666] whitespace-nowrap">地区</span>
          <RegionCascader
            v-model:province="provinceFilter"
            v-model:city="cityFilter"
          />

          <!-- 种类 -->
          <span class="text-sm text-[#666] whitespace-nowrap">种类</span>
          <select
            v-model="speciesFilter"
            class="px-3 py-1.5 border border-[#ddd] rounded-full text-sm bg-white cursor-pointer transition-colors hover:border-[#FF8C42] focus:outline-none focus:border-[#FF8C42]"
          >
            <option value="">请选择</option>
            <option>猫</option>
            <option>狗</option>
            <option>仓鼠</option>
            <option>兔子</option>
            <option>鱼类</option>
            <option>鸟类</option>
            <option>龟类</option>
            <option>其他</option>
          </select>

          <!-- 性别 -->
          <span class="text-sm text-[#666] whitespace-nowrap">性别</span>
          <select
            v-model="genderFilter"
            class="px-3 py-1.5 border border-[#ddd] rounded-full text-sm bg-white cursor-pointer transition-colors hover:border-[#FF8C42] focus:outline-none focus:border-[#FF8C42]"
          >
            <option value="">请选择</option>
            <option>公</option>
            <option>母</option>
            <option>未知</option>
          </select>

          <!-- 年龄 -->
          <span class="text-sm text-[#666] whitespace-nowrap">年龄</span>
          <select
            v-model="ageFilter"
            class="px-3 py-1.5 border border-[#ddd] rounded-full text-sm bg-white cursor-pointer transition-colors hover:border-[#FF8C42] focus:outline-none focus:border-[#FF8C42]"
          >
            <option value="">请选择</option>
            <option value="0-6">0-6个月</option>
            <option value="6-12">6-12个月</option>
            <option value="12-36">1-3岁</option>
            <option value="36-84">3-7岁</option>
            <option value="84+">7岁以上</option>
          </select>

          <!-- 领养状态 -->
          <span class="text-sm text-[#666] whitespace-nowrap">领养状态</span>
          <select
            v-model="adoptionStatusFilter"
            class="px-3 py-1.5 border border-[#ddd] rounded-full text-sm bg-white cursor-pointer transition-colors hover:border-[#FF8C42] focus:outline-none focus:border-[#FF8C42]"
          >
            <option value="">请选择</option>
            <option>短期领养</option>
            <option>长期领养</option>
          </select>
        </div>
      </div>

      <!-- Pets Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
        <div
          v-for="pet in paginatedPets"
          :key="pet.id"
          class="bg-white rounded-2xl overflow-hidden shadow-[0_8px_24px_rgba(0,0,0,0.06)] cursor-pointer transition-transform hover:-translate-y-1"
          @click="goToPetDetail(pet.id)"
        >
          <div class="relative w-full aspect-[4/3] bg-[#FFE4B5] flex items-center justify-center overflow-hidden">
            <img
              v-if="pet.photo_url"
              :src="pet.photo_url"
              :alt="pet.name"
              class="absolute inset-0 w-full h-full object-cover cursor-pointer"
            />
            <span v-else class="text-[#999] font-bold">{{ pet.name }}的照片</span>
          </div>
          <div class="p-5">
            <div class="text-lg font-bold mb-1.5 text-[#333]">{{ pet.name }}</div>
            <div class="text-sm text-[#666] mb-2.5">
              {{ pet.species }} · {{ Math.floor(pet.age / 12) }}岁{{ pet.age % 12 }}个月 · {{ pet.health_status }}
            </div>
            <div class="text-xs text-[#666] mt-1.5">
              {{ pet.adoption_status === '长期领养' ? '长期领养人：' : '临时收养者：' }}{{ pet.fosterer }}
            </div>
            <span
              :class="[
                'inline-block px-3 py-1.5 rounded-2xl text-xs font-bold',
                pet.adoption_status === '短期领养'
                  ? 'bg-[#FFF3CD] text-[#856404]'
                  : 'bg-[#D1FAE5] text-[#059669]'
              ]"
            >
              {{ pet.adoption_status }}
            </span>
            <!-- <div class="text-xs text-[#666] mt-1.5">
              {{ pet.adoption_status === '长期领养' ? '长期领养人：' : '临时收养者：' }}{{ pet.fosterer }}
            </div> -->
            <div class="bg-[#FFF9F0] p-2 rounded text-center text-sm mt-2.5">
              已{{ pet.adoption_status }} {{ pet.days_adopted }} 天
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="flex justify-center items-center gap-2 mt-10">
        <button
          :disabled="currentPage === 1"
          class="px-4 py-2 border border-[#ddd] bg-white rounded-lg cursor-pointer transition-all hover:border-[#FF8C42] hover:text-[#FF8C42] disabled:text-[#ccc] disabled:cursor-not-allowed disabled:hover:border-[#ddd] disabled:hover:text-[#ccc]"
          @click="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        <button
          v-for="(page, index) in getDisplayedPages(currentPage, totalPages)"
          :key="index"
          :class="[
            'px-4 py-2 border rounded-lg transition-all',
            page === currentPage
              ? 'bg-[#FF8C42] text-white border-[#FF8C42]'
              : 'border-[#ddd] bg-white text-[#666]',
            typeof page === 'string'
              ? 'cursor-default border-transparent'
              : 'cursor-pointer hover:border-[#FF8C42] hover:text-[#FF8C42]'
          ]"
          @click="typeof page === 'number' && goToPage(page)"
        >
          {{ page }}
        </button>
        <button
          :disabled="currentPage === totalPages"
          class="px-4 py-2 border border-[#ddd] bg-white rounded-lg cursor-pointer transition-all hover:border-[#FF8C42] hover:text-[#FF8C42] disabled:text-[#ccc] disabled:cursor-not-allowed disabled:hover:border-[#ddd] disabled:hover:text-[#ccc]"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- Footer -->
    <!-- <footer class="bg-[#2C3E50] text-white py-10 px-[5%] mt-16">
      <div class="flex flex-wrap justify-between max-w-[1200px] mx-auto gap-8">
        <div class="flex-1 min-w-[220px]">
          <h3 class="mb-4 text-lg font-semibold">数据统计</h3>
          <p class="mb-2 text-sm text-[#ccc]">我们与多家救助站建立了长期合作关系</p>
          <p class="mb-2 text-sm text-[#ccc]">已为 <strong>1,286</strong> 只小动物找到新家</p>
          <a href="#" class="text-[#FF8C42] text-sm hover:underline">查看合作救助站 →</a>
        </div>
        <div class="flex-1 min-w-[220px]">
          <h3 class="mb-4 text-lg font-semibold">联系开发团队</h3>
          <p class="mb-2 text-sm text-[#ccc]">
            如果您有任何问题、建议或合作意向，请随时联系我们的开发团队。
          </p>
          <div class="flex gap-3 mt-2.5">
            <i class="fa-brands fa-weixin text-2xl hover:text-[#FF8C42] cursor-pointer transition-colors"></i>
            <i class="fa-brands fa-qq text-2xl hover:text-[#FF8C42] cursor-pointer transition-colors"></i>
            <i class="fa-brands fa-weibo text-2xl hover:text-[#FF8C42] cursor-pointer transition-colors"></i>
          </div>
        </div>
      </div>
      <div class="text-center mt-8 pt-5 border-t border-white/10 text-sm text-[#aaa]">
        2025 FUREVERHOME动物领养平台 - 让每个生命都有温暖的家
      </div>
    </footer> -->

  </div>
</template>
