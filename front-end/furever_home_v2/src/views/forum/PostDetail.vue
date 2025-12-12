<template>
  <div class="post-detail-page" ref="containerRef">
    <main class="forum-main">
      <!-- 返回按钮 -->
      <button 
        @click="router.back()" 
        class="mb-4 flex items-center gap-2 text-[#FF8C00] hover:text-[#e6722a] transition-colors"
        title="返回"
      >
        <i class="fa-solid fa-arrow-left text-lg"></i>
        <span class="font-medium">返回</span>
      </button>

      <!-- 帖子详情 -->
      <div v-if="post" class="post-detail">
        <div class="post-header">
          <div class="post-meta">
            <div class="post-author" @click.stop="goToUserProfile(post.userId)" style="cursor: pointer;">
              <div class="author-avatar">
                <img
                  v-if="post.avatarUrl"
                  :src="post.avatarUrl"
                  alt="用户头像"
                  @error="handleAvatarError"
                />
                <span v-else>{{ post.avatarInitial || post.author?.[0] || '用' }}</span>
              </div>
              <span>{{ post.author }}</span>
            </div>
            <span>{{ post.timeAgo || post.publishDate }}</span>
          </div>
          <div class="post-title">{{ post.title }}</div>
        </div>

        <div class="post-content">
          <p v-for="(paragraph, index) in postContentParagraphs" :key="index">
            {{ paragraph }}
          </p>
        </div>

        <div v-if="post.images && post.images.length" class="grid grid-cols-3 gap-2 mt-4">
          <div
            v-for="(media, index) in post.images"
            :key="index"
            class="relative w-full aspect-[4/3] bg-slate-100 rounded-lg overflow-hidden border border-slate-200"
          >
            <img 
              v-if="typeof media === 'string' && (media.startsWith('http') || media.startsWith('/')) && !isVideoUrl(media)"
              :src="media" 
              :alt="`帖子图片 ${index + 1}`"
              class="w-full h-full object-cover hover:scale-105 transition-transform duration-500"
              @error="handleImageError"
              @click="openImagePreview(index)"
            />
            <video
              v-else-if="typeof media === 'string' && (media.startsWith('http') || media.startsWith('/')) && isVideoUrl(media)"
              :src="media"
              controls
              preload="metadata"
              class="w-full h-full object-cover"
              @play="onVideoPlay($event)"
            ></video>
            <span v-else class="flex items-center justify-center w-full h-full text-xs text-gray-400">{{ media }}</span>
          </div>
        </div>

        <!-- 图片预览大图 -->
        <div v-if="showImagePreview" class="image-preview-mask" @click.self="closeImagePreview">
          <button class="nav-btn left" @click.stop="prevImage"><i class="fa-solid fa-chevron-left"></i></button>
          <div class="image-wrapper">
            <img :src="currentPreviewImage" alt="预览" @error="handlePreviewImageError" />
          </div>
          <button class="nav-btn right" @click.stop="nextImage"><i class="fa-solid fa-chevron-right"></i></button>
          <button class="close-btn" @click="closeImagePreview"><i class="fa-solid fa-xmark"></i></button>
        </div>

        <div class="post-stats">
          <div
            class="stat-item"
            :class="{ liked: isLiked }"
            @click="toggleLike"
          >
            <i class="fa-solid fa-thumbs-up"></i> {{ post.likes || 0 }}
          </div>
          <div class="stat-item">
            <i class="fa-solid fa-comment"></i> {{ post.comments || 0 }}
          </div>
          <div class="stat-item">
            <i class="fa-solid fa-eye"></i> {{ post.views || 0 }}
          </div>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="comments-section" v-if="post">
        <h2 class="section-title">评论 ({{ post.comments || 0 }})</h2>

        <div class="comment-form">
          <textarea
            v-model="newComment"
            class="comment-input"
            placeholder="写下你的评论..."
            :disabled="!canComment"
          ></textarea>
          <button
            class="comment-submit"
            @click="handleRootSubmit"
            :disabled="!newComment.trim()"
          >
            发表评论
          </button>
          <p v-if="!canComment" class="comment-hint">
            帖子不存在或未发布，暂无法评论。
          </p>
        </div>

        <div class="comments-list space-y-4">
          <div v-if="comments.length === 0" class="no-comments py-8 text-center text-gray-500">
             暂无评论，快来抢沙发吧！
          </div>
          <CommentItem 
            v-for="c in comments" 
            :key="c.id" 
            :comment="c"
            :postId="post?.id"
            :currentUser="currentUser"
            @like="handleLikeComment"
            @update-count="handleUpdateCount"
          />
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination-controls flex justify-center items-center gap-2 mt-6">
          <button 
            @click="changePage(currentPage - 1)" 
            :disabled="currentPage === 1"
            class="px-4 py-2 bg-white border border-gray-300 rounded-md hover:border-[#FF8C42] hover:text-[#FF8C42] disabled:opacity-50 disabled:cursor-not-allowed transition-all"
          >
            上一页
          </button>
          
          <button
            v-for="(page, index) in getDisplayedPages(currentPage, totalPages)"
            :key="index"
            :class="[
              'px-4 py-2 border rounded-md transition-all',
              page === currentPage
                ? 'bg-[#FF8C42] text-white border-[#FF8C42]'
                : 'border-gray-300 bg-white text-gray-600',
              typeof page === 'string'
                ? 'cursor-default border-transparent'
                : 'cursor-pointer hover:border-[#FF8C42] hover:text-[#FF8C42]'
            ]"
            @click="typeof page === 'number' && changePage(page)"
          >
            {{ page }}
          </button>

          <button 
            @click="changePage(currentPage + 1)" 
            :disabled="currentPage === totalPages"
            class="px-4 py-2 bg-white border border-gray-300 rounded-md hover:border-[#FF8C42] hover:text-[#FF8C42] disabled:opacity-50 disabled:cursor-not-allowed transition-all"
          >
            下一页
          </button>
        </div>
      </div>
    </main>

    <!-- 错误提示模态框 -->
    <div v-if="showErrorModal" class="modal" @click.self="showErrorModal = false">
      <div class="modal-content">
        <div class="modal-icon" style="color: #D9534F;">❌</div>
        <h2>操作失败</h2>
        <p>{{ errorMessage }}</p>
        <div class="modal-buttons">
          <button class="btn btn-primary" @click="showErrorModal = false; errorMessage = ''">
            确定
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, onDeactivated } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getPostDetail, type 帖子详情DTO } from '@/api/postApi';
import {
  getPostComments,
  submitComment as submitCommentApi,
  likePost as likePostApi,
  likeComment as likeCommentApi,
  incrementPostViews,
  type Comment
} from '@/api/commentapi';
import { getCurrentUser, getUserInfo, type CurrentUserInfo } from '@/api/userApi';
import { isVideoUrl } from '@/utils/mediaUtils';
import { formatDateTime } from '@/utils/format';
import { normalizeComments } from '@/utils/commentUtils';
import CommentItem from '@/components/forum/CommentItem.vue';

const route = useRoute();
const router = useRouter();

const containerRef = ref<HTMLElement | null>(null);

const stopAllVideos = (excludeVideo?: HTMLVideoElement) => {
  if (!containerRef.value) return;
  const videos = containerRef.value.querySelectorAll('video');
  videos.forEach((video) => {
    if (video !== excludeVideo && !video.paused) {
      video.pause();
    }
  });
};

const onVideoPlay = (event: Event) => {
  const target = event.target as HTMLVideoElement;
  stopAllVideos(target);
};

// 图片预览状态
const showImagePreview = ref(false);
const previewImages = ref<string[]>([]);
const currentPreviewIndex = ref(0);

const refreshPreviewList = () => {
  if (!post.value || !post.value.images) {
    previewImages.value = [];
    return;
  }
  previewImages.value = post.value.images.filter(
    (media) => typeof media === 'string' && (media.startsWith('http') || media.startsWith('/')) && !isVideoUrl(media)
  );
};

const currentPreviewImage = computed(() => previewImages.value[currentPreviewIndex.value] || '');

const openImagePreview = (index: number) => {
  refreshPreviewList();
  if (!previewImages.value.length) return;
  const media = post.value?.images?.[index];
  const targetIndex = media ? previewImages.value.indexOf(media as string) : -1;
  currentPreviewIndex.value = targetIndex >= 0 ? targetIndex : Math.min(index, previewImages.value.length - 1);
  showImagePreview.value = true;
  document.addEventListener('keydown', handleKeyDown);
};

const closeImagePreview = () => {
  showImagePreview.value = false;
  document.removeEventListener('keydown', handleKeyDown);
};

const prevImage = () => {
  if (!previewImages.value.length) return;
  currentPreviewIndex.value = (currentPreviewIndex.value - 1 + previewImages.value.length) % previewImages.value.length;
};

const nextImage = () => {
  if (!previewImages.value.length) return;
  currentPreviewIndex.value = (currentPreviewIndex.value + 1) % previewImages.value.length;
};

const handleKeyDown = (e: KeyboardEvent) => {
  if (!showImagePreview.value) return;
  if (e.key === 'Escape') closeImagePreview();
  if (e.key === 'ArrowLeft') prevImage();
  if (e.key === 'ArrowRight') nextImage();
};

const handlePreviewImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  if (target) {
    target.alt = '图片加载失败';
  }
};

onBeforeUnmount(() => {
  stopAllVideos();
  document.removeEventListener('keydown', handleKeyDown);
});

onDeactivated(() => {
  stopAllVideos();
});

interface PostDetailData {
  id: number;
  userId?: number;
  title: string;
  author: string;
  avatarInitial?: string;
  avatarUrl?: string;
  timeAgo?: string;
  publishDate?: string;
  content: string;
  fullContent?: string[]; // 完整内容（多段文字）
  images: string[];
  likes: number;
  comments: number;
  views: number | string;
}

const post = ref<PostDetailData | undefined>(undefined);
// 是否为前端示例帖子（如"李同学""王医生"），示例帖子在后端并不存在
const isMockPost = ref(false);
const comments = ref<Comment[]>([]);
const loadingComments = ref(false);
const totalComments = ref(0);
const currentPage = ref(1);
const pageSize = ref(5);

const totalPages = computed(() => Math.ceil(totalComments.value / pageSize.value));

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

const changePage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    if (post.value) {
      loadComments(post.value.id);
    }
  }
};

const newComment = ref('');
const isLiked = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
// 审核提示（例如：尚未通过审核，仅自己可见）
const reviewNotice = ref('');
const canComment = computed(() => !!post.value && !isMockPost.value && !reviewNotice.value);
// 当前登录用户信息
const currentUser = ref<CurrentUserInfo | null>(null);
const replyTargetId = ref<number | null>(null);
const replyContent = ref('');
// 用户信息缓存，减少重复请求
const userNameCache = new Map<number, string>();
const userAvatarCache = new Map<number, string | undefined>();
const pendingUserFetch = new Map<number, Promise<void>>();

// 示例帖子数据（仅用于演示 id 为 1 和 2 的情况）
const getExamplePost = (id: number): PostDetailData | undefined => {
  if (id === 1) {
    return {
      id: 1,
      title: '小橘的领养更新：越来越亲人啦！',
      author: '李同学',
      avatarInitial: '李',
      timeAgo: '3小时前',
      content: '小橘本来是出生一个月就被遗弃在校园里的小流浪猫，从最开始的警惕怕人到现在的主动蹭腿求摸摸。它已经成为我们宿舍楼的团宠，每天都有同学带着猫粮、猫条来看它。',
      fullContent: [
        '小橘本来是出生一个月就被遗弃在校园里的小流浪猫，从最开始的警惕怕人到现在的主动蹭腿求摸摸。它已经成为我们宿舍楼的团宠，每天都有同学带着猫粮、猫条来看它。',
        '记得刚发现它的时候，它躲在宿舍楼下的灌木丛里，只敢在夜深人静的时候出来找吃的。我们几个同学轮流喂它，一开始它总是保持距离，等我们走远了才敢靠近食物。',
        '经过一个多月的耐心接触，小橘终于开始信任我们了。现在它不仅会主动蹭我们的腿，还会发出响亮的呼噜声，甚至会在我们腿上打盹。它的性格真的超级好，不抓人不咬人，特别适合作为家庭宠物。',
        '最近我们正在为小橘寻找一个永久的家。希望找到一个有爱心、负责任的主人，能够给它一个温暖的家。小橘已经做了驱虫，下周会带它去做绝育手术。',
        '如果有意向领养小橘的同学，欢迎联系我！'
      ],
      images: ['小橘晒太阳', '小橘玩耍', '小橘吃饭'],
      likes: 128,
      comments: 42,
      views: 568
    };
  }
  if (id === 2) {
    return {
      id: 2,
      title: '秋季宠物常见疾病预防指南',
      author: '王医生',
      avatarInitial: '王',
      timeAgo: '1天前',
      content: '随着天气转凉，宠物容易患上呼吸道疾病和关节问题。本文为大家提供秋季宠物护理指南，包括饮食调整、保暖措施、疫苗接种提醒等实用建议。',
      fullContent: [
        '随着天气转凉，宠物容易患上呼吸道疾病和关节问题。本文为大家提供秋季宠物护理指南，包括饮食调整、保暖措施、疫苗接种提醒等实用建议。',
        '秋季是宠物疾病高发期，特别是呼吸道感染和关节疼痛。建议主人们提前做好预防工作，包括定期体检、及时接种疫苗、注意保暖等。',
        '饮食方面，可以适当增加营养，但要注意控制食量，避免宠物在秋季过度增重。同时，要保证充足的饮水，防止干燥引起的健康问题。'
      ],
      images: ['疾病预防图', '护理指南'],
      likes: 89,
      comments: 23,
      views: 432
    };
  }
  // 其它 id 不再返回示例帖子
  return undefined;
};

// 示例评论数据
const getExampleComments = (postId: number): Comment[] => {
  if (postId === 1) {
    return [
      {
        id: 1,
        content: '小橘太可爱了！我经常在宿舍楼下看到它，性格真的很好，希望它能找到一个好人家！',
        authorName: '王同学',
        date: '2小时前',
        likes: 12,
        isLiked: false,
        children: [
           {
             id: 101,
             content: '确实，上次我喂它，它还蹭我裤腿呢。',
             authorName: '赵同学',
             date: '1小时前',
             likes: 2,
             isLiked: false,
             parentId: 1,
             replyTo: '王同学'
           }
        ]
      },
      {
        id: 2,
        content: '感谢你们为小橘做的一切！绝育手术对流浪猫的健康和寿命非常重要。如果需要帮助，可以联系学校的动物保护社团。',
        authorName: '张医生',
        date: '1小时前',
        likes: 24,
        isLiked: true,
        children: []
      },
      {
        id: 3,
        content: '我室友对领养小橘很感兴趣！可以私聊一下领养条件和流程吗？',
        authorName: '陈同学',
        date: '45分钟前',
        likes: 5,
        isLiked: false,
        children: []
      },
      {
        id: 4,
        content: '看到小橘的变化真的很感动！你们做得太好了，希望更多流浪动物能像小橘一样幸运。',
        authorName: '刘学姐',
        date: '30分钟前',
        likes: 8,
        isLiked: false,
        children: []
      }
    ];
  } else {
    return [
      {
        id: 1,
        content: '非常实用的指南！我家的猫咪最近确实有些咳嗽，看来需要带它去检查一下了。',
        authorName: '李同学',
        date: '5小时前',
        likes: 3,
        isLiked: false,
        children: []
      },
      {
        id: 2,
        content: '感谢王医生的专业建议，这些预防措施对我们这些新手宠物主人来说很有帮助。',
        authorName: '张同学',
        date: '3小时前',
        likes: 7,
        isLiked: false,
        children: []
      }
    ];
  }
};

// 将帖子内容转换为段落数组
const postContentParagraphs = computed(() => {
  if (!post.value) return [];
  if (post.value.fullContent && Array.isArray(post.value.fullContent)) {
    return post.value.fullContent;
  }
  // 如果没有完整内容，将content按句号分割
  return post.value.content.split('。').filter(p => p.trim()).map(p => p.trim() + '。');
});

// 如果是从「我的帖子」过来，并且后端不返回详情时，尝试用路由 query 构建一个可预览的数据
const buildPostFromRouteIfAvailable = (id: number): boolean => {
  // 允许从任意列表页带参数进入，以便待审核/不可见时仍能展示基本信息和计数
  const title = (route.query.title as string) || '未命名帖子';
  const content = (route.query.content as string) || '';
  const time = (route.query.time as string) || '刚刚';
  const updatedAt = (route.query.updateTime as string) || '';
  const authorFromRoute = (route.query.author as string) || '';
  const avatarUrlFromRoute = (route.query.avatarUrl as string) || undefined;
  const userIdFromRoute = Number(route.query.userId) || undefined;
  const likes = Number(route.query.likes) || 0;
  const commentsCount = Number(route.query.comments) || 0;
  const views = Number(route.query.views) || 0;
  const reviewStatus = (route.query.reviewStatus as string) || '';
  let images: string[] = [];
  if (typeof route.query.images === 'string') {
    try {
      const parsed = JSON.parse(route.query.images);
      if (Array.isArray(parsed)) {
        images = parsed;
      }
    } catch (e) {
      console.error('解析路由中的图片列表失败', e);
    }
  }

  // 如果路由没有提供标题和内容，认为没有足够信息
  if (!title && !content && images.length === 0) return false;

  post.value = {
    id,
    userId: userIdFromRoute,
    title,
    author: authorFromRoute || '用户',
    avatarInitial: (authorFromRoute || '用')[0] || '用',
    avatarUrl: avatarUrlFromRoute,
    timeAgo: updatedAt || time,
    publishDate: updatedAt || time,
    content,
    fullContent: content
      ? content
          .split('。')
          .filter(p => p.trim())
          .map(p => p.trim() + '。')
      : [],
    images,
    likes,
    comments: commentsCount,
    views
  };
  isMockPost.value = false;
  if (reviewStatus && reviewStatus !== '通过') {
    reviewNotice.value = '当前帖子尚未通过审核，仅自己可见预览。';
  }
  return true;
};

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    // 先从缓存获取
    const cached = localStorage.getItem('currentUser');
    if (cached) {
      try {
        currentUser.value = JSON.parse(cached) as CurrentUserInfo;
      } catch (e) {
        console.error('解析缓存用户信息失败', e);
      }
    }
    // 再从接口获取最新信息
    const res = await getCurrentUser();
    if ((res.code === 0 || res.code === 200) && res.data) {
      currentUser.value = res.data;
      localStorage.setItem('currentUser', JSON.stringify(res.data));
    }
  } catch (error) {
    console.error('获取当前用户信息失败', error);
  }
};

// 加载帖子详情
const updateSnapshot = () => {
  if (!post.value) return;
  sessionStorage.setItem(
    `forumPostSnapshot_${post.value.id}`,
    JSON.stringify({
      likes: post.value.likes ?? 0,
      comments: post.value.comments ?? 0,
      views: post.value.views ?? 0,
      liked: isLiked.value
    })
  );
  window.dispatchEvent(new CustomEvent('forum-post-updated'));
};

const loadPost = async (id: number) => {
  try {
    // 并行请求：浏览数增加、详情获取、评论获取
    // 浏览数增加不阻塞主流程，但为了更新本地视图，我们会等待其结果
    const incrementTask = incrementPostViews(id)
      .then(() => true)
      .catch((e) => {
        console.warn('增加浏览数失败，不阻塞详情加载', e);
        return false;
      });

    // 详情请求
    const detailTask = getPostDetail(id);
    // 评论请求（非阻塞）
    const commentsTask = loadComments(id);

    const res = await detailTask;
    if (res.data) {
      const p = res.data as 帖子详情DTO;
      const createdAt = formatDateTime(p.createTime);
      const updatedAt = formatDateTime((p as any).updateTime || (p as any).editTime);
      const routeTime =
        (route.query.time as string) ||
        (route.query.publishDate as string) ||
        (route.query.timeAgo as string) ||
        '';
      const displayTime = routeTime || updatedAt || createdAt || '';

      const userInfo = (p as any).user || {};
      // 显示真实昵称/头像，优先用户中心昵称
      // 兼容不同字段命名，避免昵称为空
      const authorName =
        (p as any).userName ||
        (p as any).username ||
        (p as any).user_name ||
        (p as any).authorName ||
        (p as any).displayName ||
        (p as any).name ||
        (p as any).nickname ||
        (p as any).nickName ||
        (p as any).userNickname ||
        (p as any).user_nickname ||
        userInfo.userName ||
        userInfo.username ||
        userInfo.user_name ||
        userInfo.displayName ||
        userInfo.name ||
        userInfo.nickname ||
        userInfo.nickName ||
        userInfo.userNickname ||
        userInfo.user_nickname ||
        '未知用户';
      const avatarInitial = authorName[0] || '用';
      const avatarUrl =
        (p as any).userAvatar ||
        (p as any).authorAvatar ||
        (p as any).avatarUrl ||
        (p as any).avatar ||
        userInfo.avatarUrl ||
        userInfo.userAvatar ||
        userInfo.avatar;
      
      post.value = {
        id: p.postId ?? id,
        userId: p.userId,
        title: p.title || '无标题',
        author: authorName,
        avatarInitial: avatarInitial,
        avatarUrl,
        timeAgo: displayTime || createdAt,
        publishDate: displayTime || createdAt,
        content: p.content || '',
        fullContent: p.content
          ? p.content
              .split('。')
              .filter((para: string) => para.trim())
              .map((para: string) => para.trim() + '。')
          : [],
        images: Array.isArray(p.mediaUrls) ? p.mediaUrls : (typeof p.mediaUrls === 'string' ? [p.mediaUrls] : []),
        likes: p.likeCount ?? 0,
        comments: p.commentCount ?? 0,
        views: p.viewCount ?? 0
      };
      isMockPost.value = false;
      // 审核状态：非通过则提示仅预览、禁用互动
      const status = (p as any).reviewStatus || (p as any).status || '';
      if (status && String(status) !== '通过') {
        const timeTip = updatedAt || createdAt || '';
        reviewNotice.value = timeTip
          ? `当前帖子未通过审核或已下架（最新编辑时间：${timeTip}），暂不支持互动。`
          : '当前帖子未通过审核或已下架，暂不支持互动。';

        // 对未通过/待审核的帖子，如果后端清零计数，则用路由携带的快照保留原有计数
        const queryLikes = Number(route.query.likes) || 0;
        const queryComments = Number(route.query.comments) || 0;
        const queryViews = Number(route.query.views) || 0;
        if (queryLikes > 0) post.value.likes = queryLikes;
        if (queryComments > 0) post.value.comments = queryComments;
        if (queryViews > 0) post.value.views = queryViews;
      } else {
        reviewNotice.value = '';
      }
      
      // 如仍未知昵称，尝试请求用户信息补全
      if (post.value.author === '未知用户' && post.value.userId) {
        await ensureAuthorFromUser(post.value.userId);
      }
      // 检查点赞状态（优先使用接口返回的，其次使用本地存储的）
      if (typeof (p as any).isLiked === 'boolean') {
        isLiked.value = (p as any).isLiked;
      } else if (typeof (p as any).liked === 'boolean') {
        isLiked.value = (p as any).liked;
      } else {
      // 如果接口没有返回点赞状态，从本地存储读取
      const likedPosts = (JSON.parse(localStorage.getItem('likedPosts') || '[]') as (number | string)[])
        .map((val) => normalizeId(val))
        .filter((n) => Number.isFinite(n));
      isLiked.value = likedPosts.includes(normalizeId(p.postId ?? id));
      }

    // 路由快照：避免列表与详情不同步
    const likedFromRoute = route.query.liked === '1' || route.query.liked === 'true';
    if (likedFromRoute) {
      isLiked.value = true;
      updateLikedPostsStorage(post.value.id, true);
    }
    const queryLikes = Number(route.query.likes) || 0;
    const queryComments = Number(route.query.comments) || 0;
    const queryViews = Number(route.query.views) || 0;
    if (queryLikes > 0) post.value.likes = Math.max(post.value.likes || 0, queryLikes);
    if (queryComments > 0) post.value.comments = Math.max(post.value.comments || 0, queryComments);
    if (queryViews > 0) post.value.views = Math.max(Number(post.value.views) || 0, queryViews);

    // 检查浏览数增加结果
    const incremented = await incrementTask;
    if (incremented && post.value) {
      // 手动增加浏览数，确保用户看到的数字是即时更新的
      post.value.views = Number(post.value.views || 0) + 1;
    }
    // 更新快照，确保返回列表时数据一致
    updateSnapshot();

    } else {
      // 接口返回空数据，如果是示例 id（1 或 2），使用示例数据
      const example = getExamplePost(id);
      if (example) {
        post.value = example;
        isMockPost.value = true;
        reviewNotice.value = '';
      } else if (!buildPostFromRouteIfAvailable(id)) {
        // 既不是示例帖子，也没有从「我的帖子」带过来的数据，才提示不可查看
        post.value = undefined;
        isMockPost.value = false;
        reviewNotice.value = '';
        errorMessage.value = '帖子不存在或尚未通过审核，暂时无法查看详情。';
        showErrorModal.value = true;
      }
    }
    
    // 等待评论加载完成
    await commentsTask;

    // 如果评论接口返回了总数，同步更新帖子显示的评论数
    if (totalComments.value > 0 && post.value) {
        // 取最大值，避免详情接口和评论接口数据不一致导致减少
        post.value.comments = Math.max(post.value.comments || 0, totalComments.value);
        updateSnapshot();
    }
  } catch (error) {
    console.error('加载帖子失败:', error);
    // 接口失败，如果是示例 id（1 或 2），使用示例数据
    const example = getExamplePost(id);
    if (example) {
      post.value = example;
      isMockPost.value = true;
      reviewNotice.value = '';
      await loadComments(id);
    } else if (buildPostFromRouteIfAvailable(id)) {
      // 即使是从路由恢复的帖子，也尝试加载评论
      await loadComments(id);
    } else {
      post.value = undefined;
      isMockPost.value = false;
      reviewNotice.value = '';
      errorMessage.value = '帖子详情加载失败，可能不存在或尚未通过审核。';
      showErrorModal.value = true;
    }
  }
};



// 获取评论列表
const loadComments = async (postId: number) => {
  if (loadingComments.value) return;
  loadingComments.value = true;
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      sortBy: 'TIME',
      order: 'DESC'
    };
    const res = await getPostComments(postId, params);

    // 后端返回格式可能是：
    // 1) data: Comment[]
    // 2) data: { data: Comment[] }
    // 3) data: { list: Comment[] } / { records: Comment[] }
    let rawList: any[] = [];
    const d: any = res.data;
    
    // 尝试提取总数
    if (d && d.total !== undefined) {
      totalComments.value = Number(d.total);
    } else if ((res as any).total !== undefined) {
      totalComments.value = Number((res as any).total);
    }

    // 注意：不再同步更新 post.value.comments，因为 totalComments 仅代表根评论总数，而非全贴总评论数（含子评论）

    if (Array.isArray(d)) {
      rawList = d;
    } else if (d && Array.isArray(d.data)) {
      rawList = d.data;
    } else if (d && Array.isArray(d.list)) {
      rawList = d.list;
    } else if (d && Array.isArray(d.records)) {
      rawList = d.records;
    }

    const flatComments = normalizeComments(rawList);

    // 如果接口返回空数据，仅对内置示例帖子使用示例评论
    if (flatComments.length === 0 && isMockPost.value) {
      comments.value = getExampleComments(postId);
      // 示例数据总数
      totalComments.value = comments.value.length; 
    } else {
       // 直接赋值，因为后端现在返回的就是分页的根评论列表
       comments.value = flatComments;
    }
  } catch (error) {
    console.error('加载评论失败:', error);
    // 失败时不覆盖现有评论，除非是第一次加载
    if (comments.value.length === 0) {
       // 可以在这里设置错误状态
    }
  } finally {
    loadingComments.value = false;
  }
};


// 通过 userId 请求用户信息，补全作者昵称/头像
const ensureAuthorFromUser = async (userId: number) => {
  if (userNameCache.has(userId)) {
    const cachedName = userNameCache.get(userId);
    const cachedAvatar = userAvatarCache.get(userId);
    if (post.value && cachedName) {
      post.value.author = cachedName;
      post.value.avatarInitial = cachedName[0] || post.value.avatarInitial || '用';
    }
    if (post.value && cachedAvatar && !post.value.avatarUrl) {
      post.value.avatarUrl = cachedAvatar;
    }
    return;
  }
  if (pendingUserFetch.has(userId)) {
    await pendingUserFetch.get(userId);
    return;
  }
  const fetchPromise = (async () => {
    try {
      const res = await getUserInfo(userId);
      if ((res.code === 0 || res.code === 200) && res.data) {
        const d: any = res.data;
        const name =
          d.userName ||
          d.username ||
          d.user_name ||
          d.name ||
          d.nickname ||
          d.nickName ||
          d.userNickname ||
          d.user_nickname ||
          d.displayName ||
          `用户${userId}`;
        const avatar =
          d.avatarUrl ||
          d.userAvatar ||
          d.avatar ||
          d.userAvatarUrl ||
          d.avatarPath;
        userNameCache.set(userId, name);
        userAvatarCache.set(userId, avatar);
        if (post.value && post.value.userId === userId) {
          post.value.author = name;
          post.value.avatarInitial = name[0] || post.value.avatarInitial || '用';
          if (!post.value.avatarUrl && avatar) {
            post.value.avatarUrl = avatar;
          }
        }
      }
    } catch (e) {
      console.error('获取用户信息失败', e);
    } finally {
      pendingUserFetch.delete(userId);
    }
  })();
  pendingUserFetch.set(userId, fetchPromise);
  await fetchPromise;
};

const normalizeId = (id: number | string): number => {
  const n = typeof id === 'string' ? Number(id) : id;
  return Number.isFinite(n) ? n : 0;
};

// 更新本地存储的点赞状态
const updateLikedPostsStorage = (postId: number | string, liked: boolean) => {
  try {
    const idNum = normalizeId(postId);
    const likedPosts = (JSON.parse(localStorage.getItem('likedPosts') || '[]') as (number | string)[])
      .map((val) => normalizeId(val))
      .filter(Boolean);
    if (liked) {
      if (!likedPosts.includes(idNum)) {
        likedPosts.push(idNum);
      }
    } else {
      const index = likedPosts.indexOf(idNum);
      if (index > -1) {
        likedPosts.splice(index, 1);
      }
    }
    localStorage.setItem('likedPosts', JSON.stringify(likedPosts));
  } catch (error) {
    console.error('更新点赞状态到本地存储失败', error);
  }
};

// 点赞
const toggleLike = async () => {
  if (!post.value) return;

  // 示例帖子并不存在于后端，直接提示用户，不调用接口
  if (isMockPost.value) {
    errorMessage.value = '当前帖子是示例内容，用于展示效果，暂不支持实际点赞操作。';
    showErrorModal.value = true;
    return;
  }
  
  const previousLiked = isLiked.value;
  const previousLikes = post.value.likes || 0;
  const postId = post.value.id;

  // 乐观更新UI
  isLiked.value = !isLiked.value;
  if (isLiked.value) {
    // 如果之前未点赞，现在点赞，数量+1
    post.value.likes = (post.value.likes || 0) + 1;
  } else {
    // 如果之前已点赞，现在取消点赞，数量-1
    post.value.likes = Math.max(0, (post.value.likes || 0) - 1);
  }
  
  // 更新本地存储
  updateLikedPostsStorage(postId, isLiked.value);

  try {
    await likePostApi(postId);
    // 记录最新快照，供列表页同步
    updateSnapshot();
  } catch (error: any) {
    // 回滚UI状态
    isLiked.value = previousLiked;
    post.value.likes = previousLikes;
    // 回滚本地存储
    updateLikedPostsStorage(postId, previousLiked);
    errorMessage.value = error?.message || '点赞失败，请稍后重试';
    showErrorModal.value = true;
    console.error('点赞失败:', error);
  }
};

// 提交根评论
const handleRootSubmit = async () => {
  if (!post.value || !newComment.value.trim()) return;
  await doSubmitComment(newComment.value.trim());
};

// 处理评论数更新（来自子组件）
const handleUpdateCount = (delta: number) => {
  if (post.value) {
    post.value.comments = (post.value.comments || 0) + delta;
  }
  // totalComments 仅用于根评论分页，子评论回复不应影响它
  // totalComments.value = (totalComments.value || 0) + delta;
  updateSnapshot();
};

// 统一提交逻辑（仅用于根评论）
const doSubmitComment = async (content: string) => {
  if (!post.value) return;

  try {
    await submitCommentApi(post.value.id, { content });
    
    newComment.value = ''; // 清空根评论输入框
    // 提交成功后，重新加载评论列表（回到第一页，因为是按时间倒序）
    currentPage.value = 1;
    await loadComments(post.value.id);
    
    // 手动增加总评论数（根评论+1），不再使用 loadComments 返回的 rootTotal 覆盖
    if (post.value) {
       post.value.comments = (post.value.comments || 0) + 1;
    }
    updateSnapshot();
    
  } catch (error: any) {
    const msg =
      error?.message?.includes('不存在') || error?.message?.includes('未发布')
        ? '帖子不存在或未发布，无法评论。'
        : error?.message || '评论失败，请稍后重试';
    errorMessage.value = msg;
    showErrorModal.value = true;
  }
};

// 点赞评论
const handleLikeComment = async (commentId: number) => {
  // 递归查找评论
  const findComment = (list: Comment[], id: number): Comment | undefined => {
    for (const c of list) {
      if (c.id === id) return c;
      if (c.children) {
        const found = findComment(c.children, id);
        if (found) return found;
      }
    }
    return undefined;
  };

  const comment = findComment(comments.value, commentId);
  if (!comment) return;

  const previousLiked = comment.isLiked;
  const previousLikes = comment.likes;

  // 乐观更新
  comment.isLiked = !comment.isLiked;
  comment.likes = comment.isLiked ? comment.likes + 1 : Math.max(0, comment.likes - 1);

  try {
    await likeCommentApi(commentId);
  } catch (error) {
    // 回滚
    comment.isLiked = previousLiked;
    comment.likes = previousLikes;
    console.error('点赞评论失败', error);
  }
};

// 返回
const goBack = () => {
  // 返回前保存快照，供列表页同步最新点赞/评论/浏览状态
  updateSnapshot();

  // 优先处理来源路由
  if (route.query.from === 'myPosts') {
    const myPostsPage = route.query.fromMyPostsPage || sessionStorage.getItem('myPostsLastPage');
    if (myPostsPage && typeof myPostsPage === 'string') {
      router.push({ path: '/user-center', query: { menu: 'posts', page: myPostsPage } });
    } else {
      router.push({ path: '/user-center', query: { menu: 'posts' } });
    }
    return;
  }
  const fromPage = route.query.fromPage || sessionStorage.getItem('forumLastPage');
  if (fromPage && typeof fromPage === 'string') {
    const pageNum = parseInt(fromPage, 10);
    if (!isNaN(pageNum) && pageNum > 0) {
      router.push({ name: 'Forum', query: { page: pageNum.toString() } });
      return;
    }
  }
  // 优先回退到历史页面，若无历史则回论坛列表
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push({ name: 'Forum' });
  }
};

// 监听路由参数
watch(
  () => route.params.id,
  async (newVal) => {
    if (!newVal) return;
    const idNum = Number(newVal);
    if (!isNaN(idNum)) {
      // 确保用户信息已加载
      if (!currentUser.value) {
        await loadCurrentUser();
      }
      loadPost(idNum);
    }
  },
  { immediate: true }
);

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  if (target) {
    target.style.display = 'none';
  }
};

const handleAvatarError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  if (target) {
    target.style.display = 'none';
  }
  if (post.value) {
    post.value.avatarUrl = undefined;
  }
};

const handleCommentAvatarError = (event: Event, c: Comment) => {
  const target = event.target as HTMLImageElement;
  if (target) {
    target.style.display = 'none';
  }
  c.authorAvatar = '';
};

// 跳转到用户主页（有 userId 时）
const goToUserProfile = (userId?: number) => {
  if (!userId) return;
  router.push({ name: 'UserProfile', params: { userId } });
};
</script>

<style scoped>
:root {
  --primary-color: #FF8C00;
  --text-orange: #FF8C00;
  --bg-color: #F8F9FB;
  --card-bg: #FFFFFF;
  --text-main: #333333;
  --text-sub: #666666;
  --border-color: #E5E7EB;
}

.post-detail-page {
  background-color: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 40px;
}

.forum-main {
  max-width: 900px;
  margin: 30px auto;
  padding: 0 20px;
}

/* 返回按钮 */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 10px 16px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-sub);
  font-size: 14px;
  transition: all 0.3s;
  text-decoration: none;
  cursor: pointer;
}

.back-btn:hover {
  border-color: #FF8C00 !important;
  color: #FF8C00 !important;
}

/* 帖子详情 */
.post-detail {
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 30px;
  margin-bottom: 30px;
}

.post-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: var(--text-sub);
  font-size: 14px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #FF8C00 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white !important;
  font-weight: 600;
  overflow: hidden;
}
.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-title {
  font-size: 24px;
  font-weight: 700;
  color: #FF8C00 !important;
  line-height: 1.4;
}

.post-content {
  color: var(--text-main);
  line-height: 1.8;
  margin-bottom: 25px;
}

.post-content p {
  margin-bottom: 15px;
}

/* 帖子图片和视频 */
.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin: 25px 0;
}

.post-image,
.post-media {
  width: 100%;
  height: 200px;
  border-radius: 12px;
  object-fit: cover;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  font-size: 14px;
  overflow: hidden;
  position: relative;
}

.post-image img,
.post-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-media .post-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000;
}

.post-stats {
  display: flex;
  gap: 25px;
  color: var(--text-sub);
  font-size: 14px;
  border-top: 1px solid var(--border-color);
  padding-top: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: color 0.3s;
}

.stat-item:hover {
  color: #FF8C00 !important;
}

/* 点过赞后的高亮状态，使用主色橙色并强制覆盖 */
.stat-item.liked {
  color: #FF8C00 !important;
}

/* 评论区域 */
.comments-section {
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 30px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  color: var(--text-main);
}

.comment-form {
  margin-bottom: 30px;
}

.comment-input {
  width: 100%;
  padding: 15px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 15px;
  resize: vertical;
  min-height: 100px;
  margin-bottom: 15px;
  transition: border-color 0.3s;
  font-family: inherit;
}

.comment-input:focus {
  border-color: #FF8C00 !important;
  outline: none;
}

.comment-submit {
  padding: 12px 25px;
  background: #FF8C00 !important;
  color: white !important;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.comment-submit:hover:not(:disabled) {
  background: #E67A2A !important;
}

.comment-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 20px;
  border-radius: 12px;
  background: #fafafa;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #4A90E2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
  font-weight: 600;
  overflow: hidden;
}
.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-author {
  font-weight: 600;
  color: var(--text-main);
}

.comment-time {
  color: var(--text-sub);
  font-size: 13px;
}

.reply-btn {
  margin-left: auto;
  background: transparent;
  border: none;
  color: #FF8C00;
  cursor: pointer;
}

.reply-box {
  margin-top: 12px;
}

.reply-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.btn-cancel {
  padding: 12px 16px;
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: #555;
}

.comment-content {
  color: var(--text-main);
  line-height: 1.6;
}

/* 错误模态框 */
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.modal-icon {
  font-size: 70px;
  margin-bottom: 25px;
}

.modal-content h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 24px;
}

.modal-content p {
  margin-bottom: 30px;
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

.modal-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.btn {
  padding: 14px 40px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  min-width: 140px;
}

.btn-primary {
  background: #ff8c00 !important;
  color: white;
}

.btn-primary:hover {
  background: #E67A2A !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(255, 140, 0, 0.3);
}

/* 图片预览弹层 */
.image-preview-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.image-preview-mask .image-wrapper {
  max-width: 80vw;
  max-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #111;
  border-radius: 8px;
  padding: 12px;
}

.image-preview-mask img {
  max-width: 78vw;
  max-height: 78vh;
  object-fit: contain;
}

.nav-btn,
.close-btn {
  position: absolute;
  border: none;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  width: 40px;
  height: 40px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s ease;
}

.nav-btn:hover,
.close-btn:hover {
  background: rgba(255, 255, 255, 0.28);
}

.nav-btn.left {
  left: 40px;
}

.nav-btn.right {
  right: 40px;
}

.close-btn {
  top: 24px;
  right: 32px;
}

/* 响应式 */
@media (max-width: 850px) {
  .post-images {
    grid-template-columns: 1fr;
  }
}
</style>
