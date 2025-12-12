<template>
  <div>
    <h2 class="text-2xl font-bold mb-5" style="color: #111;">我发布的帖子</h2>

    <div v-if="posts.length" class="flex flex-col gap-5">
      <div
        v-for="post in posts"
        :key="post.id"
        class="bg-white rounded-xl p-6 shadow-sm flex flex-col border cursor-pointer transition-transform hover:-translate-y-1"
        style="border-color: #F3F4F6;"
        @click="goToDetail(post)"
      >
        <!-- 头部用户信息 & 帖子状态 -->
        <div class="flex items-center justify-between mb-3">
        <div class="flex items-center">
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold text-white mr-2.5 overflow-hidden"
            style="background-color: #FBBF24;"
          >
            <img
              v-if="avatarUrl"
              :src="avatarUrl"
              alt="用户头像"
              class="w-full h-full object-cover"
            />
            <span v-else>{{ userName.charAt(0) }}</span>
          </div>
            <div class="flex items-center gap-2.5 text-sm">
              <span class="font-bold" style="color: #333;">{{ userName }}</span>
              <span class="text-xs" style="color: #9CA3AF;">{{ post.time }}</span>
            </div>
          </div>

          <!-- 审核状态标签 -->
          <div v-if="post.reviewStatus" class="ml-4">
            <span
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold"
              :class="getReviewStatusClass(post.reviewStatus)"
            >
              审核状态：{{ post.reviewStatus }}
            </span>
          </div>
        </div>

        <!-- 标题 -->
        <div class="text-lg font-bold mb-2.5 leading-snug" style="color: #E67E22;">
          {{ post.title }}
        </div>

        <!-- 正文 -->
        <div class="text-sm leading-relaxed mb-4" style="color: #333;">
          {{ post.content }}
        </div>

        <!-- 图片/视频区域 -->
        <div v-if="post.images && post.images.length" class="grid grid-cols-3 gap-2.5 mb-4">
          <div
            v-for="(img, index) in post.images.slice(0, 3)"
            :key="index"
            class="relative w-full aspect-[4/3] bg-slate-100 rounded-lg overflow-hidden border border-slate-200"
          >
            <template v-if="typeof img === 'string' && (img.startsWith('http') || img.startsWith('/'))">
              <video
                v-if="isVideoUrl(img)"
                :src="img"
                controls
                preload="metadata"
                class="w-full h-full object-cover"
              ></video>
              <img
                v-else
                :src="img"
                :alt="`帖子图片 ${index + 1}`"
                class="w-full h-full object-cover hover:scale-105 transition-transform duration-500"
                @error="handleImageError"
              />
            </template>
            <span v-else class="flex items-center justify-center w-full h-full text-xs text-gray-400">{{ img }}</span>
          </div>
        </div>

        <!-- 底部数据栏和操作按钮 -->
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-6 text-xs" style="color: #6B7280;">
            <div class="flex items-center gap-1.5 cursor-pointer hover:text-[#FF8C00]">
              <i class="fa-solid fa-heart text-sm"></i>
              {{ post.likes }}
            </div>
            <div class="flex items-center gap-1.5 cursor-pointer hover:text-[#FF8C00]">
              <i class="fa-solid fa-comment text-sm"></i>
              {{ post.comments }}
            </div>
            <div class="flex items-center gap-1.5 cursor-pointer hover:text-[#FF8C00]">
              <i class="fa-solid fa-eye text-sm"></i>
              {{ post.views }}
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button
              class="px-4 py-1.5 text-sm font-medium rounded-lg cursor-pointer transition-all hover:opacity-90"
              style="background-color: #F59E0B; color: white;"
              @click.stop="handleEdit(post)"
            >
              <i class="fa-regular fa-pen-to-square"></i> 编辑
            </button>
            <button
              class="px-4 py-1.5 text-sm font-medium rounded-lg cursor-pointer transition-all hover:opacity-90"
              style="background-color: #EF4444; color: white;"
              @click.stop="handleDelete(post)"
            >
              <i class="fa-solid fa-trash"></i> 删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="mt-8 text-center text-sm" style="color: #9CA3AF;">
      当前还没有发布帖子
    </div>

    <!-- 分页：统一样式，列表为空也显示，至少一页 -->
    <div class="flex justify-center mt-10 mb-5">
      <div class="flex items-center gap-2.5">
        <button 
          class="w-10 h-10 rounded-lg border border-gray-300 bg-white text-sm cursor-pointer flex items-center justify-center transition-all hover:border-[#FF8C00] hover:text-[#FF8C00]"
          style="color: #6B7280;"
          :disabled="currentPage === 1"
          :class="currentPage === 1 ? 'cursor-not-allowed opacity-50' : ''"
          @click="goPrev"
        >
          <i class="fa-solid fa-chevron-left"></i>
        </button>
        <button
          v-for="(page, index) in getDisplayedPages(currentPage, totalPages)"
          :key="index"
          class="w-10 h-10 rounded-lg border border-gray-300 bg-white text-sm flex items-center justify-center transition-all"
          :class="[
            page === currentPage ? 'bg-[#FF8C00] text-white border-[#FF8C00] active-page' : 'text-gray-600',
            typeof page === 'string' ? 'cursor-default border-transparent' : 'cursor-pointer hover:border-[#FF8C00] hover:text-[#FF8C00]'
          ]"
          style="color: #6B7280;"
          @click="typeof page === 'number' && goPage(page)"
        >
          {{ page }}
        </button>
        <button
          class="w-10 h-10 rounded-lg border border-gray-300 bg-white text-sm cursor-pointer flex items-center justify-center transition-all hover:border-[#FF8C00] hover:text-[#FF8C00]"
          style="color: #6B7280;"
          :disabled="currentPage === totalPages"
          :class="currentPage === totalPages ? 'cursor-not-allowed opacity-50' : ''"
          @click="goNext"
        >
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <ConfirmModal
      :visible="showDeleteConfirmModal"
      title="确认删除"
      message="确定要删除这条帖子吗？删除后无法恢复。"
      @confirm="confirmDelete"
      @cancel="closeDeleteConfirm"
    />

    <!-- 删除成功弹窗 -->
    <SuccessModal
      :visible="showDeleteSuccessModal"
      :title="deleteResult.title"
      :message="deleteResult.message"
      @close="closeDeleteSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import ConfirmModal from '../../../components/common/ConfirmModal.vue';
import SuccessModal from '../../../components/common/SuccessModal.vue';
import { getMyPostList, deletePost } from '@/api/postApi';
import { getCurrentUser, type CurrentUserInfo } from '@/api/userApi';
import { isVideoUrl } from '@/utils/mediaUtils';

const router = useRouter();
const route = useRoute();

interface Post {
  id: number;
  title: string;
  content: string;
  time: string;
  images: string[];
  likes: number;
  comments: number;
  views: number;
   // 审核状态：待审核 / 通过 / 拒绝
  reviewStatus?: string;
}

const userName = ref('用户');
const avatarUrl = ref<string | null>(null);
const userId = ref<number | undefined>(undefined);

const posts = ref<Post[]>([]);

const currentPage = ref(1);
// 每页 3 条帖子卡片
const pageSize = 3;
const total = ref(0);
const totalPages = computed(() => Math.max(1, Math.ceil((total.value || 0) / pageSize)));

const showDeleteConfirmModal = ref(false);
const showDeleteSuccessModal = ref(false);
const postToDelete = ref<Post | null>(null);
const deleteResult = reactive({
  title: '删除成功',
  message: '帖子已成功删除。'
});

function handleEdit(post: Post) {
  // 跳转到发帖页面的编辑模式，同时传递基础信息作为备用（如果后端返回data为空）
  router.push({
    name: 'PostNew',
    query: {
      id: post.id.toString(),
      mode: 'edit',
      from: 'myPosts',
      title: post.title,
      content: post.content,
      images: JSON.stringify(post.images || []),
      likes: post.likes,
      comments: post.comments,
      views: post.views,
      reviewStatus: post.reviewStatus || '',
      time: post.time
    }
  });
}

function handleDelete(post: Post) {
  postToDelete.value = post;
  showDeleteConfirmModal.value = true;
}

async function confirmDelete() {
  if (!postToDelete.value) {
    showDeleteConfirmModal.value = false;
    return;
  }

  try {
    const id = postToDelete.value.id;
    console.log('开始删除帖子，ID:', id);

    // 使用删除帖子的接口 DELETE /api/post/{id}
    // 后端需要允许帖子作者删除自己的帖子（不需要管理员权限）
    const res = await deletePost(id);
    console.log('删除帖子响应:', res);

    if (res.code === 200 || res.code === 0) {
      // 本地列表中移除
      const index = posts.value.findIndex(p => p.id === id);
      if (index > -1) {
        posts.value.splice(index, 1);
        // 如果删除后当前页没有数据了，且不是第一页，则跳转到上一页
        if (posts.value.length === 0 && currentPage.value > 1) {
          goPage(currentPage.value - 1);
        } else if (posts.value.length === 0) {
          // 如果第一页也没有数据了，重新加载
          loadPosts(currentPage.value);
        }
      }
      deleteResult.title = '删除成功';
      deleteResult.message = '帖子已成功删除。';
    } else {
      console.error('删除帖子失败', res);
      deleteResult.title = '删除失败';
      deleteResult.message = res.message || '删除帖子失败，请稍后重试。';
    }
  } catch (err: any) {
    console.error('========== 删除帖子错误详情 ==========');
    console.error('错误消息:', err?.message);
    console.error('HTTP状态码:', err?.status);
    console.error('请求URL:', err?.url);
    console.error('响应数据:', err?.responseData);
    console.error('完整错误对象:', err);
    console.error('=====================================');

    // 尝试从错误对象中提取更详细的错误信息
    let errorMsg = err?.message || '删除帖子接口异常，请稍后重试。';

    // 如果有响应数据，尝试提取更详细的错误信息
    if (err?.responseData) {
      const responseData = err.responseData;
      if (typeof responseData === 'object') {
        const backendMsg = responseData.message || responseData.msg || responseData.error || responseData.details;
        if (backendMsg) {
          errorMsg = backendMsg;
        }
      }
    }

    // 根据错误消息提供更友好的提示
    if (errorMsg.includes('无此权限：post:delete') || errorMsg.includes('post:delete')) {
      errorMsg = `权限错误：无此权限：post:delete\n\n问题说明：\n后端要求用户具有 'post:delete' 权限才能删除帖子，但这是不合理的。\n\n正确的逻辑应该是：\n✅ 帖子作者可以删除自己的帖子（不需要特殊权限）\n✅ 管理员可以删除任何帖子\n\n请后端开发人员修改权限检查逻辑：\n1. 检查当前用户是否是帖子作者\n2. 如果是作者，允许删除\n3. 如果是管理员，也允许删除\n4. 否则才返回权限错误`;
    } else if (errorMsg.includes('权限') || errorMsg.includes('permission') || errorMsg.includes('无此权限')) {
      errorMsg = `权限错误：${errorMsg}\n\n提示：后端需要修改权限逻辑，允许帖子作者删除自己的帖子。`;
    } else if (errorMsg.includes('401') || errorMsg.includes('未登录')) {
      errorMsg = '请先登录后再试（401）。';
    } else if (errorMsg.includes('403') || errorMsg.includes('禁止')) {
      errorMsg = '您没有权限删除此帖子（403）。';
    }

    // 根据HTTP状态码提供额外提示
    const status = err?.status;
    if (status === 500 && errorMsg.includes('权限')) {
      errorMsg += '\n\n注意：后端返回500错误，但错误消息是权限问题。这可能是后端权限检查逻辑有问题。';
    }

    deleteResult.title = '删除失败';
    deleteResult.message = errorMsg;
  } finally {
    showDeleteConfirmModal.value = false;
    showDeleteSuccessModal.value = true;
    postToDelete.value = null;
  }
}

function closeDeleteConfirm() {
  showDeleteConfirmModal.value = false;
  postToDelete.value = null;
}

function closeDeleteSuccess() {
  showDeleteSuccessModal.value = false;
}

// 从「我的帖子」跳转到帖子详情，同时把基础信息通过 query 传过去，便于在详情页回显（例如待审核时后端不返回详情）
function goToDetail(post: Post) {
  // 记录当前页，便于详情返回后恢复
  sessionStorage.setItem('myPostsLastPage', currentPage.value.toString());
  router.push({
    name: 'PostDetail',
    params: { id: post.id.toString() },
    query: {
      from: 'myPosts',
      fromMyPostsPage: currentPage.value.toString(),
      title: post.title,
      content: post.content,
      time: post.time,
      images: JSON.stringify(post.images || []),
      reviewStatus: post.reviewStatus || '',
      likes: post.likes,
      comments: post.comments,
      views: post.views,
      author: userName.value,
      avatarUrl: avatarUrl.value || '',
      userId: userId.value?.toString() || ''
    }
  });
}

async function loadPosts(page = 1) {
  try {
    const res = await getMyPostList({ page, pageSize });
    console.log('我的帖子列表API返回:', res);

    if (res.code === 200 && res.data) {
      total.value = res.data.total ?? 0;
      const records = res.data.records ?? [];
      console.log('帖子记录:', records);

      posts.value = records.map((item: any, index: number) => {
        const id = item.postId ?? item.id ?? index + 1;
        const title = item.title || '';
        const content = item.content || '';
        const time = item.createTime || '';

        // 解析图片：优先 images，其次 mediaUrls（可能是数组或字符串）
        let images: string[] = [];
        if (Array.isArray(item.images)) {
          images = item.images;
        } else if (Array.isArray(item.mediaUrls)) {
          images = item.mediaUrls;
        } else if (typeof item.mediaUrls === 'string' && item.mediaUrls) {
          try {
            const parsed = JSON.parse(item.mediaUrls);
            if (Array.isArray(parsed)) {
              images = parsed;
            } else {
              images = [item.mediaUrls];
            }
          } catch {
            images = [item.mediaUrls];
          }
        }

        console.log(`帖子 ${id} (${title}) 的图片:`, images);

        return {
          id,
          title,
          content,
          time,
          images,
          likes: item.likeCount ?? 0,
          comments: item.commentCount ?? 0,
          views: item.viewCount ?? 0,
          reviewStatus: item.reviewStatus || '',
        } as Post;
      });

      console.log('处理后的帖子列表:', posts.value);
    } else {
      console.error('获取我的帖子列表失败', res);
    }
  } catch (err) {
    console.error('获取我的帖子列表接口异常', err);
  }
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  if (target) {
    target.style.display = 'none';
  }
};

// 加载当前登录用户信息（昵称和头像），用于展示“我”的帖子列表头部信息
function loadUserFromCache() {
  try {
    const cached = localStorage.getItem('currentUser');
    if (cached) {
      const data = JSON.parse(cached) as CurrentUserInfo;
      if (data.userName) userName.value = data.userName;
      if (data.avatarUrl) avatarUrl.value = data.avatarUrl;
      if (data.userId) userId.value = data.userId;
    }
  } catch (e) {
    console.error('解析本地缓存用户信息失败(MyPosts)', e);
  }
}

async function loadUserFromApi() {
  try {
    const res = await getCurrentUser();
    if ((res.code === 0 || res.code === 200) && res.data) {
      const data = res.data;
      if (data.userName) userName.value = data.userName;
    if (data.avatarUrl) avatarUrl.value = data.avatarUrl;
    if (data.userId) userId.value = data.userId;
    // 更新本地缓存，供其他页面复用
    localStorage.setItem('currentUser', JSON.stringify(data));
  }
} catch (e) {
  console.error('获取当前用户信息失败(MyPosts)', e);
}
}

function goPage(page: number) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  loadPosts(page);
  // 同步路由，便于返回时恢复高亮页码
  router.push({ path: '/user-center', query: { menu: 'posts', page: page.toString() } });
}

function goPrev() {
  if (currentPage.value > 1) {
    goPage(currentPage.value - 1);
  }
}

function goNext() {
  if (currentPage.value < totalPages.value) {
    goPage(currentPage.value + 1);
  }
}

onMounted(() => {
  // 初始化页码：优先路由参数，其次缓存
  const pageFromRoute = route.query.page;
  const pageFromCache = sessionStorage.getItem('myPostsLastPage');
  if (typeof pageFromRoute === 'string' && !isNaN(parseInt(pageFromRoute, 10))) {
    currentPage.value = Math.max(1, parseInt(pageFromRoute, 10));
  } else if (pageFromCache && !isNaN(parseInt(pageFromCache, 10))) {
    currentPage.value = Math.max(1, parseInt(pageFromCache, 10));
  }
  loadUserFromCache();
  loadUserFromApi();
  loadPosts(currentPage.value);
});

watch(
  () => route.query.page,
  (newPage) => {
    if (typeof newPage === 'string') {
      const num = parseInt(newPage, 10);
      if (!isNaN(num) && num > 0 && num !== currentPage.value) {
        currentPage.value = num;
        loadPosts(num);
      }
    }
  }
);

function getReviewStatusClass(status: string) {
  if (status === '通过') {
    return 'bg-green-50 text-green-600';
  }
  if (status === '拒绝') {
    return 'bg-red-50 text-red-500';
  }
  // 默认：待审核 或 其它未知状态
  return 'bg-yellow-50 text-yellow-600';
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
</script>

<style scoped>
.active-page {
  outline: 2px solid #FF8C00 !important;
  box-shadow: 0 0 0 2px #FF8C00 !important;
}
</style>
