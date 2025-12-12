<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-[#111318] dark:text-white text-3xl font-bold leading-tight tracking-[-0.033em]">管理申请</h1>
    </header>
    <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white dark:bg-[#181C25] rounded-xl border border-gray-100 dark:border-gray-800 p-5">
        <p class="text-sm text-gray-500 dark:text-gray-400">待审核申请</p>
        <div class="flex items-end justify-between">
          <h3 class="text-3xl font-semibold text-[#111318] dark:text-white">{{ stats.pending }}</h3>
        </div>
      </div>
    </section>
    <section class="bg-white dark:bg-[#181C25] rounded-xl border border-gray-100 dark:border-gray-800 shadow-sm">
      <div class="border-b border-gray-100 dark:border-gray-800 p-5 flex flex-wrap gap-4 justify-between items-center">
        <div>
          <h2 class="text-xl font-semibold text-[#111318] dark:text-white">待审核的领养申请列表</h2>
        </div>
        <div class="relative">
          <input
            v-model="search"
            type="text"
            placeholder="搜索申请..."
            class="pl-10 pr-4 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-transparent text-sm text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/40"
          />
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">search</span>
        </div>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 2xl:grid-cols-4 gap-5 p-5">
        <div
          v-for="app in paginatedApplications"
          :key="app.id"
          class="rounded-2xl border border-gray-100 dark:border-gray-800 bg-white dark:bg-[#1C202B] shadow-sm p-5 flex flex-col gap-4"
        >
          <div class="flex items-center gap-3">
            <span class="px-2 py-1 rounded-full text-xs bg-primary/10 text-primary">#{{ app.id }}</span>
          </div>
          <div class="space-y-2.5 text-xs text-gray-600 dark:text-gray-400">
            <div class="flex items-center gap-2">
              <p class="text-gray-500 dark:text-gray-400">申请者：</p>
              <img
                v-if="app.applicantAvatar"
                :src="app.applicantAvatar"
                :alt="app.applicant"
                class="size-7 rounded-full object-cover shrink-0"
                @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
              />
              <div
                v-if="!app.applicantAvatar || !app.applicantAvatar.trim()"
                class="size-7 rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-300 flex items-center justify-center text-xs font-medium shrink-0"
              >
                {{ getNameInitial(app.applicant) }}
              </div>
              <p class="text-gray-900 dark:text-white font-medium">{{ app.applicant }}</p>
            </div>
            <div class="flex items-center gap-2">
              <p class="text-gray-500 dark:text-gray-400">被申请用户：</p>
              <img
                v-if="app.targetUserAvatar"
                :src="app.targetUserAvatar"
                :alt="app.targetUser"
                class="size-7 rounded-full object-cover shrink-0"
                @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
              />
              <div
                v-if="!app.targetUserAvatar || !app.targetUserAvatar.trim()"
                class="size-7 rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-300 flex items-center justify-center text-xs font-medium shrink-0"
              >
                {{ getNameInitial(app.targetUser) }}
              </div>
              <p class="text-gray-900 dark:text-white font-medium">{{ app.targetUser }}</p>
            </div>
            <div class="flex items-center gap-2">
              <p class="text-gray-500 dark:text-gray-400">申请的宠物：</p>
              <p class="text-gray-900 dark:text-white font-medium">{{ app.petName }}</p>
            </div>
          </div>
          <div class="flex items-center gap-2 text-xs text-gray-500 dark:text-gray-400">
            <p class="text-gray-500 dark:text-gray-400">申请时间：</p>
            <span>{{ app.time }}</span>
          </div>
          <div class="flex items-center gap-2 justify-end">
            <button
              title="审核通过"
              class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-green-500 hover:bg-green-50 dark:hover:bg-green-900/30"
              @click="handleApprove(app)"
            >
              <span class="material-symbols-outlined">check</span>
            </button>
            <button
              title="审核不通过"
              class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30"
              @click="handleReject(app)"
            >
              <span class="material-symbols-outlined">close</span>
            </button>
            <button
              title="查看详情"
              class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/30"
              @click="handleViewDetail(app)"
            >
              <span class="material-symbols-outlined">visibility</span>
            </button>
          </div>
        </div>
      </div>
      <div class="flex flex-wrap items-center justify-between gap-4 border-t border-gray-100 dark:border-gray-800 p-5">
        <p class="text-sm text-gray-500 dark:text-gray-400">
          显示 {{ applicationsTotal ? (currentPage - 1) * PAGE_SIZE + 1 : 0 }} 到 {{ Math.min(currentPage * PAGE_SIZE, applicationsTotal) }} 条，共 {{ applicationsTotal }} 条
        </p>
        <div class="flex gap-2">
          <button
            :disabled="currentPage === 1"
            class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
            @click="currentPage--"
          >
            上一页
          </button>
          <button
            v-for="(page, index) in getDisplayedPages(currentPage, totalPages)"
            :key="index"
            class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm transition-colors"
            :class="[
              page === currentPage ? 'bg-blue-500 text-white border-blue-500' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800',
              typeof page === 'string' ? 'cursor-default border-transparent hover:bg-transparent' : 'cursor-pointer'
            ]"
            @click="typeof page === 'number' && (currentPage = page)"
          >
            {{ page }}
          </button>
          <button
            :disabled="currentPage === totalPages"
            class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
            @click="currentPage++"
          >
            下一页
          </button>
        </div>
      </div>
    </section>

    <!-- 弹窗组件 -->
    <ApplicationDetailModal
      :visible="showApplicationDetailModal"
      :application-data="selectedApplication ? {
        id: selectedApplication.id,
        applicant: selectedApplication.applicant,
        applicantAvatar: selectedApplication.applicantAvatar,
        petName: selectedApplication.petName,
        targetUser: selectedApplication.targetUser,
        targetUserAvatar: selectedApplication.targetUserAvatar,
        time: selectedApplication.time,
        phone: selectedApplicationDetail?.contactInfo?.split(',')[0] || '138-****-2640',
        email: selectedApplicationDetail?.contactInfo?.split(',')[1] || `${selectedApplication.applicant?.toLowerCase()}@example.com`,
        address: selectedApplicationDetail?.address || '上海市徐汇区桂林路 188 弄',
        reason: selectedApplicationDetail?.adoptReason || selectedApplicationDetail?.reason || '希望领养一只性格温顺的宠物作为伴侣。'
      } : undefined"
      @close="showApplicationDetailModal = false"
    />
    <ApproveModal
      :visible="showApproveModal"
      message="该申请已成功通过审核。"
      @close="showApproveModal = false"
      @confirm="onApproveConfirm"
    />
    <RejectModal
      :visible="showRejectModal"
      message="该申请审核未通过，已通知申请人。"
      @close="showRejectModal = false"
      @confirm="onRejectConfirm"
    />
    <ConfirmModal
      :visible="showConfirmModal"
      :title="confirmAction === 'approve' ? '确认审核通过' : '确认审核不通过'"
      :message="confirmAction === 'approve' ? '确定要通过该申请的审核吗？' : '确定要拒绝该申请的审核吗？'"
      @confirm="onConfirmModalConfirm"
      @cancel="onConfirmModalCancel"
      @close="showConfirmModal = false"
    />
    <RejectReasonModal
      :visible="showRejectReasonModal"
      title="请输入拒绝原因"
      message="请输入拒绝原因（可选）:"
      @confirm="onRejectReasonConfirm"
      @cancel="onRejectReasonCancel"
      @close="onRejectReasonCancel"
    />
    <ErrorModal
      :visible="showErrorModal"
      :message="errorMessage"
      @confirm="showErrorModal = false"
      @close="showErrorModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, watch } from 'vue';
import ApplicationDetailModal from '../../components/admin/ApplicationDetailModal.vue';
import ApproveModal from '../../components/admin/ApproveModal.vue';
import RejectModal from '../../components/admin/RejectModal.vue';
import ConfirmModal from '../../components/admin/ConfirmModal.vue';
import RejectReasonModal from '../../components/admin/RejectReasonModal.vue';
import ErrorModal from '../../components/admin/ErrorModal.vue';
import {
  getPendingAdopts,
  getProcessedAdopts,
  getAdoptDetail,
  approveAdopt,
  rejectAdopt,
  type AdminAdoptSummaryDto,
  type AdminAdoptDetailDto
} from '../../api/adminApi';
import { formatDateTime } from '@/utils/format';

interface Application {
  id: number;
  applicant: string;
  applicantAvatar?: string;
  petName: string;
  targetUser: string;
  targetUserAvatar?: string;
  time: string;
}

const PAGE_SIZE = 4;
const stats = reactive({
  pending: 0,
  processed: 0,
  total: 0
});
const search = ref('');
const currentPage = ref(1);
const applications = ref<Application[]>([]);
const applicationsTotal = ref(0);
const loading = ref(false);

function getNameInitial(name: string): string {
  if (!name) return '用';
  return name.trim().charAt(0) || '用';
}

// 后端分页，前端仅做搜索过滤
const filteredApplications = computed(() => {
  if (!search.value) return applications.value;
  const searchLower = search.value.toLowerCase();
  return applications.value.filter(
    app => app.applicant.toLowerCase().includes(searchLower) ||
            app.petName.toLowerCase().includes(searchLower) ||
            app.targetUser.toLowerCase().includes(searchLower)
  );
});

const totalPages = computed(() => Math.ceil(applicationsTotal.value / PAGE_SIZE));

const paginatedApplications = computed(() => filteredApplications.value);

// 将后端 AdminAdoptSummaryDto 映射到前端展示用 Application
function mapAdoptToApplication(item: AdminAdoptSummaryDto): Application {
  const createTime = formatDateTime(item.createTime);
  return {
    id: item.adoptId ?? 0,
    applicant: item.userName ?? '未知申请人',
    applicantAvatar: item.userAvatar,
    petName: item.animalName ?? '未知宠物',
    targetUser: item.targetUserName ?? '未知用户',
    targetUserAvatar: item.targetUserAvatar,
    time: createTime
  };
}

// 加载待审核领养申请列表
async function loadApplications() {
  try {
    loading.value = true;
    const res = await getPendingAdopts({
      page: currentPage.value,
      pageSize: PAGE_SIZE,
      keyword: search.value || undefined
    });
    if ((res.code === 0 || res.code === 200) && res.data) {
      const list = res.data.list || res.data.records || [];
      applications.value = list.map(mapAdoptToApplication);
      applicationsTotal.value = res.data.total ?? list.length;
      stats.pending = applicationsTotal.value;
      stats.total = stats.pending + stats.processed;
    } else {
      console.warn('获取待审核领养申请列表失败', res);
    }
  } catch (error) {
    console.error('获取待审核领养申请列表异常', error);
  } finally {
    loading.value = false;
  }
}

// 加载已处理申请数量（仅用于统计卡片）
async function loadProcessedStats() {
  try {
    const res = await getProcessedAdopts({
      page: 1,
      pageSize: 1
    });
    if ((res.code === 0 || res.code === 200) && res.data) {
      const total = res.data.total ?? (res.data.list?.length || res.data.records?.length || 0);
      stats.processed = total;
      stats.total = stats.pending + stats.processed;
    }
  } catch (error) {
    console.error('获取已处理领养申请统计异常', error);
  }
}

// 弹窗状态
const showApplicationDetailModal = ref(false);
const showApproveModal = ref(false);
const showRejectModal = ref(false);
const showConfirmModal = ref(false);
const showRejectReasonModal = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
const confirmAction = ref<'approve' | 'reject' | null>(null);
const selectedApplication = ref<Application | null>(null);

function handleApprove(app: Application) {
  selectedApplication.value = app;
  confirmAction.value = 'approve';
  showConfirmModal.value = true;
}

function handleReject(app: Application) {
  selectedApplication.value = app;
  confirmAction.value = 'reject';
  showConfirmModal.value = true;
}

const selectedApplicationDetail = ref<any>(null);

async function handleViewDetail(app: Application) {
  try {
    const res = await getAdoptDetail(app.id);
    if ((res.code === 0 || res.code === 200) && res.data) {
      const data = res.data;
      selectedApplication.value = {
        id: data.adoptId ?? app.id,
        applicant: data.userName ?? app.applicant,
        applicantAvatar: data.userAvatar ?? app.applicantAvatar,
        petName: data.animalName ?? app.petName,
        targetUser: data.targetUserName ?? app.targetUser,
        targetUserAvatar: data.targetUserAvatar ?? app.targetUserAvatar,
        time: formatDateTime(data.createTime) || app.time
      };
      selectedApplicationDetail.value = data;
      showApplicationDetailModal.value = true;
    } else {
      selectedApplication.value = app;
      selectedApplicationDetail.value = null;
      showApplicationDetailModal.value = true;
    }
  } catch (error) {
    console.error('获取领养申请详情异常', error);
  selectedApplication.value = app;
    selectedApplicationDetail.value = null;
  showApplicationDetailModal.value = true;
}
}

async function onConfirmModalConfirm() {
  if (!selectedApplication.value || !confirmAction.value) {
    console.warn('确认弹窗回调时缺少选中申请或动作类型', {
      selectedApplication: selectedApplication.value,
      confirmAction: confirmAction.value
    });
    return;
  }

  console.log('ConfirmModal 确认操作', {
    action: confirmAction.value,
    applicationId: selectedApplication.value.id
  });
  
  showConfirmModal.value = false;
  
  if (confirmAction.value === 'reject') {
    // 如果是拒绝操作，先显示拒绝理由输入弹窗
    console.log('准备显示拒绝理由弹窗');
    showRejectReasonModal.value = true;
    return;
  }

  // 其他操作直接执行
  await executeAction();
}

async function executeAction(reason: string = '') {
  if (!selectedApplication.value || !confirmAction.value) {
    console.warn('执行审核操作时缺少选中申请或动作类型', {
      selectedApplication: selectedApplication.value,
      confirmAction: confirmAction.value,
      reason
    });
    return;
  }

  console.log('开始执行审核操作', {
    action: confirmAction.value,
    applicationId: selectedApplication.value.id,
    reason
  });

  try {
  if (confirmAction.value === 'approve') {
      // 审核通过
      const res = await approveAdopt(selectedApplication.value.id, { adoptId: selectedApplication.value.id });
      if (res.code === 0 || res.code === 200) {
        console.log('审核通过请求成功', res);
    showApproveModal.value = true;
        // 重新加载列表
        await loadApplications();
        await loadProcessedStats();
      } else {
        console.error('审核通过请求失败', res);
        errorMessage.value = res.message || '审核通过失败';
        showErrorModal.value = true;
      }
  } else if (confirmAction.value === 'reject') {
      // 审核拒绝
      const res = await rejectAdopt(selectedApplication.value.id, { adoptId: selectedApplication.value.id, reason });
      if (res.code === 0 || res.code === 200) {
        console.log('审核拒绝请求成功', res);
    showRejectModal.value = true;
        // 重新加载列表
        await loadApplications();
        await loadProcessedStats();
      } else {
        console.error('审核拒绝请求失败', res);
        errorMessage.value = res.message || '审核拒绝失败';
        showErrorModal.value = true;
      }
    }
  } catch (error: any) {
    console.error('操作失败', error);
    errorMessage.value = error?.message || '操作失败，请稍后重试';
    showErrorModal.value = true;
  }
}

function onRejectReasonConfirm(reason: string) {
  showRejectReasonModal.value = false;
  executeAction(reason);
}

function onRejectReasonCancel() {
  showRejectReasonModal.value = false;
  confirmAction.value = null;
  selectedApplication.value = null;
}

function onConfirmModalCancel() {
  showConfirmModal.value = false;
  confirmAction.value = null;
}

function onApproveConfirm() {
  showApproveModal.value = false;
  selectedApplication.value = null;
  confirmAction.value = null;
}

function onRejectConfirm() {
  showRejectModal.value = false;
  selectedApplication.value = null;
  confirmAction.value = null;
}

onMounted(() => {
  loadApplications();
  loadProcessedStats();
});

// 监听分页变化
watch(currentPage, () => {
  loadApplications();
});

// 监听搜索关键词变化（防抖）
let searchTimer: number | undefined;
watch(search, () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = window.setTimeout(() => {
    currentPage.value = 1;
    loadApplications();
  }, 500);
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
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;900&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 24;
}
</style>

