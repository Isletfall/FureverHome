<template>
  <div class="space-y-6">
    <header>
      <h1 class="text-[#111318] dark:text-white text-3xl font-bold leading-tight tracking-[-0.033em]">管理帖子</h1>
    </header>
    <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white dark:bg-[#181C25] rounded-xl border border-gray-100 dark:border-gray-800 p-5">
        <p class="text-sm text-gray-500 dark:text-gray-400">待审核帖子</p>
        <div class="flex items-end justify-between">
          <h3 class="text-3xl font-semibold text-[#111318] dark:text-white">{{ stats.pending }}</h3>
        </div>
      </div>
      <div class="bg-white dark:bg-[#181C25] rounded-xl border border-gray-100 dark:border-gray-800 p-5">
        <p class="text-sm text-gray-500 dark:text-gray-400">已发布帖子</p>
        <div class="flex items-end justify-between">
          <h3 class="text-3xl font-semibold text-[#111318] dark:text-white">{{ stats.published }}</h3>
        </div>
      </div>
    </section>
    <section class="bg-white dark:bg-[#181C25] rounded-xl border border-gray-100 dark:border-gray-800 shadow-sm">
      <div class="flex flex-wrap border-b border-gray-100 dark:border-gray-800 text-sm font-medium">
        <button
          class="py-4 px-6 transition-colors"
          :class="activeTab === 'pending' ? 'text-blue-500 border-b-2 border-blue-500' : 'text-gray-500 border-b-2 border-transparent hover:text-blue-500'"
          @click="activeTab = 'pending'"
        >
          待审核的帖子列表
        </button>
        <button
          class="py-4 px-6 transition-colors"
          :class="activeTab === 'published' ? 'text-blue-500 border-b-2 border-blue-500' : 'text-gray-500 border-b-2 border-transparent hover:text-blue-500'"
          @click="activeTab = 'published'"
        >
          已发布的帖子列表
        </button>
      </div>

      <!-- 待审核标签页 -->
      <div v-if="activeTab === 'pending'" class="post-tab">
        <div class="p-5 flex flex-wrap gap-4 justify-between items-center border-b border-gray-100 dark:border-gray-800">
          <div class="relative">
            <input
              v-model="pendingSearch"
              type="text"
              placeholder="搜索帖子..."
              class="pl-10 pr-4 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-transparent text-sm text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">search</span>
          </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 2xl:grid-cols-4 gap-5 p-5">
          <div
            v-for="post in paginatedPendingPosts"
            :key="post.id"
            class="rounded-2xl border border-gray-100 dark:border-gray-800 bg-white dark:bg-[#1C202B] shadow-sm p-5 flex flex-col gap-4"
          >
            <span class="px-2 py-1 rounded-full text-xs bg-primary/10 text-primary">#{{ post.id }}</span>
            <div class="flex items-center justify-between text-sm text-gray-600 dark:text-gray-400">
              <div class="flex items-center gap-3">
                <img
                  v-if="post.authorAvatar"
                  :src="post.authorAvatar"
                  :alt="post.author"
                  class="size-9 rounded-full object-cover shrink-0"
                  @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
                />
                <div
                  v-if="!post.authorAvatar || !post.authorAvatar.trim()"
                  class="size-9 rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-300 flex items-center justify-center text-xs font-medium shrink-0"
                >
                  {{ post.author?.charAt(0) || '头' }}
                </div>
                <div class="flex flex-col">
                  <span class="text-gray-900 dark:text-white">{{ post.author }}</span>
                </div>
              </div>
              <div class="flex items-center gap-2 text-xs">
                <span class="material-symbols-outlined text-base text-gray-400">schedule</span>
                <span>{{ post.time }}</span>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <div>
                <h3 class="text-base font-semibold text-gray-900 dark:text-white">{{ post.title }}</h3>
                <p class="text-xs text-gray-500 dark:text-gray-400 line-clamp-2 mt-1">{{ post.excerpt }}</p>
              </div>
            </div>
            <div class="flex items-center justify-end gap-2">
              <button
                title="审核通过"
                class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-green-500 hover:bg-green-50 dark:hover:bg-green-900/30"
                @click="handleApprove(post)"
              >
                <span class="material-symbols-outlined">check</span>
              </button>
              <button
                title="审核不通过"
                class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30"
                @click="handleReject(post)"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
              <button
                title="查看详情"
                class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/30"
                @click="handleViewDetail(post)"
              >
                <span class="material-symbols-outlined">visibility</span>
              </button>
            </div>
          </div>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-4 border-t border-gray-100 dark:border-gray-800 p-5">
          <p class="text-sm text-gray-500 dark:text-gray-400">
            显示 {{ pendingTotal ? (currentPendingPage - 1) * PAGE_SIZE + 1 : 0 }} 到 {{ Math.min(currentPendingPage * PAGE_SIZE, pendingTotal) }} 条，共 {{ pendingTotal }} 条
          </p>
          <div class="flex gap-2">
            <button
              :disabled="currentPendingPage === 1"
              class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="currentPendingPage--"
            >
              上一页
            </button>
            <button
              v-for="(page, index) in getDisplayedPages(currentPendingPage, totalPendingPages)"
              :key="index"
              class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm transition-colors"
              :class="[
                page === currentPendingPage ? 'bg-blue-500 text-white border-blue-500' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800',
                typeof page === 'string' ? 'cursor-default border-transparent hover:bg-transparent' : 'cursor-pointer'
              ]"
              @click="typeof page === 'number' && (currentPendingPage = page)"
            >
              {{ page }}
            </button>
            <button
              :disabled="currentPendingPage === totalPendingPages"
              class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="currentPendingPage++"
            >
              下一页
            </button>
          </div>
        </div>
      </div>

      <!-- 已发布标签页 -->
      <div v-if="activeTab === 'published'" class="post-tab">
        <div class="p-5 flex flex-wrap gap-4 justify-between items-center border-b border-gray-100 dark:border-gray-800">
          <div class="relative">
            <input
              v-model="publishedSearch"
              type="text"
              placeholder="搜索帖子..."
              class="pl-10 pr-4 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-transparent text-sm text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">search</span>
          </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 2xl:grid-cols-4 gap-5 p-5">
          <div
            v-for="post in paginatedPublishedPosts"
            :key="post.id"
            class="rounded-2xl border border-gray-100 dark:border-gray-800 bg-white dark:bg-[#1C202B] shadow-sm p-5 flex flex-col gap-4"
          >
            <span class="px-2 py-1 rounded-full text-xs bg-primary/10 text-primary">#{{ post.id }}</span>
            <div class="flex items-center justify-between text-sm text-gray-600 dark:text-gray-400">
              <div class="flex items-center gap-3">
                <img
                  v-if="post.authorAvatar"
                  :src="post.authorAvatar"
                  :alt="post.author"
                  class="size-9 rounded-full object-cover shrink-0"
                  @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex'; }"
                />
                <div
                  v-if="!post.authorAvatar || !post.authorAvatar.trim()"
                  class="size-9 rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-300 flex items-center justify-center text-xs font-medium shrink-0"
                >
                  {{ post.author?.charAt(0) || '头' }}
                </div>
                <div class="flex flex-col">
                  <span class="text-gray-900 dark:text-white">{{ post.author }}</span>
                </div>
              </div>
              <div class="flex items-center gap-2 text-xs">
                <span class="material-symbols-outlined text-base text-gray-400">schedule</span>
                <span>{{ post.time }}</span>
              </div>
            </div>
            <div class="flex items-start gap-3">
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ post.title }}</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400 line-clamp-2">{{ post.excerpt }}</p>
              </div>
            </div>
            <div class="flex items-center justify-end gap-2">
              <button
                title="查看详情"
                class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/30"
                @click="handleViewDetail(post)"
              >
                <span class="material-symbols-outlined">visibility</span>
              </button>
              <button
                title="删除"
                class="inline-flex items-center justify-center rounded-full size-9 border border-transparent text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30"
                @click="handleDelete(post)"
              >
                <span class="material-symbols-outlined">delete</span>
              </button>
            </div>
          </div>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-4 border-t border-gray-100 dark:border-gray-800 p-5">
          <p class="text-sm text-gray-500 dark:text-gray-400">
            显示 {{ publishedTotal ? (currentPublishedPage - 1) * PAGE_SIZE + 1 : 0 }} 到 {{ Math.min(currentPublishedPage * PAGE_SIZE, publishedTotal) }} 条，共 {{ publishedTotal }} 条
          </p>
          <div class="flex gap-2">
            <button
              :disabled="currentPublishedPage === 1"
              class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="currentPublishedPage--"
            >
              上一页
            </button>
            <button
              v-for="(page, index) in getDisplayedPages(currentPublishedPage, totalPublishedPages)"
              :key="index"
              class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm transition-colors"
              :class="[
                page === currentPublishedPage ? 'bg-blue-500 text-white border-blue-500' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800',
                typeof page === 'string' ? 'cursor-default border-transparent hover:bg-transparent' : 'cursor-pointer'
              ]"
              @click="typeof page === 'number' && (currentPublishedPage = page)"
            >
              {{ page }}
            </button>
            <button
              :disabled="currentPublishedPage === totalPublishedPages"
              class="px-3 py-1 border border-gray-300 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="currentPublishedPage++"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 弹窗组件 -->
    <PostDetailModal
      :visible="showPostDetailModal"
      :post-data="selectedPost ? {
        id: selectedPost.id,
        title: selectedPost.title,
        content: selectedPostDetail?.content ?? selectedPost.excerpt,
        excerpt: selectedPost.excerpt,
        author: selectedPost.author,
        authorAvatar: selectedPost.authorAvatar,
        time: selectedPost.time,
        images: selectedPostDetail?.images ?? selectedPostDetail?.mediaUrls ?? [],
        likes: selectedPostDetail?.likeCount,
        views: selectedPostDetail?.viewCount,
        commentCount: selectedPostDetail?.commentCount
      } : undefined"
      @close="showPostDetailModal = false"
    />
    <ApproveModal
      :visible="showApproveModal"
      message="该帖子已成功通过审核并发布。"
      @close="showApproveModal = false"
      @confirm="onApproveConfirm"
    />
    <RejectModal
      :visible="showRejectModal"
      message="该帖子审核未通过，已通知发布者。"
      @close="showRejectModal = false"
      @confirm="onRejectConfirm"
    />
    <DeleteSuccessModal
      :visible="showDeleteSuccessModal"
      message="该帖子已从列表中永久移除。"
      @close="showDeleteSuccessModal = false"
      @confirm="onDeleteConfirm"
    />
    <ConfirmModal
      :visible="showConfirmModal"
      :title="confirmAction === 'approve' ? '确认审核通过' : confirmAction === 'reject' ? '确认审核不通过' : '确认删除'"
      :message="confirmAction === 'approve' ? '确定要通过该帖子的审核吗？' : confirmAction === 'reject' ? '确定要拒绝该帖子的审核吗？' : '确定要删除该帖子吗？此操作不可恢复。'"
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
import { useRoute } from 'vue-router';
import PostDetailModal from '../../components/admin/PostDetailModal.vue';
import ApproveModal from '../../components/admin/ApproveModal.vue';
import RejectModal from '../../components/admin/RejectModal.vue';
import DeleteSuccessModal from '../../components/admin/DeleteSuccessModal.vue';
import ConfirmModal from '../../components/admin/ConfirmModal.vue';
import RejectReasonModal from '../../components/admin/RejectReasonModal.vue';
import ErrorModal from '../../components/admin/ErrorModal.vue';
import {
  getPendingPosts,
  getPublishedPosts,
  getPostDetail,
  approvePost,
  rejectPost,
  deletePost as deletePostApi,
  type AdminPostSummaryDto,
  type AdminPostDetailDto
} from '../../api/adminApi';
import { formatDateTime } from '@/utils/format';

interface Post {
  id: number;
  title: string;
  excerpt: string;
  author: string;
  authorAvatar?: string;
  time: string;
}

const route = useRoute();
const PAGE_SIZE = 4;

const stats = reactive({
  pending: 0,
  published: 0,
  total: 0
});

const activeTab = ref<string>((route.query.tab as string) || 'pending');
const pendingSearch = ref('');
const publishedSearch = ref('');
const currentPendingPage = ref(1);
const currentPublishedPage = ref(1);

// 待审核和已发布帖子数据来自后端
const pendingPosts = ref<Post[]>([]);
const pendingTotal = ref(0);
const publishedPosts = ref<Post[]>([]);
const publishedTotal = ref(0);
const loadingPending = ref(false);
const loadingPublished = ref(false);

const filteredPendingPosts = computed(() => {
  if (!pendingSearch.value) return pendingPosts.value;
  const search = pendingSearch.value.toLowerCase();
  return pendingPosts.value.filter(
    post =>
      post.title.toLowerCase().includes(search) ||
            post.author.toLowerCase().includes(search) ||
            post.excerpt.toLowerCase().includes(search)
  );
});

const filteredPublishedPosts = computed(() => {
  if (!publishedSearch.value) return publishedPosts.value;
  const search = publishedSearch.value.toLowerCase();
  return publishedPosts.value.filter(
    post =>
      post.title.toLowerCase().includes(search) ||
      post.author.toLowerCase().includes(search) ||
      post.excerpt.toLowerCase().includes(search)
  );
});

const totalPendingPages = computed(() => Math.ceil(pendingTotal.value / PAGE_SIZE));
const totalPublishedPages = computed(() => Math.ceil(publishedTotal.value / PAGE_SIZE));

// 待审核列表由后端分页，前端仅做搜索过滤
const paginatedPendingPosts = computed(() => {
  if (!pendingSearch.value) return pendingPosts.value;
  const search = pendingSearch.value.toLowerCase();
  return pendingPosts.value.filter(
    post => post.title.toLowerCase().includes(search) ||
            post.author.toLowerCase().includes(search) ||
            post.excerpt.toLowerCase().includes(search)
  );
});

// 已发布列表由后端分页，前端仅做搜索过滤
const paginatedPublishedPosts = computed(() => {
  if (!publishedSearch.value) return publishedPosts.value;
  const search = publishedSearch.value.toLowerCase();
  return publishedPosts.value.filter(
    post =>
      post.title.toLowerCase().includes(search) ||
      post.author.toLowerCase().includes(search) ||
      post.excerpt.toLowerCase().includes(search)
  );
});

// 将后端 AdminPostSummaryDto 映射到前端展示用 Post
function mapAdminPostToPost(item: AdminPostSummaryDto): Post {
  console.log('Post Item:', item.postId, 'Create:', item.createTime, 'Update:', item.updateTime);
  const displayTime = formatDateTime(item.updateTime || item.createTime);
  return {
    id: item.postId ?? 0,
    title: item.title ?? '',
    excerpt: item.excerpt ?? '',
    author: (item.authorName ?? '未知作者') as string,
    authorAvatar: item.authorAvatar,
    time: displayTime
  };
}

// 加载待审核帖子列表
async function loadPendingPosts() {
  try {
    loadingPending.value = true;
    const res = await getPendingPosts({
      page: currentPendingPage.value,
      pageSize: PAGE_SIZE,
      keyword: pendingSearch.value || undefined
    });
    if ((res.code === 0 || res.code === 200) && res.data) {
      const list = res.data.list || res.data.records || [];
      pendingPosts.value = list.map(mapAdminPostToPost);
      pendingTotal.value = res.data.total ?? list.length;
      stats.pending = pendingTotal.value;
      stats.total = stats.pending + stats.published;
    } else {
      console.warn('获取待审核帖子列表失败', res);
    }
  } catch (error) {
    console.error('获取待审核帖子列表异常', error);
  } finally {
    loadingPending.value = false;
  }
}

// 加载已发布帖子列表
async function loadPublishedPosts() {
  try {
    loadingPublished.value = true;
    const res = await getPublishedPosts({
      page: currentPublishedPage.value,
      pageSize: PAGE_SIZE,
      keyword: publishedSearch.value || undefined
    });
    if ((res.code === 0 || res.code === 200) && res.data) {
      const list = res.data.list || res.data.records || [];
      publishedPosts.value = list.map(mapAdminPostToPost);
      publishedTotal.value = res.data.total ?? list.length;
      stats.published = publishedTotal.value;
      stats.total = stats.pending + stats.published;
    } else {
      console.warn('获取已发布帖子列表失败', res);
    }
  } catch (error) {
    console.error('获取已发布帖子列表异常', error);
  } finally {
    loadingPublished.value = false;
  }
}

// 弹窗状态
const showPostDetailModal = ref(false);
const showApproveModal = ref(false);
const showRejectModal = ref(false);
const showDeleteSuccessModal = ref(false);
const showConfirmModal = ref(false);
const showRejectReasonModal = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
const confirmAction = ref<'approve' | 'reject' | 'delete' | null>(null);
const selectedPost = ref<Post | null>(null);

function handleApprove(post: Post) {
  selectedPost.value = post;
  confirmAction.value = 'approve';
  showConfirmModal.value = true;
}

function handleReject(post: Post) {
  selectedPost.value = post;
  confirmAction.value = 'reject';
  showConfirmModal.value = true;
}

const selectedPostDetail = ref<any>(null);

async function handleViewDetail(post: Post) {
  try {
    const res = await getPostDetail(post.id);
    if ((res.code === 0 || res.code === 200) && res.data) {
      selectedPost.value = {
        id: res.data.postId ?? post.id,
        title: res.data.title ?? post.title,
        excerpt: res.data.content?.substring(0, 100) ?? post.excerpt,
        author: res.data.authorName ?? post.author,
        authorAvatar: res.data.authorAvatar ?? post.authorAvatar,
        time: formatDateTime(res.data.createTime) || post.time
      };
      selectedPostDetail.value = res.data;
      showPostDetailModal.value = true;
    } else {
      console.warn('获取帖子详情失败', res);
      // 如果获取详情失败，仍然显示基本信息
      selectedPost.value = post;
      selectedPostDetail.value = null;
      showPostDetailModal.value = true;
    }
  } catch (error) {
    console.error('获取帖子详情异常', error);
    // 如果获取详情失败，仍然显示基本信息
  selectedPost.value = post;
  selectedPostDetail.value = null;
  showPostDetailModal.value = true;
  }
}

function handleDelete(post: Post) {
  selectedPost.value = post;
  confirmAction.value = 'delete';
  showConfirmModal.value = true;
}

async function onConfirmModalConfirm() {
  console.log('[Posts] onConfirmModalConfirm 被调用', { selectedPost: selectedPost.value?.id, confirmAction: confirmAction.value });
  if (!selectedPost.value || !confirmAction.value) {
    console.warn('[Posts] onConfirmModalConfirm 缺少必要参数', { selectedPost: selectedPost.value, confirmAction: confirmAction.value });
    return;
  }
  
  showConfirmModal.value = false;
  
  if (confirmAction.value === 'reject') {
    // 如果是拒绝操作，先显示拒绝理由输入弹窗
    console.log('[Posts] 准备显示拒绝理由弹窗');
    showRejectReasonModal.value = true;
    return;
  }
  
  // 其他操作直接执行
  await executeAction();
}

async function executeAction(reason: string = '') {
  console.log('executeAction 被调用', { reason, confirmAction: confirmAction.value, selectedPost: selectedPost.value?.id });
  if (!selectedPost.value || !confirmAction.value) {
    console.warn('executeAction 缺少必要参数', { selectedPost: selectedPost.value, confirmAction: confirmAction.value });
    return;
  }
  
  try {
  if (confirmAction.value === 'approve') {
      // 审核通过
      const res = await approvePost(selectedPost.value.id);
      if (res.code === 0 || res.code === 200) {
    showApproveModal.value = true;
        // 从待审核列表中移除
        if (activeTab.value === 'pending') {
          await loadPendingPosts();
        }
      } else {
        errorMessage.value = res.message || '审核通过失败';
        showErrorModal.value = true;
      }
  } else if (confirmAction.value === 'reject') {
      // 审核拒绝
      console.log('准备发送审核拒绝请求', { postId: selectedPost.value.id, reason });
      const res = await rejectPost(selectedPost.value.id, { reason });
      console.log('审核拒绝请求响应', res);
      if (res.code === 0 || res.code === 200) {
    showRejectModal.value = true;
        // 从待审核列表中移除
        if (activeTab.value === 'pending') {
          await loadPendingPosts();
        }
      } else {
        errorMessage.value = res.message || '审核拒绝失败';
        showErrorModal.value = true;
      }
  } else if (confirmAction.value === 'delete') {
      // 删除帖子
      const res = await deletePostApi(selectedPost.value.id);
      if (res.code === 0 || res.code === 200) {
        showDeleteSuccessModal.value = true;
    // 从列表中移除
    if (activeTab.value === 'pending') {
          await loadPendingPosts();
        } else {
          await loadPublishedPosts();
        }
    } else {
        errorMessage.value = res.message || '删除失败';
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
  console.log('[Posts] onRejectReasonConfirm 被调用', { reason, confirmAction: confirmAction.value, selectedPost: selectedPost.value?.id });
  showRejectReasonModal.value = false;
  executeAction(reason);
}

function onRejectReasonCancel() {
  console.log('[Posts] onRejectReasonCancel 被调用');
  showRejectReasonModal.value = false;
  confirmAction.value = null;
  selectedPost.value = null;
}

function onConfirmModalCancel() {
  showConfirmModal.value = false;
  confirmAction.value = null;
}

function onApproveConfirm() {
  showApproveModal.value = false;
  selectedPost.value = null;
  confirmAction.value = null;
}

function onRejectConfirm() {
  showRejectModal.value = false;
  selectedPost.value = null;
  confirmAction.value = null;
}

function onDeleteConfirm() {
  showDeleteSuccessModal.value = false;
  selectedPost.value = null;
  confirmAction.value = null;
}

onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab as string;
  }

  // 进入页面时，同时加载待审核和已发布帖子，
  // 让顶部统计卡片中的数量可以立即显示真实数据
  loadPendingPosts();
  loadPublishedPosts();
});

// 监听分页变化
watch(currentPendingPage, () => {
  if (activeTab.value === 'pending') {
    loadPendingPosts();
  }
});

watch(currentPublishedPage, () => {
  if (activeTab.value === 'published') {
    loadPublishedPosts();
  }
});

// 监听标签页切换
watch(activeTab, (value) => {
  if (value === 'pending') {
    currentPendingPage.value = 1;
    loadPendingPosts();
  } else if (value === 'published') {
    currentPublishedPage.value = 1;
    loadPublishedPosts();
  }
});

// 监听搜索关键词变化（防抖）
let pendingSearchTimer: number | undefined;
watch(pendingSearch, () => {
  if (pendingSearchTimer) clearTimeout(pendingSearchTimer);
  pendingSearchTimer = window.setTimeout(() => {
    if (activeTab.value === 'pending') {
      currentPendingPage.value = 1;
      loadPendingPosts();
    }
  }, 500);
});

let publishedSearchTimer: number | undefined;
watch(publishedSearch, () => {
  if (publishedSearchTimer) clearTimeout(publishedSearchTimer);
  publishedSearchTimer = window.setTimeout(() => {
    if (activeTab.value === 'published') {
      currentPublishedPage.value = 1;
      loadPublishedPosts();
    }
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

