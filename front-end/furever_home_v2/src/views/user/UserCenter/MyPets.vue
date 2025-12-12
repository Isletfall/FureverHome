<template>
  <div>
    <div class="mb-6">
      <h2 class="text-2xl font-bold mb-2" style="color: #111;">我的宠物</h2>
      <p class="text-sm" style="color: #666666;">查看并管理您发布的短期与长期宠物。</p>
    </div>

    <div class="flex items-center gap-3 mb-6">
      <button
        class="px-6 py-2.5 rounded-full text-smfont-medium border transition-all"
        :class="activeTab === 'short'
          ? 'text-white border-transparent'
          : 'text-gray-700 border-gray-300 bg-white hover:border-[#FF8C00] hover:text-[#FF8C00]'"
        :style="activeTab === 'short' ? 'background-color: #FF8C00;' : ''"
        @click="switchTab('short')"
      >
        短期宠物
      </button>
      <button
        class="px-6 py-2.5 rounded-full text-sm font-medium border transition-all"
        :class="activeTab === 'long'
          ? 'text-white border-transparent'
          : 'text-gray-700 border-gray-300 bg-white hover:border-[#FF8C00] hover:text-[#FF8C00]'"
        :style="activeTab === 'long' ? 'background-color: #FF8C00;' : ''"
        @click="switchTab('long')"
      >
        长期宠物
      </button>
    </div>

    <div v-if="currentPets.length" class="grid grid-cols-3 gap-6">
      <div
        v-for="pet in currentPets"
        :key="pet.id"
        class="bg-white rounded-xl shadow-sm overflow-hidden flex flex-col border cursor-pointer"
        style="border-color: #F3F4F6;"
        @click="viewDetail(pet)"
      >
        <div class="relative w-full aspect-[4/3] bg-[#FFEFD5] overflow-hidden">
          <img
            v-if="pet.cover"
            :src="pet.cover"
            :alt="pet.name"
            class="absolute inset-0 w-full h-full object-cover"
          />
          <div
            v-else
            class="absolute inset-0 flex items-center justify-center text-sm"
            style="color: #C97A30;"
          >
            {{ pet.photoText }}
          </div>
        </div>

        <div class="px-5 py-4 flex flex-col gap-3">
          <div>
            <div class="text-base font-bold hover:underline" style="color: #FF8C00;">
              {{ pet.name }}
            </div>
            <div class="text-xs mt-1" style="color: #666666;">
              {{ pet.meta }}
            </div>
          </div>

          <div class="flex items-center justify-between">
            <button
              class="px-4 py-1.5 rounded-full text-xs font-medium border border-transparent cursor-pointer transition-all"
              :style="pet.type === 'short'
                ? 'background-color: #FFEDD5; color: #C05621;'
                : 'background-color: #FEF9C3; color: #854D0E;'"
              @click.stop
            >
              {{ pet.type === 'short' ? '短期领养' : '长期领养' }}
            </button>
            <div class="flex items-center gap-3 text-lg" style="color: #9CA3AF;">
              <button
                class="w-8 h-8 rounded-full flex items-center justify-center border border-transparent bg-white cursor-pointer hover:border-[#FF8C00] hover:text-[#FF8C00] transition-all"
                @click.stop="handleEdit(pet)"
              >
                <i class="fa-regular fa-pen-to-square text-sm"></i>
              </button>
              <button
                class="w-8 h-8 rounded-full flex items-center justify-center border border-transparent bg-white cursor-pointer hover:border-[#EF4444] hover:text-[#EF4444] transition-all"
                @click.stop="handleDelete(pet)"
              >
                <i class="fa-regular fa-trash-can text-sm"></i>
              </button>
            </div>
          </div>

          <div
            class="text-xs pt-3 border-t flex items-center justify-between"
            style="color: #9CA3AF; border-color: #F3F4F6;"
          >
            <span>
              {{ pet.footerText }}
            </span>
            <span
              v-if="pet.reviewStatus"
              class="px-2 py-0.5 rounded-full font-medium"
              :class="getReviewStatusClass(pet.reviewStatus)"
            >
              审核状态：{{ pet.reviewStatus }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="mt-8 text-center text-sm" style="color: #9CA3AF;">
      {{ activeTab === 'short' ? '当前没有发布的短期宠物' : '当前没有发布的长期宠物' }}
    </div>

    <!-- 统一分页样式，列表为空时也展示，至少一页 -->
    <div class="flex justify-center mt-10 mb-4">
      <div class="flex items-center gap-2.5">
        <button
          class="w-9 h-9 rounded-lg border bg-white text-xs flex items-center justify-center cursor-pointer transition-all"
          style="border-color: #E5E7EB; color: #6B7280;"
          :disabled="currentPage === 1"
          :class="currentPage === 1 ? 'cursor-not-allowed opacity-50' : 'hover:border-[#FF8C00] hover:text-[#FF8C00]'"
          @click="goPrev"
        >
          <i class="fa-solid fa-chevron-left"></i>
        </button>

        <button
          v-for="(page, index) in getDisplayedPages(currentPage, totalPages)"
          :key="index"
          class="w-9 h-9 rounded-lg border text-xs flex items-center justify-center transition-all"
          :class="[
            page === currentPage ? 'bg-[#FF8C00] text-white border-[#FF8C00]' : 'bg-white border-gray-200 text-gray-600',
            typeof page === 'string' ? 'cursor-default border-transparent' : 'cursor-pointer hover:border-[#FF8C00] hover:text-[#FF8C00]'
          ]"
          @click="typeof page === 'number' && goPage(page)"
        >
          {{ page }}
        </button>

        <button
          class="w-9 h-9 rounded-lg border bg-white text-xs flex items-center justify-center cursor-pointer transition-all"
          style="border-color: #E5E7EB; color: #6B7280;"
          :disabled="currentPage === totalPages"
          :class="currentPage === totalPages ? 'cursor-not-allowed opacity-50' : 'hover:border-[#FF8C00] hover:text-[#FF8C00]'"
          @click="goNext"
        >
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div
      v-if="showEditModal"
      class="fixed inset-0 bg-black/50 z-[3000] flex items-start justify-center overflow-y-auto px-4 py-10"
    >
      <div class="bg-white rounded-2xl w-full max-w-3xl shadow-2xl p-8">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-2xl font-bold text-[#333333]">编辑宠物信息</h3>
            <p class="text-sm text-[#9CA3AF]">根据发布宠物页的字段样式进行修改</p>
          </div>
          <button class="text-[#9CA3AF] hover:text-[#FF8C00]" @click="cancelEdit">
            <i class="fa-solid fa-times text-xl"></i>
          </button>
        </div>

        <div class="space-y-8">
          <section class="pb-5 border-b border-[#E5E7EB]">
            <h4 class="text-lg font-semibold mb-4 flex items-center gap-2 text-[#333333]">
              <i class="fa-solid fa-paw text-[#FF8C00]"></i>
              宠物基本信息
            </h4>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div v-for="field in basicFields" :key="field.key">
                <label class="block mb-2 text-sm font-semibold text-[#666666]">
                  {{ field.label }}
                  <span v-if="field.required" class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-if="field.type === 'text'"
                  v-model="editForm[field.key]"
                  :placeholder="field.placeholder"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm focus:border-[#FF8C00] focus:outline-none"
                />
                <select
                  v-else
                  v-model="editForm[field.key]"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm focus:border-[#FF8C00] focus:outline-none"
                >
                  <option value="">请选择</option>
                  <option v-for="option in field.options" :key="option" :value="option">
                    {{ option }}
                  </option>
                </select>
              </div>
            </div>
          </section>

          <!-- 目前位置 -->
          <section class="pb-5 border-b border-[#E5E7EB]">
            <h4 class="text-lg font-semibold mb-4 flex items-center gap-2 text-[#333333]">
              <i class="fa-solid fa-location-dot text-[#FF8C00]"></i>
              目前位置
            </h4>
            <div class="space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                  <label class="block mb-2 text-sm font-semibold text-[#666666]">
                    所在省份 <span class="text-[#EF4444]">*</span>
                  </label>
                  <RegionCascader
                    v-model:province="editForm.province"
                    v-model:city="editForm.city"
                  />
                </div>
              </div>
              <div>
                <label class="block mb-2 text-sm font-semibold text-[#666666]">
                  详细地址 <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="editForm.location"
                  type="text"
                  placeholder="详细地址，如街道、小区、楼栋、门牌号等"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm focus:border-[#FF8C00] focus:outline-none"
                />
              </div>
            </div>
          </section>

          <section class="pb-5 border-b border-[#E5E7EB]">
            <h4 class="text-lg font-semibold mb-4 flex items-center gap-2 text-[#333333]">
              <i class="fa-solid fa-book text-[#FF8C00]"></i>
              详细描述
            </h4>
            <textarea
              v-model="editForm.story"
              placeholder="请详细描述救助背景、健康状态及注意事项"
              class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm min-h-[120px] resize-y focus:border-[#FF8C00] focus:outline-none"
            ></textarea>
          </section>

          <!-- 宠物照片管理 -->
          <section class="pb-5 border-b border-[#E5E7EB]">
            <h4 class="text-lg font-semibold mb-4 flex items-center gap-2 text-[#333333]">
              <i class="fa-solid fa-image text-[#FF8C00]"></i>
              宠物照片
            </h4>
            <div
              class="border-2 border-dashed border-[#E5E7EB] rounded-xl p-6 text-center cursor-pointer transition-all hover:border-[#FF8C00] hover:bg-[rgba(255,140,0,0.03)]"
              @click="triggerPhotoUpload"
            >
              <input
                ref="photoInputRef"
                type="file"
                accept=".jpg,.jpeg,.png,.webp"
                class="hidden"
                @change="onPhotoSelected"
              />
              <div class="text-3xl mb-2.5 text-[#666666]">
                <i class="fa-solid fa-cloud-arrow-up"></i>
              </div>
              <div class="text-sm text-[#666666] mb-1.5">
                点击上传新的宠物照片
              </div>
              <div class="text-xs text-[#9CA3AF]">
                支持 JPG、JPEG、PNG、WEBP 格式
              </div>
            </div>
            <div v-if="editPhotos.length" class="mt-4 flex flex-wrap gap-3">
              <div
                v-for="url in editPhotos"
                :key="url"
                class="relative w-24 h-24 rounded-lg overflow-hidden border border-[#E5E7EB] cursor-pointer"
                @click="openImagePreview(url)"
              >
                <img :src="url" alt="宠物照片" class="w-full h-full object-cover" />
                <button
                  type="button"
                  class="absolute top-1 right-1 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center text-xs hover:bg-red-600 z-10"
                  @click.stop="removePhoto(url)"
                >
                  ×
                </button>
              </div>
            </div>
          </section>

          <section class="pb-5 border-b border-[#E5E7EB]">
            <h4 class="text-lg font-semibold mb-4 flex items-center gap-2 text-[#333333]">
              <i class="fa-solid fa-address-book text-[#FF8C00]"></i>
              联系信息
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block mb-2 text-sm font-semibold text-[#666666]">
                  联系电话 <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="editForm.phone"
                  type="tel"
                  placeholder="13812345678"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm focus:border-[#FF8C00] focus:outline-none"
                />
              </div>
              <div>
                <label class="block mb-2 text-sm font-semibold text-[#666666]">
                  电子邮箱 <span class="text-[#EF4444]">*</span>
                </label>
                <input
                  v-model="editForm.email"
                  type="email"
                  placeholder="user@example.com"
                  class="w-full px-4 py-3 border border-[#E5E7EB] rounded-lg text-sm focus:border-[#FF8C00] focus:outline-none"
                />
              </div>
            </div>
          </section>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button
            class="px-6 py-2.5 rounded-lg border border-gray-300 text-sm font-semibold text-[#666666] hover:bg-gray-50"
            @click="cancelEdit"
          >
            取消
          </button>
          <button
            class="px-6 py-2.5 rounded-lg text-sm font-semibold text-white"
            style="background-color: #FF8C00;"
            @click="submitEdit"
          >
            确认保存
          </button>
        </div>
      </div>
    </div>

    <ConfirmModal
      :visible="showDeleteConfirm"
      title="确认删除"
      message="确定要删除这条宠物信息吗？删除后将无法恢复。"
      @confirm="confirmDelete"
      @cancel="showDeleteConfirm = false"
    />

    <SuccessModal
      :visible="successModal.visible"
      :title="successModal.title"
      :message="successModal.message"
      @close="closeSuccessModal"
    />

    <!-- 编辑时图片大图预览 -->
    <div
      v-if="showImagePreview && previewImageUrl"
      class="fixed inset-0 bg-black/60 z-[3100] flex items-center justify-center"
      @click.self="closeImagePreview"
    >
      <div class="relative max-w-[90vw] max-h-[90vh] bg-white rounded-lg overflow-hidden shadow-xl">
        <button
          type="button"
          class="absolute top-2 right-2 w-8 h-8 rounded-full bg-black/70 text-white flex items-center justify-center text-lg cursor-pointer"
          @click="closeImagePreview"
        >
          ×
        </button>
        <img
          v-if="previewImageUrl"
          :src="previewImageUrl"
          alt="宠物照片预览"
          class="max-w-[90vw] max-h-[90vh] object-contain block"
        />
      </div>
    </div>

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

<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import ConfirmModal from '../../../components/common/ConfirmModal.vue';
import SuccessModal from '../../../components/common/SuccessModal.vue';
import RegionCascader from '../../../components/common/RegionCascader.vue';
import ImageCropper from '../../../components/common/ImageCropper.vue';
import { getMyShortAnimals, getMyLongAnimals, deleteAnimal, updateAnimal, ReviewStatus } from '@/api/animalApi';
import { uploadImage, deleteImage } from '@/api/storageApi';

interface PetCard {
  id: number;
  name: string;
  meta: string;
  type: 'short' | 'long';
  days: number;
  cover: string;
  story: string;
  phone: string;
  email: string;
  gender: string;
  age: string;
  species: string;
  breed: string;
  sterilized: string;
  // 省市 + 详细地址
  province?: string;
  city?: string;
  location: string;
  // 审核状态：仅发布者自己在“我的宠物”中可见
  reviewStatus?: ReviewStatus;
  // 所有图片 URL 列表
  photos?: string[];
}

const router = useRouter();
const route = useRoute();

const shortPets = ref<PetCard[]>([]);
const longPets = ref<PetCard[]>([]);
const shortTotal = ref(0);
const longTotal = ref(0);

const activeTab = ref<'short' | 'long'>('short');
// 每页展示 3 条宠物卡片
const pageSize = 3;
const currentPage = ref(1);

// 月龄转换为“X岁Y个月”展示
function formatAge(ageInMonths: number | null | undefined): string {
  if (ageInMonths == null || isNaN(ageInMonths as number)) return '';
  const months = Math.max(0, Math.floor(ageInMonths as number));
  const years = Math.floor(months / 12);
  const restMonths = months % 12;
  if (years === 0) return `${restMonths}个月`;
  if (restMonths === 0) return `${years}岁`;
  return `${years}岁${restMonths}个月`;
}

const currentList = computed(() => (activeTab.value === 'short' ? shortPets.value : longPets.value));
const currentTotal = computed(() => (activeTab.value === 'short' ? shortTotal.value : longTotal.value));
// 根据 total 计算页数，至少 1 页；空列表也展示 1 页
const totalPages = computed(() => Math.max(1, Math.ceil((currentTotal.value || 0) / pageSize)));

const currentPets = computed(() => {
  const start = 0; // 列表来自接口当前页，无需再次 slice
  return currentList.value.slice(start, start + pageSize).map(pet => ({
    ...pet,
    photoText: `${pet.name}的照片`,
    footerText: pet.type === 'short' ? `已短期领养第 ${pet.days} 天` : `已长期领养第 ${pet.days} 天`
  }));
});

watch(activeTab, () => {
  currentPage.value = 1;
});

watch(currentPage, () => {
  if (activeTab.value === 'short') {
    loadMyShortPets();
  } else {
    loadMyLongPets();
  }
});

function switchTab(tab: 'short' | 'long') {
  activeTab.value = tab;
  if (tab === 'short' && shortPets.value.length === 0) {
    loadMyShortPets();
  }
  if (activeTab.value === 'long' && longPets.value.length === 0) {
    loadMyLongPets();
  }
}

function getDisplayedPages(current: number, total: number): (number | string)[] {
  if (total <= 5) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  if (current <= 3) {
    return [1, 2, 3, 4, '...', total];
  } else if (current >= total - 2) {
    return [1, '...', total - 3, total - 2, total - 1, total];
  } else {
    return [1, '...', current - 1, current, current + 1, '...', total];
  }
}

function getReviewStatusClass(status?: ReviewStatus) {
  switch (status) {
    case ReviewStatus.通过:
      return 'bg-green-50 text-green-600';
    case ReviewStatus.拒绝:
      return 'bg-red-50 text-red-500';
    case ReviewStatus.待审核:
    default:
      return 'bg-yellow-50 text-yellow-600';
  }
}

function goPrev() {
  if (currentPage.value > 1) currentPage.value -= 1;
}

function goNext() {
  if (currentPage.value < totalPages.value) currentPage.value += 1;
}

function goPage(page: number) {
  currentPage.value = page;
}

function viewDetail(pet: PetCard) {
  router.push({ name: 'PetDetail', params: { id: pet.id } });
}

async function loadMyShortPets() {
  try {
    const res = await getMyShortAnimals({ page: currentPage.value, pageSize });
    if (res.code === 200 && res.data) {
      shortTotal.value = res.data.total ?? res.data.records?.length ?? 0;
      const records = res.data.records ?? [];
      shortPets.value = records.map((item: any) => {
        const sterilizedLabel =
          item.isSterilized === '是' || item.isSterilized === '已绝育'
            ? '已绝育'
            : item.isSterilized === '否'
              ? '未绝育'
              : item.isSterilized || '未知';
        const ageLabel = item.animalAge != null ? formatAge(item.animalAge as number) : '';
        const species = item.species || '';
        // 处理 photoUrls 既可能是数组，也可能是 JSON 字符串的情况
        let photos: string[] = [];
        if (Array.isArray(item.photoUrls)) {
          photos = item.photoUrls;
        } else if (typeof item.photoUrls === 'string' && item.photoUrls.trim()) {
          try {
            const parsed = JSON.parse(item.photoUrls);
            if (Array.isArray(parsed)) {
              photos = parsed;
            }
          } catch (e) {
            // ignore parse error
          }
        }
        return {
          id: item.animalId ?? 0,
          name: item.animalName || '',
          meta: `${species || '未知'} · ${ageLabel || '未知'} · ${sterilizedLabel}`,
          type: 'short',
          // adoptionDays: 后端返回的已领养/发布天数
          days: item.adoptionDays ?? 0,
          // 兼容后端两种字段：photoUrls(string[]) / animalPhoto(string)
          cover:
            (item.animalPhoto as string | undefined) ||
            (photos.length > 0 ? photos[0] : ''),
          story: item.shortDescription || '',
          phone: '',
          email: '',
          gender: item.gender || '',
          age: ageLabel,
          species,
          breed: item.breed || '',
          sterilized: sterilizedLabel,
          province: item.province || '',
          city: item.city || '',
          location: item.currentLocation || '',
          reviewStatus: item.reviewStatus as ReviewStatus | undefined,
          photos,
        } as PetCard;
      });
    } else {
      console.error('获取我的短期宠物列表失败', res);
    }
  } catch (err) {
    console.error('获取我的短期宠物列表接口异常', err);
  }
}

async function loadMyLongPets() {
  try {
    const res = await getMyLongAnimals({ page: currentPage.value, pageSize });
    if (res.code === 200 && res.data) {
      longTotal.value = res.data.total ?? res.data.records?.length ?? 0;
      const records = res.data.records ?? [];
      longPets.value = records.map((item: any) => {
        const sterilizedLabel =
          item.isSterilized === '是' || item.isSterilized === '已绝育'
            ? '已绝育'
            : item.isSterilized === '否'
              ? '未绝育'
              : item.isSterilized || '未知';
        const ageLabel = item.animalAge != null ? formatAge(item.animalAge as number) : '';
        const species = item.species || '';
        let photos: string[] = [];
        if (Array.isArray(item.photoUrls)) {
          photos = item.photoUrls;
        } else if (typeof item.photoUrls === 'string' && item.photoUrls.trim()) {
          try {
            const parsed = JSON.parse(item.photoUrls);
            if (Array.isArray(parsed)) {
              photos = parsed;
            }
          } catch (e) {
            // ignore parse error
          }
        }
        return {
          id: item.animalId ?? 0,
          name: item.animalName || '',
          meta: `${species || '未知'} · ${ageLabel || '未知'} · ${sterilizedLabel}`,
          type: 'long',
          // adoptionDays: 后端返回的已领养/发布天数
          days: item.adoptionDays ?? 0,
          cover:
            (item.animalPhoto as string | undefined) ||
            (photos.length > 0 ? photos[0] : ''),
          story: item.shortDescription || '',
          phone: '',
          email: '',
          gender: item.gender || '',
          age: ageLabel,
          species,
          breed: item.breed || '',
          sterilized: sterilizedLabel,
          province: item.province || '',
          city: item.city || '',
          location: item.currentLocation || '',
          reviewStatus: item.reviewStatus as ReviewStatus | undefined,
          photos,
        } as PetCard;
      });
    } else {
      console.error('获取我的长期宠物列表失败', res);
    }
  } catch (err) {
    console.error('获取我的长期宠物列表接口异常', err);
  }
}

const showEditModal = ref(false);
const showDeleteConfirm = ref(false);
const petToDelete = ref<PetCard | null>(null);
const editingPet = ref<PetCard | null>(null);

const editForm = reactive<Record<string, string>>({
  name: '',
  age: '',
  gender: '',
  species: '',
  breed: '',
  sterilized: '',
  // 省市 + 详细地址
  province: '',
  city: '',
  location: '',
  story: '',
  phone: '',
  email: ''
});

// 编辑态下的图片列表
const editPhotos = ref<string[]>([]);
const isUploading = ref(false);
const photoInputRef = ref<HTMLInputElement | null>(null);
const showImagePreview = ref(false);
const previewImageUrl = ref<string | null>(null);

const basicFields = [
  { key: 'name', label: '动物名称', type: 'text', required: true, placeholder: '例如：豆豆' },
  { key: 'age', label: '年龄', type: 'text', required: true, placeholder: '例如：6个月/2岁' },
  { key: 'gender', label: '性别', type: 'select', required: true, options: ['公', '母'] },
  { key: 'species', label: '动物种类', type: 'select', required: true, options: ['狗', '猫', '兔子', '鸟类', '其他'] },
  { key: 'breed', label: '动物品种', type: 'text', required: true, placeholder: '例如：布偶猫' },
  { key: 'sterilized', label: '是否绝育', type: 'select', required: true, options: ['是', '否', '未知'] }
];

function populateEditForm(pet: PetCard) {
  editForm.name = pet.name;
  editForm.age = pet.age;
  editForm.gender = pet.gender;
  editForm.species = pet.species;
  editForm.breed = pet.breed;
  // 将显示用的 "已绝育"/"未绝育" 映射回后端枚举值 "是"/"否"
  if (pet.sterilized === '已绝育') {
    editForm.sterilized = '是';
  } else if (pet.sterilized === '未绝育') {
    editForm.sterilized = '否';
  } else {
    editForm.sterilized = pet.sterilized || '未知';
  }
  editForm.province = pet.province || '';
  editForm.city = pet.city || '';
  editForm.location = pet.location || '';
  editForm.story = pet.story;
  editForm.phone = pet.phone;
  editForm.email = pet.email;
  editPhotos.value = pet.photos ? [...pet.photos] : pet.cover ? [pet.cover] : [];
}

function handleEdit(pet: PetCard) {
  if (!canModifyLongPet(pet)) {
    successModal.title = '无法操作';
    successModal.message = '长期领养须满 6 个月后才能修改信息。';
    successModal.visible = true;
    return;
  }
  editingPet.value = pet;
  populateEditForm(pet);
  showEditModal.value = true;
}

const successModal = reactive({
  visible: false,
  title: '提示',
  message: ''
});

// 长期领养修改/删除的权限判断：领养天数需 > 180 天（约 6 个月）
function canModifyLongPet(pet: PetCard) {
  if (pet.type !== 'long') return true;
  const days = pet.days ?? 0;
  return days > 180;
}

function cancelEdit() {
  showEditModal.value = false;
  successModal.title = '编辑取消';
  successModal.message = '已取消本次编辑操作。';
  successModal.visible = true;
}

function triggerPhotoUpload() {
  photoInputRef.value?.click();
}

// 图片裁剪相关
const showCropper = ref(false);
const currentCropFile = ref<File | null>(null);

async function onPhotoSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    successModal.title = '错误';
    successModal.message = '请选择图片文件';
    successModal.visible = true;
    if (photoInputRef.value) photoInputRef.value.value = '';
    return;
  }

  // 打开裁剪弹窗
  currentCropFile.value = file;
  showCropper.value = true;
  if (photoInputRef.value) photoInputRef.value.value = '';
}

async function handleCropConfirm(croppedFile: File) {
  try {
    isUploading.value = true;
    const res = await uploadImage(croppedFile);
    if ((res.code === 0 || res.code === 200) && res.data) {
      editPhotos.value.push(res.data);
    } else {
      successModal.title = '上传失败';
      successModal.message = res.message || '宠物照片上传失败，请稍后重试。';
      successModal.visible = true;
    }
  } catch (error) {
    console.error('上传宠物照片失败', error);
    successModal.title = '上传失败';
    successModal.message = '宠物照片上传失败，请稍后重试。';
    successModal.visible = true;
  } finally {
    isUploading.value = false;
    currentCropFile.value = null;
  }
}

function handleCropCancel() {
  currentCropFile.value = null;
}

async function removePhoto(url: string) {
  // 从 URL 中提取对象名（最后一段路径），如果失败则直接使用原字符串
  const parts = url.split('/');
  const object = parts[parts.length - 1] || url;

  try {
    isUploading.value = true;
    const res = await deleteImage(object);
    if (res.code === 0 || res.code === 200) {
      editPhotos.value = editPhotos.value.filter(item => item !== url);
    } else {
      successModal.title = '删除失败';
      successModal.message = res.message || '删除宠物照片失败，请稍后重试。';
      successModal.visible = true;
    }
  } catch (error) {
    console.error('删除宠物照片失败', error);
    successModal.title = '删除失败';
    successModal.message = '删除宠物照片失败，请稍后重试。';
    successModal.visible = true;
  } finally {
    isUploading.value = false;
  }
}

function openImagePreview(url: string) {
  previewImageUrl.value = url;
  showImagePreview.value = true;
}

function closeImagePreview() {
  showImagePreview.value = false;
  previewImageUrl.value = null;
}

async function submitEdit() {
  if (!editingPet.value) return;

  try {
    const id = editingPet.value.id;
    // 确保 isSterilized 是后端接受的枚举值：'是'、'否'、'未知'
    let sterilizedValue: string = editForm.sterilized;
    if (sterilizedValue === '已绝育') {
      sterilizedValue = '是';
    } else if (sterilizedValue === '未绝育') {
      sterilizedValue = '否';
    } else if (!['是', '否', '未知'].includes(sterilizedValue)) {
      sterilizedValue = '未知';
    }
    
    const reqBody = {
      animalName: editForm.name,
      animalAge: editForm.age ? Number(editForm.age) : 0,
      gender: editForm.gender as any,
      species: editForm.species as any,
      breed: editForm.breed,
      isSterilized: sterilizedValue as any,
      province: editForm.province || '',
      city: editForm.city || '',
      currentLocation: editForm.location || '',
      shortDescription: editForm.story,
      contactPhone: editForm.phone || '',
      contactEmail: editForm.email || '',
      photoUrls: editPhotos.value.length ? editPhotos.value : undefined,
    };

    const res = await updateAnimal(id, reqBody);
    if (res.code !== 200) {
      console.error('更新宠物信息失败', res);
      successModal.title = '保存失败';
      successModal.message = res.message || '更新宠物信息失败，请稍后重试。';
      successModal.visible = true;
      return;
    }

    // 更新成功后刷新当前列表
    if (editingPet.value.type === 'short') {
      await loadMyShortPets();
    } else {
      await loadMyLongPets();
    }

    showEditModal.value = false;
    successModal.title = '提交成功';
    successModal.message = '您提交的宠物信息将送往管理员审核，审核通过后将展示在平台上。';
    successModal.visible = true;
  } catch (err) {
    console.error('更新宠物信息接口异常', err);
    successModal.title = '保存失败';
    successModal.message = '更新宠物信息接口异常，请稍后重试。';
    successModal.visible = true;
  }
}

function handleDelete(pet: PetCard) {
  if (!canModifyLongPet(pet)) {
    successModal.title = '无法操作';
    successModal.message = '长期领养须满 6 个月后才能删除。';
    successModal.visible = true;
    return;
  }
  petToDelete.value = pet;
  showDeleteConfirm.value = true;
}

async function confirmDelete() {
  if (!petToDelete.value) return;

  try {
    const id = petToDelete.value.id;
    const res = await deleteAnimal(id);
    if (res.code !== 200) {
      console.error('删除宠物失败', res);
      successModal.title = '删除失败';
      successModal.message = res.message || '删除宠物失败，请稍后重试。';
      successModal.visible = true;
      return;
    }

    // 后端删除成功后刷新当前列表
    if (petToDelete.value.type === 'short') {
      await loadMyShortPets();
    } else {
      await loadMyLongPets();
    }

    successModal.title = '删除成功';
    successModal.message = '宠物信息已被删除。';
    successModal.visible = true;
  } catch (err) {
    console.error('删除宠物接口异常', err);
    successModal.title = '删除失败';
    successModal.message = '删除宠物接口异常，请稍后重试。';
    successModal.visible = true;
  } finally {
    showDeleteConfirm.value = false;
    petToDelete.value = null;
  }
}

function closeSuccessModal() {
  successModal.visible = false;
}

// 监听路由变化，当有 refresh 参数时重新加载数据
watch(() => route.query.refresh, (refresh) => {
  if (refresh === 'true') {
    // 重新加载当前标签页的数据
    if (activeTab.value === 'short') {
      loadMyShortPets();
    } else {
      loadMyLongPets();
    }
    // 移除 refresh 参数，避免重复刷新
    router.replace({ path: route.path, query: { menu: route.query.menu } });
  }
}, { immediate: true });

onMounted(() => {
  loadMyShortPets();
  // 长期宠物列表在用户切换到"长期宠物"标签时按需加载
});

</script>

<style scoped>
</style>


