<template>
  <div>
    <h2 class="text-2xl font-bold mb-5" style="color: #111;">我的待办</h2>

    <div v-if="pagedTodos.length" class="flex flex-col gap-5">
      <div 
        v-for="todo in pagedTodos" 
        :key="todo.id"
        class="bg-white rounded-xl p-6 shadow-sm border-l-4"
        style="border-left-color: #FF8C00;"
      >
        <div class="flex justify-between mb-4 pb-4 border-b border-gray-100">
          <div class="flex items-center gap-2 text-sm font-bold" style="color: #FF8C00;">
            <i :class="todo.typeIcon"></i>
            <span>{{ todo.type }}</span>
          </div>
          <div class="text-xs" style="color: #9CA3AF;">{{ todo.date }}</div>
        </div>

        <div class="flex flex-col gap-4 mb-5 cursor-pointer" @click="handleViewDetail(todo)">
          <div class="flex-1">
            <div class="text-base font-bold mb-2.5" style="color: #1F2937;">{{ todo.title }}</div>
            
            <div class="flex items-center gap-2 mb-3 text-sm" style="color: #333;">
              <span class="font-normal">申请人：</span>
              <div class="w-6 h-6 rounded-full overflow-hidden bg-gray-200 flex items-center justify-center text-xs mr-0.5">
                <img
                  v-if="todo.avatar"
                  :src="todo.avatar"
                  alt="avatar"
                  class="w-full h-full object-cover"
                />
                <i v-else class="fa-regular fa-circle"></i>
              </div>
              <span 
                class="font-bold cursor-pointer transition-colors hover:text-[#FF8C00]" 
                style="color: #111;"
                @click.stop="router.push({ name: 'UserProfile', params: { userId: todo.requesterId || 1 } })"
              >
                {{ todo.requester }}
              </span>
            </div>

            <div class="bg-gray-50 p-4 rounded-lg text-xs leading-relaxed" style="color: #4B5563;">
              {{ todo.reason }}
            </div>
          </div>
        </div>

        <div class="flex justify-end items-center gap-2.5">
          <!-- 已处理结果提示 -->
          <span
            v-if="todo.resultText"
            class="px-4 py-2 rounded-md text-sm font-medium"
            style="background-color: #F3F4F6; color: #4B5563;"
          >
            {{ todo.resultText }}
          </span>

          <!-- 操作按钮：仅在未处理时显示 -->
          <button 
            v-if="!todo.resultText && todo.showConfirm"
            class="px-5 py-2 rounded-md text-sm font-medium cursor-pointer transition-colors hover:opacity-90"
            style="background-color: #FF8C00; color: white;"
            @click="handleConfirm(todo)"
          >
            同意领养
          </button>
          <button 
            v-if="!todo.resultText"
            class="px-5 py-2 rounded-md text-sm font-medium cursor-pointer transition-colors border border-gray-300 hover:border-[#FF8C00] hover:text-[#FF8C00]"
            style="background-color: white; color: #333;"
            @click="handleContact(todo)"
          >
            联系申请人
          </button>
          <button 
            v-if="!todo.resultText && todo.showReject"
            class="px-5 py-2 rounded-md text-sm font-medium cursor-pointer transition-colors hover:bg-red-100"
            style="background-color: #FEF2F2; color: #EF4444;"
            @click="handleReject(todo)"
          >
            婉拒
          </button>
        </div>
      </div>
    </div>

    <div v-else class="mt-8 text-center text-sm" style="color: #9CA3AF;">
      当前没有待办事项
    </div>

    <!-- 分页：统一样式，空列表也显示，至少一页 -->
    <div class="flex justify-center mt-10 mb-5">
      <div class="flex items-center gap-2.5">
        <button 
          class="w-11 h-11 rounded-lg border border-gray-300 bg-white text-base cursor-pointer flex items-center justify-center transition-all hover:border-[#FF8C00] hover:text-[#FF8C00]"
          style="color: #6B7280;"
          :disabled="currentPage === 1"
          :class="currentPage === 1 ? 'cursor-not-allowed opacity-50' : ''"
          @click="goPage(Math.max(1, currentPage - 1))"
        >
          <i class="fa-solid fa-chevron-left"></i>
        </button>
        <button 
          v-for="(page, index) in getDisplayedPages(currentPage, totalPages)" 
          :key="index"
          class="w-11 h-11 rounded-lg border text-base flex items-center justify-center transition-all"
          :class="[
            page === currentPage ? 'bg-[#FF8C00] text-white border-[#FF8C00]' : 'bg-white border-gray-300 text-gray-600',
            typeof page === 'string' ? 'cursor-default border-transparent' : 'cursor-pointer hover:border-[#FF8C00] hover:text-[#FF8C00]'
          ]"
          style="color: #6B7280;"
          @click="typeof page === 'number' && goPage(page)"
        >
          {{ page }}
        </button>
        <button 
          class="w-11 h-11 rounded-lg border border-gray-300 bg-white text-base cursor-pointer flex items-center justify-center transition-all hover:border-[#FF8C00] hover:text-[#FF8C00]"
          style="color: #6B7280;"
          :disabled="currentPage === totalPages"
          :class="currentPage === totalPages ? 'cursor-not-allowed opacity-50' : ''"
          @click="goPage(Math.min(totalPages, currentPage + 1))"
        >
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <!-- 成功弹窗 -->
    <SuccessModal
      :visible="showSuccessModal"
      title="操作成功"
      :message="successMessage"
      @close="closeSuccessModal"
    />

    <!-- 错误/提示弹窗 -->
    <ErrorModal
      :visible="showErrorModal"
      title="提示"
      :message="errorMessage"
      @close="closeErrorModal"
    />

    <!-- 确认弹窗 - 同意申请 -->
    <ConfirmModal
      :visible="showConfirmModal"
      title="确认操作"
      :message="confirmMessage"
      @confirm="onConfirmConfirm"
      @cancel="showConfirmModal = false; currentTodo = null"
    />

    <!-- 确认弹窗 - 婉拒申请 -->
    <ConfirmModal
      :visible="showRejectModal"
      title="确认操作"
      :message="confirmMessage"
      @confirm="onRejectConfirm"
      @cancel="showRejectModal = false; currentTodo = null"
    />

    <!-- 申请详情弹窗 -->
    <ApplicationDetailModal
      v-if="currentTodo"
      :visible="showApplicationDetailModal"
      :application="currentTodo"
      @close="closeApplicationDetailModal"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import SuccessModal from '../../../components/common/SuccessModal.vue';
import ErrorModal from '../../../components/common/ErrorModal.vue';
import ConfirmModal from '../../../components/common/ConfirmModal.vue';
import ApplicationDetailModal from '../../../components/common/ApplicationDetailModal.vue';
import { 
  getMyAdoptTodoList, 
  getAdoptDetail, 
  reviewAdopt,
  ApplicationStatus,
  type AdoptDetail, 
  type AdoptTodoItem 
} from '@/api/adoptApi';

import { formatDateTime } from '@/utils/format';

const router = useRouter();

interface Todo {
  id: number;
  type: string;
  typeIcon: string;
  date: string;
  title: string;
  requester: string;
  requesterId?: number;
  reason: string;
  showConfirm: boolean;
  showReject: boolean;
  avatar?: string;
  // 处理结果（同意 / 婉拒），用于在本页面上展示状态变化
  resultText?: string;
  // 申请详情相关字段
  phone?: string;
  email?: string;
  address?: string;
  petName?: string;
}

const todos = ref<Todo[]>([]);
const currentPage = ref(1);
const pageSize = 3;
const totalPages = computed(() => Math.max(1, Math.ceil((todos.value.length || 0) / pageSize)));
const pagedTodos = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return todos.value.slice(start, start + pageSize);
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

function goPage(page: number) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
}

const showSuccessModal = ref(false);
const showErrorModal = ref(false);
const showConfirmModal = ref(false);
const showRejectModal = ref(false);
const showApplicationDetailModal = ref(false);
const successMessage = ref('');
const errorMessage = ref('');
const currentTodo = ref<Todo | null>(null);
const confirmMessage = ref('');
const loadingDetail = ref(false);

function handleConfirm(todo: Todo) {
  currentTodo.value = todo;
  confirmMessage.value = `确定同意${todo.requester}的领养申请吗？`;
  showConfirmModal.value = true;
}

async function handleViewDetail(todo: Todo) {
  // 点击时调用 /adopt/{id} 获取最新的申请详情
  loadingDetail.value = true;
  try {
    const res = await getAdoptDetail(todo.id);
    if (res.code === 200 && res.data) {
      const detail: AdoptDetail = res.data;

      currentTodo.value = {
        ...todo,
        avatar: (detail as any).applicantAvatar || todo.avatar,
        requester: detail.userName || todo.requester,
        date: formatDateTime(detail.createTime) || todo.date,
        phone: detail.phone || todo.phone,
        email: detail.email || todo.email,
        address: detail.livingLocation || todo.address,
        petName: (detail as any).animalName || todo.petName,
        reason: detail.adoptReason || todo.reason,
      };
      showApplicationDetailModal.value = true;
    } else {
      errorMessage.value = '获取申请详情失败，请稍后重试';
      showErrorModal.value = true;
      console.error('获取申请详情失败', res);
    }
  } catch (err) {
    errorMessage.value = '获取申请详情接口异常，请稍后重试';
    showErrorModal.value = true;
    console.error('获取申请详情接口异常', err);
  } finally {
    loadingDetail.value = false;
  }
}

function handleContact(todo: Todo) {
  // 跳转到沟通对接页面，并传递申请人ID
  router.push({ 
    name: 'Communication', 
    query: { userId: todo.requesterId || 1 } 
  });
}

function closeApplicationDetailModal() {
  showApplicationDetailModal.value = false;
  currentTodo.value = null;
}

function handleReject(todo: Todo) {
  currentTodo.value = todo;
  confirmMessage.value = `确定要婉拒${todo.requester}的领养申请吗？`;
  showRejectModal.value = true;
}

async function onConfirmConfirm() {
  if (!currentTodo.value) {
    showConfirmModal.value = false;
    return;
  }

  try {
    const res = await reviewAdopt(currentTodo.value.id, {
      applicationStatus: ApplicationStatus.申请成功
    });
    if (res.code === 200 || res.code === 0) {
      successMessage.value = '已同意领养申请';
      showSuccessModal.value = true;
      // 本地更新当前待办项的显示状态
      todos.value = todos.value.map(todo =>
        todo.id === currentTodo.value?.id
          ? { ...todo, resultText: '已同意领养申请', showConfirm: false, showReject: false }
          : todo
      );
      // 重新加载列表以获取最新状态
      await loadTodos();
    } else {
      errorMessage.value = res.message || '同意领养申请失败，请稍后重试';
      showErrorModal.value = true;
    }
  } catch (err: any) {
    console.error('同意领养申请失败', err);
    errorMessage.value = err?.message || '同意领养申请失败，请稍后重试';
    showErrorModal.value = true;
  }

  showConfirmModal.value = false;
  currentTodo.value = null;
}

async function onRejectConfirm() {
  if (!currentTodo.value) {
    showRejectModal.value = false;
    return;
  }

  try {
    const res = await reviewAdopt(currentTodo.value.id, {
      applicationStatus: ApplicationStatus.申请失败
    });
    if (res.code === 200 || res.code === 0) {
      successMessage.value = '已婉拒申请';
      showSuccessModal.value = true;
      // 本地更新当前待办项的显示状态
      todos.value = todos.value.map(todo =>
        todo.id === currentTodo.value?.id
          ? { ...todo, resultText: '已婉拒申请', showConfirm: false, showReject: false }
          : todo
      );
      // 重新加载列表以获取最新状态
      await loadTodos();
    } else {
      errorMessage.value = res.message || '婉拒申请失败，请稍后重试';
      showErrorModal.value = true;
    }
  } catch (err: any) {
    console.error('婉拒申请失败', err);
    errorMessage.value = err?.message || '婉拒申请失败，请稍后重试';
    showErrorModal.value = true;
  }

  showRejectModal.value = false;
  currentTodo.value = null;
}

function closeSuccessModal() {
  showSuccessModal.value = false;
}

function closeErrorModal() {
  showErrorModal.value = false;
}

async function loadTodos() {
  try {
    const res = await getMyAdoptTodoList();
    if (res.code === 200 && Array.isArray(res.data)) {
      todos.value = res.data.map((item: AdoptTodoItem, index: number) => {
        const id = item.adoptId ?? index + 1;
        const requester = item.applicantName || '未知申请人';
        const petName = item.animalName || '';
        const createdAt = formatDateTime(item.createTime);
        const reason = item.reason || '';
        return {
          id,
          type: '领养申请',
          typeIcon: 'fa-solid fa-heart',
          date: createdAt,
          title: petName ? `对"${petName}"的领养申请` : '领养申请',
          requester,
          requesterId: item.applicantId || undefined,
          reason,
          showConfirm: true,
          showReject: true,
          avatar: item.applicantAvatar || undefined,
          resultText: undefined,
          // 列表接口目前不返回电话/邮箱/地址等，这些在详情接口中获取
          phone: undefined,
          email: undefined,
          address: undefined,
          petName
        } as Todo;
      });
    } else {
      console.error('获取待办列表失败', res);
    }
  } catch (err) {
    console.error('获取待办列表接口异常', err);
  }
}

onMounted(() => {
  loadTodos();
});
</script>

<style scoped>
</style>

