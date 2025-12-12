<template>
  <div>
    <h2 class="text-2xl font-bold mb-5" style="color: #111;">我的申请记录</h2>

    <div v-if="applications.length" class="flex flex-col gap-5">
      <div 
        v-for="application in applications" 
        :key="application.id"
        class="bg-white rounded-xl p-6 shadow-sm flex justify-between items-center gap-5 cursor-pointer"
        @click="handleViewDetail(application)"
      >
        <!-- 左侧：图片和基本信息 -->
        <div class="flex gap-5 flex-1">
          <div class="w-20 h-20 rounded-lg overflow-hidden flex-shrink-0" style="background-color: #FFF3E0;">
            <img
              v-if="application.animalPhoto"
              :src="normalizeImageUrl(application.animalPhoto)"
              :alt="application.animalName"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-xs text-gray-400">
              {{ application.animalName }}的照片
            </div>
          </div>
          <div class="flex flex-col justify-center">
            <div class="text-base font-bold mb-1" style="color: #1F2937;">申请领养："{{ application.animalName }}"</div>
            <div class="flex items-center gap-2 mb-0.5">
              <span class="text-xs" style="color: #6B7280;">被申请人：</span>
              <div class="w-5 h-5 rounded-full overflow-hidden flex-shrink-0" style="background-color: #F3C697;">
                <img
                  v-if="application.targetUserAvatar"
                  :src="normalizeImageUrl(application.targetUserAvatar)"
                  :alt="application.targetUserName"
                  class="w-full h-full object-cover"
                />
                <span v-else class="w-full h-full flex items-center justify-center text-xs text-white">
                  {{ application.targetUserName?.charAt(0) || '?' }}
                </span>
              </div>
              <span class="text-xs" style="color: #6B7280;">{{ application.targetUserName || '未知' }}</span>
            </div>
            <div class="text-xs" style="color: #9CA3AF;">申请日期：{{ formatDateTime(application.createTime) }}</div>
          </div>
        </div>

        <!-- 右侧：状态和操作 -->
        <div class="flex flex-col items-end gap-2.5">
          <span 
            class="text-xs font-semibold px-3 py-1 rounded-full flex items-center gap-1"
            :style="{ 
              backgroundColor: getStatusBg(application.comprehensiveStatus), 
              color: getStatusColor(application.comprehensiveStatus) 
            }"
          >
            <i :class="getStatusIcon(application.comprehensiveStatus)"></i>
            {{ application.comprehensiveStatus }}
          </span>
          <button 
            v-if="application.comprehensiveStatus === '等待管理员审核' || application.comprehensiveStatus === '等待被申请人确认'"
            class="border border-gray-300 bg-white px-4 py-1.5 rounded-md text-xs cursor-pointer transition-colors hover:border-[#FF8C00] hover:text-[#FF8C00]"
            style="color: #4B5563;"
            @click.stop="handleCancel(application)"
          >
            撤销申请
          </button>
          <button 
            v-if="application.comprehensiveStatus === '被管理员拒绝' || application.comprehensiveStatus === '被申请人拒绝' || application.comprehensiveStatus === '已自己撤销'"
            class="border border-gray-300 bg-white px-4 py-1.5 rounded-md text-xs cursor-pointer transition-colors hover:border-[#FF8C00] hover:text-[#FF8C00]"
            style="color: #4B5563;"
            @click.stop="handleReapply(application)"
          >
            重新申请
          </button>
        </div>
      </div>
    </div>

    <div v-else class="mt-8 text-center text-sm" style="color: #9CA3AF;">
      当前没有领养申请记录
    </div>

    <!-- 分页：统一样式，列表为空也显示，至少一页 -->
    <div class="flex justify-center mt-10 mb-5">
      <div class="flex items-center gap-2.5">
        <button 
          class="w-11 h-11 rounded-lg border border-gray-300 bg-white text-base cursor-pointer flex items-center justify-center transition-all hover:border-[#FF8C00] hover:text-[#FF8C00]"
          style="color: #6B7280;"
          :disabled="currentPage === 1"
          :class="currentPage === 1 ? 'cursor-not-allowed opacity-50' : ''"
          @click="prevPage"
        >
          <i class="fa-solid fa-chevron-left"></i>
        </button>
        <button 
          v-for="(page, index) in getDisplayedPages(currentPage, totalPages)" 
          :key="index"
          class="w-11 h-11 rounded-lg border border-gray-300 text-base flex items-center justify-center transition-all"
          :class="[
            page === currentPage ? 'bg-[#FF8C00] text-white border-[#FF8C00]' : 'bg-white text-gray-600',
            typeof page === 'string' ? 'cursor-default border-transparent' : 'cursor-pointer hover:border-[#FF8C00] hover:text-[#FF8C00]'
          ]"
          style="color: #6B7280;"
          @click="typeof page === 'number' && (currentPage = page, loadApplications())"
        >
          {{ page }}
        </button>
        <button 
          class="w-11 h-11 rounded-lg border border-gray-300 bg-white text-base cursor-pointer flex items-center justify-center transition-all hover:border-[#FF8C00] hover:text-[#FF8C00]"
          style="color: #6B7280;"
          :disabled="currentPage === totalPages"
          :class="currentPage === totalPages ? 'cursor-not-allowed opacity-50' : ''"
          @click="nextPage"
        >
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <!-- 撤销确认弹窗 -->
    <ConfirmModal
      :visible="showCancelConfirmModal"
      title="确认操作"
      :message="cancelMessage"
      @confirm="confirmCancel"
      @cancel="closeCancelConfirm"
    />

    <!-- 撤销成功弹窗 -->
    <SuccessModal
      :visible="showCancelSuccessModal"
      title="操作成功"
      message="已撤销申请"
      @close="closeCancelSuccessModal"
    />

    <!-- 错误/提示弹窗 -->
    <ErrorModal
      :visible="showErrorModal"
      title="提示"
      :message="errorMessage"
      @close="closeErrorModal"
    />

    <!-- 申请详情弹窗（与“我的待办”保持一致） -->
    <ApplicationDetailModal
      v-if="currentApplication"
      :visible="showApplicationDetailModal"
      :application="currentApplication"
      @close="closeApplicationDetailModal"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import SuccessModal from '../../../components/common/SuccessModal.vue';
import ConfirmModal from '../../../components/common/ConfirmModal.vue';
import ErrorModal from '../../../components/common/ErrorModal.vue';
import ApplicationDetailModal from '../../../components/common/ApplicationDetailModal.vue';
import {
  getMyAdoptMineList,
  getAdoptDetail,
  cancelMyAdopt,
  ApplicationStatus,
  ReviewStatus,
  type AdoptDetail,
  type MyAdoptItemDTO
} from '@/api/adoptApi';
import { formatDateTime } from '@/utils/format';

interface Application extends MyAdoptItemDTO {
  id: number;
  comprehensiveStatus: '等待管理员审核' | '被管理员拒绝' | '等待被申请人确认' | '被申请人同意' | '被申请人拒绝' | '已自己撤销';
  // 详情相关字段（用于详情弹窗）
  avatar?: string;
  requester?: string;
  phone?: string;
  email?: string;
  address?: string;
}

const router = useRouter();
const route = useRoute();
const applications = ref<Application[]>([]);

const currentPage = ref(1);
// 每页最多 3 条申请记录
const pageSize = 3;
const total = ref(0);
const totalPages = computed(() => Math.max(1, Math.ceil((total.value || 0) / pageSize)));

// 规范化图片URL
function normalizeImageUrl(url: string | undefined | null): string {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:') || url.startsWith('blob:')) {
    return url;
  }
  if (url.startsWith('/api/')) {
    return url;
  }
  return `/api/storage/image/${url.replace(/^\/+/, '')}`;
}


// 根据三个状态字段判断综合状态
function determineComprehensiveStatus(
  reviewStatus: string | undefined,
  applicationStatus: string | undefined,
  isCanceled: string | undefined
): Application['comprehensiveStatus'] {
  // 1. 已自己撤销（最高优先级）
  if (isCanceled === '是' || isCanceled === 'true') {
    return '已自己撤销';
  }
  
  // 2. 被管理员拒绝
  if (reviewStatus === 'REJECTED') {
    return '被管理员拒绝';
  }
  
  // 3. 管理员通过 + 被申请人同意
  if (reviewStatus === 'APPROVED' && applicationStatus === '已同意') {
    return '被申请人同意';
  }
  
  // 4. 管理员通过 + 被申请人拒绝
  if (reviewStatus === 'APPROVED' && applicationStatus === '已拒绝') {
    return '被申请人拒绝';
  }
  
  // 5. 管理员通过 + 等待被申请人确认
  if (reviewStatus === 'APPROVED' && applicationStatus === '待审核') {
    return '等待被申请人确认';
  }
  
  // 6. 默认：等待管理员审核
  return '等待管理员审核';
}

function getStatusIcon(status: string) {
  const icons: Record<string, string> = {
    '被申请人同意': 'fa-solid fa-check',
    '等待管理员审核': 'fa-regular fa-clock',
    '等待被申请人确认': 'fa-regular fa-clock',
    '被管理员拒绝': 'fa-solid fa-xmark',
    '被申请人拒绝': 'fa-solid fa-xmark',
    '已自己撤销': 'fa-regular fa-circle-xmark',
  };
  return icons[status] || '';
}

function getStatusBg(status: string) {
  const colors: Record<string, string> = {
    '被申请人同意': '#D1FAE5',
    '等待管理员审核': '#DBEAFE',
    '等待被申请人确认': '#DBEAFE',
    '被管理员拒绝': '#F3F4F6',
    '被申请人拒绝': '#F3F4F6',
    '已自己撤销': '#F3F4F6',
  };
  return colors[status] || '#F3F4F6';
}

function getStatusColor(status: string) {
  const colors: Record<string, string> = {
    '被申请人同意': '#059669',
    '等待管理员审核': '#2563EB',
    '等待被申请人确认': '#2563EB',
    '被管理员拒绝': '#6B7280',
    '被申请人拒绝': '#6B7280',
    '已自己撤销': '#6B7280',
  };
  return colors[status] || '#6B7280';
}

const showCancelSuccessModal = ref(false);
const showCancelConfirmModal = ref(false);
const applicationToCancel = ref<Application | null>(null);
const cancelMessage = ref('');

// 详情弹窗状态
const showApplicationDetailModal = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
const currentApplication = ref<Application | null>(null);

function handleCancel(application: Application) {
  applicationToCancel.value = application;
  cancelMessage.value = `确定要撤销对"${application.animalName}"的申请吗？`;
  showCancelConfirmModal.value = true;
}

async function confirmCancel() {
  if (!applicationToCancel.value) {
    showCancelConfirmModal.value = false;
    return;
  }

  try {
    const id = applicationToCancel.value.id;
    const res = await cancelMyAdopt(id);
    if (res.code === 200) {
      // 刷新列表以更新状态
      loadApplications();
      showCancelSuccessModal.value = true;
    } else {
      errorMessage.value = res.message || '撤销申请失败，请稍后重试';
      showErrorModal.value = true;
      console.error('撤销领养申请失败', res);
    }
  } catch (err) {
    errorMessage.value = '撤销申请接口异常，请稍后重试';
    showErrorModal.value = true;
    console.error('撤销领养申请接口异常', err);
  } finally {
    showCancelConfirmModal.value = false;
    applicationToCancel.value = null;
  }
}

function closeCancelConfirm() {
  showCancelConfirmModal.value = false;
  applicationToCancel.value = null;
}

function closeCancelSuccessModal() {
  showCancelSuccessModal.value = false;
}

function closeErrorModal() {
  showErrorModal.value = false;
}

function closeApplicationDetailModal() {
  showApplicationDetailModal.value = false;
  currentApplication.value = null;
}

// 查看申请详情：调用 /adopt/{id}，与“我的待办”保持一致
async function handleViewDetail(application: Application) {
  try {
    const res = await getAdoptDetail(application.id);
    if (res.code === 200 && res.data) {
      const detail: AdoptDetail = res.data;
      currentApplication.value = {
        ...application,
        avatar: detail.applicantAvatar || application.avatar,
        requester: detail.userName || application.requester,
        phone: detail.phone || application.phone,
        email: detail.email || application.email,
        address: detail.livingLocation || application.address,
        animalName: (detail as any).animalName || application.animalName,
        reason: detail.adoptReason || application.reason,
        date: formatDateTime(application.createTime) || '',
      };
      showApplicationDetailModal.value = true;
    } else {
      errorMessage.value = '获取申请详情失败，请稍后重试';
      showErrorModal.value = true;
      console.error('获取我的申请详情失败', res);
    }
  } catch (err) {
    errorMessage.value = '获取申请详情接口异常，请稍后重试';
    showErrorModal.value = true;
    console.error('获取我的申请详情接口异常', err);
  }
}

async function loadApplications() {
  try {
    const res = await getMyAdoptMineList();
    if (res.code === 200 && res.data) {
      const records = res.data || [];
      total.value = records.length; // 如果后端返回分页信息，使用 res.data.total
      const allApplications = records.map((item: MyAdoptItemDTO, index: number) => {
        const comprehensiveStatus = determineComprehensiveStatus(
          item.reviewStatus,
          item.applicationStatus,
          item.isCanceled
        );
        return {
          ...item,
          id: item.adoptId ?? index + 1,
          comprehensiveStatus,
        } as Application;
      });
      // 应用分页
      const start = (currentPage.value - 1) * pageSize;
      const end = start + pageSize;
      applications.value = allApplications.slice(start, end);
    } else {
      console.error('获取我的申请列表失败', res);
    }
  } catch (err) {
    console.error('获取我的申请列表接口异常', err);
  }
}

// 重新申请：跳转到申请页面并自动填充信息
function handleReapply(application: Application) {
  if (!application.animalId) {
    errorMessage.value = '无法获取宠物信息，请稍后重试';
    showErrorModal.value = true;
    return;
  }
  
  // 获取申请详情以填充表单
  getAdoptDetail(application.id).then(res => {
    if (res.code === 200 && res.data) {
      const detail = res.data;
      // 将申请信息存储到 sessionStorage，供申请页面读取
      sessionStorage.setItem('reapplyData', JSON.stringify({
        animalId: application.animalId,
        fullName: detail.userName || '',
        address: detail.livingLocation || '',
        reason: detail.adoptReason || '',
        contactPhone: detail.phone || '',
        contactEmail: detail.email || '',
        province: detail.province || '',
        city: detail.city || '',
      }));
      // 跳转到申请页面
      router.push({ name: 'ApplyAdoption', params: { id: application.animalId } });
    } else {
      // 如果获取详情失败，仍然跳转，但不填充数据
      router.push({ name: 'ApplyAdoption', params: { id: application.animalId } });
    }
  }).catch(() => {
    // 如果获取详情失败，仍然跳转，但不填充数据
    router.push({ name: 'ApplyAdoption', params: { id: application.animalId } });
  });
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--;
    loadApplications();
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    loadApplications();
  }
}

// 监听路由变化，当有 refresh 参数时重新加载数据
watch(() => route.query.refresh, (refresh) => {
  if (refresh === 'true') {
    loadApplications();
    // 移除 refresh 参数，避免重复刷新
    router.replace({ path: route.path, query: { menu: route.query.menu } });
  }
}, { immediate: true });

onMounted(() => {
  loadApplications();
});

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
</script>

<style scoped>
</style>
