<template>
  <div class="comment-root flex gap-4 py-4 border-b border-gray-100">
    <!-- Left: Avatar -->
    <div class="flex-shrink-0">
      <div class="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center text-gray-500 overflow-hidden">
        <img v-if="comment.authorAvatar" :src="comment.authorAvatar" class="w-full h-full object-cover" />
        <span v-else class="text-lg font-bold">{{ comment.authorName?.[0] || 'User' }}</span>
      </div>
    </div>

    <!-- Right: Content -->
    <div class="flex-grow">
      <!-- Author Name -->
      <div class="text-sm font-bold text-gray-700 mb-1">{{ comment.authorName }}</div>
      
      <!-- Content -->
      <div class="text-base text-gray-800 leading-relaxed mb-2">{{ comment.content }}</div>
      
      <!-- Footer: Time, Like, Reply -->
      <div class="flex items-center gap-6 text-sm text-gray-400 mb-3">
        <span>{{ comment.date }}</span>
        <button 
          v-if="!readonly"
          class="flex items-center gap-1 hover:text-orange-500 transition-colors" 
          :class="{ 'text-orange-500': comment.isLiked }"
          @click="$emit('like', comment.id)"
        >
          <i class="fa-regular fa-thumbs-up" v-if="!comment.isLiked"></i>
          <i class="fa-solid fa-thumbs-up" v-else></i>
          <span>{{ comment.likes || 0 }}</span>
        </button>
        <button v-if="!readonly" class="hover:text-blue-500 transition-colors" @click="toggleReply(comment)">
          回复
        </button>
      </div>

      <!-- Reply Input (for Root) -->
      <div v-if="replyingTo?.id === comment.id" class="mb-4 fade-in">
        <div class="flex gap-2">
          <textarea 
            v-model="replyContent"
            class="flex-grow p-2 border border-gray-300 rounded-lg focus:border-orange-500 focus:ring-1 focus:ring-orange-500 outline-none resize-none text-sm"
            rows="3"
            :placeholder="`回复 @${comment.authorName}:`"
          ></textarea>
          <button 
            class="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 self-end text-sm"
            @click="submitReply(comment.id)"
            :disabled="!replyContent.trim()"
          >
            发布
          </button>
        </div>
      </div>

      <!-- Child Comments (Sub-comments) -->
      <div v-if="hasReplies || (replies && replies.length > 0)" class="bg-gray-50 rounded-lg p-4 space-y-4">
        
        <!-- Children List -->
        <div v-if="isExpanded && replies.length > 0" class="space-y-4">
          <div v-for="child in replies" :key="child.id" class="flex gap-3">
            <!-- Child Avatar -->
            <div class="flex-shrink-0">
               <div class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-gray-500 overflow-hidden">
                  <img v-if="child.authorAvatar" :src="child.authorAvatar" class="w-full h-full object-cover" />
                  <span v-else class="text-xs font-bold">{{ child.authorName?.[0] || 'U' }}</span>
               </div>
            </div>
            
            <!-- Child Content -->
            <div class="flex-grow">
              <div class="text-sm">
                <span class="font-bold text-gray-700">{{ child.authorName }}</span>
                <span v-if="child.replyTo" class="text-gray-500 mx-1"> 回复 </span>
                <span v-if="child.replyTo" class="text-blue-500">{{ child.replyTo }}</span>
                <span class="text-gray-800 ml-1">：{{ child.content }}</span>
              </div>
              
              <!-- Child Footer -->
              <div class="flex items-center gap-4 text-xs text-gray-400 mt-1">
                <span>{{ child.date }}</span>
                <button 
                  v-if="!readonly"
                  class="flex items-center gap-1 hover:text-orange-500 transition-colors"
                  :class="{ 'text-orange-500': child.isLiked }"
                  @click="handleChildLike(child)"
                >
                  <i class="fa-regular fa-thumbs-up" v-if="!child.isLiked"></i>
                  <i class="fa-solid fa-thumbs-up" v-else></i>
                  <span>{{ child.likes || 0 }}</span>
                </button>
                <button v-if="!readonly" class="hover:text-blue-500 transition-colors" @click="toggleReply(child)">
                  回复
                </button>
              </div>

              <!-- Reply Input (for Child) -->
              <div v-if="replyingTo?.id === child.id" class="mt-2 fade-in">
                <div class="flex gap-2">
                  <textarea 
                    v-model="replyContent"
                    class="flex-grow p-2 border border-gray-300 rounded-lg focus:border-orange-500 focus:ring-1 focus:ring-orange-500 outline-none resize-none text-xs"
                    rows="2"
                    :placeholder="`回复 @${child.authorName}:`"
                  ></textarea>
                  <button 
                    class="px-3 py-1 bg-orange-500 text-white rounded-lg hover:bg-orange-600 self-end text-xs"
                    @click="submitReply(child.id, child)"
                    :disabled="!replyContent.trim()"
                  >
                    发布
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination Controls -->
        <div v-if="isExpanded && repliesTotal > repliesPageSize" class="flex items-center gap-2 text-xs text-gray-500 mt-2">
             <span class="mr-auto">共 {{ repliesTotal }} 条回复</span>
             <button 
               :disabled="repliesPage === 1 || repliesLoading"
               @click="changeReplyPage(repliesPage - 1)"
               class="px-2 py-1 border rounded hover:bg-white disabled:opacity-50"
             >上一页</button>
             <span>{{ repliesPage }} / {{ Math.ceil(repliesTotal / repliesPageSize) }}</span>
             <button 
               :disabled="repliesPage * repliesPageSize >= repliesTotal || repliesLoading"
               @click="changeReplyPage(repliesPage + 1)"
               class="px-2 py-1 border rounded hover:bg-white disabled:opacity-50"
             >下一页</button>
        </div>
        
        <!-- Loading State -->
        <div v-if="repliesLoading" class="text-center py-2 text-gray-500 text-xs">
          加载中...
        </div>

        <!-- Expand/Collapse Controls -->
        <div class="flex items-center gap-4 text-sm text-blue-500 font-medium">
          <button 
            v-if="!isExpanded && hasReplies"
            @click="handleExpandClick"
            class="hover:text-blue-600 flex items-center gap-1"
          >
            <span>展开 {{ repliesTotal || comment.replyCount || '更多' }} 条回复</span>
            <i class="fa-solid fa-chevron-down"></i>
          </button>
          <button
            v-if="isExpanded"
            @click="collapseReplies"
            class="hover:text-blue-600 flex items-center gap-1"
          >
            <span>收起回复</span>
            <i class="fa-solid fa-chevron-up"></i>
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { getCommentReplies, likeComment as likeCommentApi, submitComment as submitCommentApi, type Comment } from '@/api/commentapi';
import type { CurrentUserInfo } from '@/api/userApi';
import { normalizeComments } from '@/utils/commentUtils';

const props = defineProps<{
  comment: Comment;
  readonly?: boolean;
  postId?: number;
  currentUser?: CurrentUserInfo | null;
}>();

const emit = defineEmits<{
  (e: 'like', id: number): void;
  (e: 'update-count', delta: number): void;
}>();

// 子评论相关状态
const replies = ref<Comment[]>([]);
const isExpanded = ref(false);
const repliesLoading = ref(false);
const repliesPage = ref(1);
const repliesPageSize = ref(5);
const repliesTotal = ref(0);

// 初始化：如果有预加载的子评论，直接使用
onMounted(() => {
  if (props.comment.children && props.comment.children.length > 0) {
    replies.value = props.comment.children;
    // 如果没有 replyCount，尝试用 children.length
    repliesTotal.value = props.comment.replyCount || props.comment.children.length;
  } else {
    repliesTotal.value = props.comment.replyCount || 0;
  }
});

// 监听 props 变化，更新总数（例如点赞或新回复导致数据刷新）
watch(() => props.comment, (newVal) => {
    if (newVal.replyCount !== undefined) {
        repliesTotal.value = newVal.replyCount;
    }
}, { deep: true });

const hasReplies = computed(() => {
  return repliesTotal.value > 0 || (props.comment.children && props.comment.children.length > 0);
});

// 加载子评论
const loadReplies = async (page: number) => {
  repliesLoading.value = true;
  try {
    const res = await getCommentReplies(props.comment.id, {
      page: page,
      pageSize: repliesPageSize.value,
      sortBy: 'TIME',
      order: 'DESC'
    });
    
    // 处理返回数据
    let rawList: any[] = [];
    const d: any = res.data;
    
    // 尝试提取总数
    if (d && d.total !== undefined) {
      repliesTotal.value = Number(d.total);
    } else if ((res as any).total !== undefined) {
      repliesTotal.value = Number((res as any).total);
    }

    if (Array.isArray(d)) {
      rawList = d;
    } else if (d && Array.isArray(d.data)) {
      rawList = d.data;
    } else if (d && Array.isArray(d.list)) {
      rawList = d.list;
    } else if (d && Array.isArray(d.records)) {
      rawList = d.records;
    }
    
    replies.value = normalizeComments(rawList);
    repliesPage.value = page;

  } catch (error) {
    console.error('加载子评论失败', error);
  } finally {
    repliesLoading.value = false;
  }
};

const handleExpandClick = () => {
  isExpanded.value = true;
  // Bilibili 逻辑：点击展开，如果当前没有数据或者数据很少，加载第一页
  if (replies.value.length === 0 || (replies.value.length < repliesTotal.value && replies.value.length < repliesPageSize.value)) {
      loadReplies(1);
  }
};

const collapseReplies = () => {
  isExpanded.value = false;
};

const changeReplyPage = (page: number) => {
    loadReplies(page);
};

// 回复相关
const replyingTo = ref<Comment | null>(null);
const replyContent = ref('');

const toggleReply = (target: Comment) => {
  if (replyingTo.value?.id === target.id) {
    replyingTo.value = null;
    replyContent.value = '';
  } else {
    replyingTo.value = target;
    replyContent.value = '';
  }
};

// 子评论点赞
const handleChildLike = async (child: Comment) => {
  // 乐观更新
  const prevLiked = child.isLiked;
  const prevLikes = child.likes;
  child.isLiked = !child.isLiked;
  child.likes = child.isLiked ? child.likes + 1 : Math.max(0, child.likes - 1);
  
  try {
    await likeCommentApi(child.id);
  } catch (e) {
    // 回滚
    child.isLiked = prevLiked;
    child.likes = prevLikes;
  }
};

// 提交回复
const submitReply = async (parentId: number, target?: Comment) => {
  if (!replyContent.value.trim() || !props.postId) return;
  
  const content = replyContent.value;
  const replyToUser = target?.authorName;
  
  // 清理输入框
  replyContent.value = '';
  replyingTo.value = null;
  isExpanded.value = true;

  try {
      await submitCommentApi(props.postId, { 
        content, 
        parentId, 
        replyTo: replyToUser,
        rootId: props.comment.id // 始终传递当前组件对应的根评论ID
      });
      
      // 提交成功后，重新加载子评论列表（刷新第一页，确保看到最新回复）
      await loadReplies(1);
      
      // 通知父组件增加总评论数（因为我们不知道具体加了多少，但通常是+1）
      // 注意：loadReplies 会更新 repliesTotal，但父组件的 totalComments 可能需要额外信号
      emit('update-count', 1);

  } catch (e) {
      console.error('回复失败', e);
      alert('回复失败，请稍后重试');
  }
};

</script>

<style scoped>
.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>