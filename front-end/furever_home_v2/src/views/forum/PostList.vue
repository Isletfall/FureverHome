<template>
  <div ref="containerRef">
    <main class="forum-main">
      <div class="welcome-header">
        <h1 class="page-title">宠物论坛</h1>
        <p class="page-subtitle">分享养宠经验，交流信息心得</p>
      </div>

      <!-- 搜索栏 -->
      <div class="search-container">
        <div class="search-box">
          <input
            type="text"
            class="search-input"
            placeholder="搜索帖子..."
            v-model="searchQuery"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="post-list">
        <div
          v-for="post in pagedPosts"
          :key="post.id"
          class="post-card"
          @click="goToPostDetail(post.id)"
        >
          <div class="post-header">
            <div class="post-meta">
              <div class="post-author">
                <div class="author-avatar">
                  <img
                    v-if="post.avatarUrl"
                    :src="post.avatarUrl"
                    alt="用户头像"
                    @error="handleAvatarError($event, post)"
                  />
                  <span v-else>{{ post.avatarInitial }}</span>
                </div>
                <span>{{ post.author }}</span>
              </div>
              <span>{{ post.timeAgo }}</span>
            </div>
            <div class="post-title">{{ post.title }}</div>
          </div>

          <div class="post-content clamped">{{ post.content }}</div>

          <div v-if="post.images && post.images.length" class="grid grid-cols-3 gap-2 mt-3">
            <div
              v-for="(media, index) in post.images.slice(0, 3)"
              :key="index"
              class="relative w-full aspect-[4/3] bg-slate-100 rounded-lg overflow-hidden border border-slate-200"
            >
              <img
                v-if="typeof media === 'string' && (media.startsWith('http') || media.startsWith('/')) && !isVideoUrl(media)"
                :src="media"
                :alt="`帖子图片 ${index + 1}`"
                class="w-full h-full object-cover hover:scale-105 transition-transform duration-500"
                @error="handleImageError"
                @load="console.log('图片加载成功:', media)"
              />
              <video
                v-else-if="typeof media === 'string' && (media.startsWith('http') || media.startsWith('/')) && isVideoUrl(media)"
                :src="media"
                controls
                preload="metadata"
                class="w-full h-full object-cover"
                @loadedmetadata="console.log('视频加载成功:', media)"
                @error="console.error('视频加载失败:', media, $event)"
                @play="onVideoPlay($event)"
              ></video>
              <span v-else class="flex items-center justify-center w-full h-full text-xs text-gray-400">{{ media }}</span>
            </div>
          </div>

          <div class="post-stats" @click.stop>
            <div
              class="stat-item"
              :class="{ liked: post.isLiked }"
              @click="(e) => toggleLike(post, e)"
            >
              <i class="fa-solid fa-thumbs-up"></i> {{ post.likes }}
            </div>
            <div class="stat-item">
              <i class="fa-solid fa-comment"></i> {{ post.comments }}
            </div>
            <div class="stat-item">
              <i class="fa-solid fa-eye"></i> {{ post.views }}
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div v-if="searchType === 'post' && posts.length > 0" class="pagination-container">
        <div class="pagination-wrapper">
          <button
            class="pagination-btn"
            :class="{ disabled: currentPage === 1 }"
            :disabled="currentPage === 1"
            @click="prevPage"
          >
            <i class="fa-solid fa-chevron-left"></i>
          </button>

          <div class="pagination-pages">
            <button
              v-for="page in visiblePages"
              :key="page"
              class="pagination-page-btn"
              :class="{ active: page === currentPage }"
              @click="handlePageClick(page)"
            >
              {{ page }}
            </button>
          </div>

          <button
            class="pagination-btn"
            :class="{ disabled: currentPage >= totalPages }"
            :disabled="currentPage >= totalPages"
            @click="nextPage"
          >
            <i class="fa-solid fa-chevron-right"></i>
          </button>

          <div class="pagination-info">
            <span>共 {{ totalCount || posts.length }} 条，第 {{ currentPage }} / {{ totalPages }} 页</span>
          </div>

          <div class="pagination-jump">
            <span class="jump-label">跳转到</span>
            <input
              type="number"
              class="jump-input"
              v-model.number="jumpPageInput"
              :min="1"
              @keyup.enter="handleJumpPage"
            />
            <span class="jump-label">页</span>
            <button class="jump-btn" @click="handleJumpPage">跳转</button>
          </div>
        </div>
      </div>

      <!-- 无帖子提示 -->
      <div v-if="searchType === 'post' && posts.length === 0 && !isSearching" class="no-results">
        暂无帖子
      </div>

      <!-- 发布帖子按钮 -->
      <button class="new-post-btn" @click="goToPostCreation">
        <i class="fa-solid fa-pen"></i> 发布帖子
      </button>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onBeforeUnmount, onDeactivated, onActivated, onUnmounted, watch } from 'vue';

defineOptions({
  name: 'PostList'
})

import { useRouter, useRoute } from 'vue-router';
import { getPostList, searchPosts } from '@/api/postApi';
import { likePost as likePostApi } from '@/api/commentapi';
import { getCurrentUser, getUserInfo, type CurrentUserInfo } from '@/api/userApi';
import { isVideoUrl } from '@/utils/mediaUtils';
import { formatDateTime } from '@/utils/format';

interface Post {
  id: number;
  userId?: number;
  author: string;
  avatarInitial: string;
  avatarUrl?: string;
  timeAgo: string;
  title: string;
  content: string;
  images: string[];
  likes: number;
  comments: number;
  views: string;
  isLiked?: boolean;
}

const router = useRouter();
const route = useRoute();
const posts = ref<Post[]>([]);
const totalCount = ref(0);
const searchQuery = ref('');
const currentUser = ref<CurrentUserInfo | null>(null);
const searchType = ref<'post'>('post');
const isSearching = ref(false);
const containerRef = ref<HTMLElement | null>(null);

// 视频播放控制：确保同一时间只有一个视频播放
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

const userNameCache = new Map<number, string>();
const userAvatarCache = new Map<number, string | undefined>();
const pendingUserFetch = new Map<number, Promise<void>>();

// 分页相关
const pageSize = 4;
const currentPage = ref(1);
const jumpPageInput = ref<number | null>(null);
const useServerPaging = computed(() => totalCount.value > posts.value.length);
const totalPages = computed(() => Math.max(1, Math.ceil((totalCount.value || posts.value.length) / pageSize)));

// 当前页的帖子
const pagedPosts = computed(() => {
  if (useServerPaging.value) return posts.value;
  const start = (currentPage.value - 1) * pageSize;
  return posts.value.slice(start, start + pageSize);
});

// 显示的分页页码（只显示最多3个：前一页、当前页、后一页）
const visiblePages = computed(() => {
  const pages: number[] = [];
  const total = totalPages.value;
  const current = currentPage.value;

  if (total <= 1) {
    // 只有1页，只显示1
    pages.push(1);
  } else if (total === 2) {
    // 只有2页，显示1和2
    pages.push(1, 2);
  } else {
    // 3页及以上，只显示前一页、当前页、后一页
    if (current === 1) {
      // 当前页是第一页，显示1和2
      pages.push(1, 2);
    } else if (current === total) {
      // 当前页是最后一页，显示最后一页和前一页
      pages.push(total - 1, total);
    } else {
      // 当前页在中间，显示前一页、当前页、后一页
      pages.push(current - 1, current, current + 1);
    }
  }

  return pages;
});

// 用户搜索已移除，仅保留帖子搜索
interface UserSearchResult {
  userId: number;
  userName: string;
  avatarInitial: string;
  avatarUrl?: string;
}

const searchResults = ref<{
  users: UserSearchResult[];
}>({
  users: []
});

// 示例数据（用于展示和接口失败时的fallback）
const getExamplePosts = (): Post[] => [
  {
    id: 1,
    author: '李同学',
    avatarInitial: '李',
    timeAgo: '3小时前',
    title: '小橘的领养更新：越来越亲人啦！',
    content: '小橘本来是出生一个月就被遗弃在校园里的小流浪猫，从最开始的警惕怕人到现在的主动蹭腿求摸摸。它已经成为我们宿舍楼的团宠，每天都有同学带着猫粮、猫条来看它。',
    images: ['小橘晒太阳', '小橘玩耍', '小橘吃饭'],
    likes: 128,
    comments: 42,
    views: '568'
  },
  {
    id: 2,
    author: '王医生',
    avatarInitial: '王',
    timeAgo: '1天前',
    title: '秋季宠物常见疾病预防指南',
    content: '随着天气转凉，宠物容易患上呼吸道疾病和关节问题。本文为大家提供秋季宠物护理指南，包括饮食调整、保暖措施、疫苗接种提醒等实用建议。',
    images: ['疾病预防图', '护理指南'],
    likes: 89,
    comments: 23,
    views: '432'
  }
];

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

// 将后端返回的帖子列表（可能是数组或分页结果）转换为前端展示结构
const mapPosts = (list: any[]): Post[] => {
  return list.map((p: any) => {
    // 解析图片和视频：优先 images，其次 mediaUrls（可能是字符串或 JSON 字符串）
    let images: string[] = [];
    if (Array.isArray(p.images)) {
      images = p.images;
    } else if (Array.isArray(p.mediaUrls)) {
      images = p.mediaUrls;
    } else if (typeof p.mediaUrls === 'string' && p.mediaUrls) {
      try {
        const parsed = JSON.parse(p.mediaUrls);
        if (Array.isArray(parsed)) {
          images = parsed;
        } else {
          images = [p.mediaUrls];
        }
      } catch {
        images = [p.mediaUrls];
      }
    }

    // 调试日志：检查媒体URL
    if (images.length > 0) {
      console.log(`帖子 ${p.id || p.postId} (${p.title}) 的媒体URL:`, images);
      images.forEach((url, idx) => {
        const isVideo = isVideoUrl(url);
        console.log(`  媒体 ${idx + 1}: ${url} - ${isVideo ? '视频' : '图片'}`);
      });
    }

    // 判断是否是当前用户发布的帖子（显示真实昵称，不强制替换为“我”）
    const userInfo = p.user || {};
    // 兼容后端不同命名，避免作者名为空
    const hasNameField =
      p.userName ||
      p.username ||
      p.user_name ||
      p.authorName ||
      p.displayName ||
      p.name ||
      p.nickname ||
      p.nickName ||
      p.userNickname ||
      p.user_nickname ||
      userInfo.userName ||
      userInfo.username ||
      userInfo.user_name ||
      userInfo.displayName ||
      userInfo.name ||
      userInfo.nickname ||
      userInfo.nickName ||
      userInfo.userNickname ||
      userInfo.user_nickname;
    const authorName =
      p.userName ||
      p.username ||
      p.user_name ||
      p.authorName ||
      p.displayName ||
      p.name ||
      p.nickname ||
      p.nickName ||
      p.userNickname ||
      p.user_nickname ||
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
    let avatarUrl =
      p.userAvatar ||
      p.authorAvatar ||
      p.avatarUrl ||
      p.avatar ||
      userInfo.avatarUrl ||
      userInfo.userAvatar ||
      userInfo.avatar;

    if (currentUser.value && p.userId === currentUser.value.userId) {
      avatarUrl = currentUser.value.avatarUrl || avatarUrl;
    }

    // 检查点赞状态（优先使用接口返回的，其次使用本地存储的）
    let isLiked = false;
    if (typeof p.isLiked === 'boolean') {
      isLiked = p.isLiked;
    } else if (typeof p.liked === 'boolean') {
      isLiked = p.liked;
    } else {
      // 如果接口没有返回点赞状态，从本地存储读取
      const likedPosts = JSON.parse(localStorage.getItem('likedPosts') || '[]') as number[];
      isLiked = likedPosts.includes(p.id || p.postId);
    }

    const mapped: Post = {
      id: p.id || p.postId,
      userId: p.userId,
      author: authorName,
      avatarInitial: avatarInitial,
      avatarUrl: avatarUrl,
      timeAgo: formatDateTime(p.createTime || p.timeAgo || new Date()),
      title: p.title || '无标题',
      content: p.content || p.summary || '',
      images,
      likes: p.likes || p.likeCount || 0,
      comments: p.comments || p.commentCount || 0,
      views: p.views || p.viewCount || '0',
      isLiked: isLiked
    };
    // 若没有任何名称字段且有 userId，尝试异步补全昵称
    if (!hasNameField && mapped.userId) {
      void ensureAuthorFromUser(mapped.userId);
    }
    return mapped;
  });
};

// 加载帖子列表（支持分页参数）
const loadPosts = async (page = 1) => {
  try {
    const res = await getPostList({ page, pageNum: page, pageSize });
    let list: any[] = [];
    let total = 0;

    if (res.data) {
      const d: any = res.data;
      if (Array.isArray(d)) {
        list = d;
      } else if (Array.isArray(d.list)) {
        list = d.list;
        total = d.total ?? d.totalCount ?? d.count ?? list.length;
      } else if (Array.isArray(d.records)) {
        list = d.records;
        total = d.total ?? d.totalCount ?? d.count ?? list.length;
      }
      // 兼容有 meta/分页对象的结构
      if (!total && d?.pagination?.total) total = d.pagination.total;
    }

    if (list.length > 0) {
      posts.value = mapPosts(list);
      totalCount.value = total || posts.value.length;
    } else {
      // 接口返回空数据，使用示例数据
      posts.value = getExamplePosts();
      totalCount.value = posts.value.length;
    }
    currentPage.value = page;
  } catch (error) {
    console.error('加载接口失败，显示示例数据', error);
    // 接口失败，使用示例数据
    posts.value = getExamplePosts();
    totalCount.value = posts.value.length;
    currentPage.value = 1;
  }

  // 合并详情页返回时的快照数据
  mergeSnapshotsIntoPosts();
};

// 将详情页记录在 sessionStorage 的最新点赞/评论/浏览同步回列表
const mergeSnapshotsIntoPosts = () => {
  try {
    const keys = Object.keys(sessionStorage).filter(k => k.startsWith('forumPostSnapshot_'));
    if (!keys.length) return;
    const map = new Map<number, { likes?: number; comments?: number; views?: number | string; liked?: boolean }>();
    keys.forEach(k => {
      try {
        const val = JSON.parse(sessionStorage.getItem(k) || '{}');
        const id = Number(k.replace('forumPostSnapshot_', ''));
        if (Number.isFinite(id)) {
          map.set(id, val);
        }
      } catch (e) {
        // ignore parse error
      }
    });
    if (!map.size) return;
    posts.value = posts.value.map(p => {
      const snap = map.get(Number(p.id));
      if (!snap) return p;
      return {
        ...p,
        likes: snap.likes ?? p.likes,
        comments: snap.comments ?? p.comments,
        views: (snap.views !== undefined ? String(snap.views) : p.views),
        isLiked: typeof snap.liked === 'boolean' ? snap.liked : p.isLiked
      };
    });
  } catch (e) {
    console.warn('合并帖子快照失败', e);
  }
};

// 搜索功能
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    // 如果搜索框为空，加载全部帖子
    if (searchType.value === 'post') {
      loadPosts();
    } else {
      searchResults.value.users = [];
    }
    return;
  }

  const keyword = searchQuery.value.trim().toLowerCase();

  if (searchType.value === 'post') {
    // 搜索帖子
    isSearching.value = true;
    try {
      const res = await searchPosts({
        keyword: keyword,
        page: 1,
        pageSize: 100 // 获取更多结果以便前端过滤
      });

      if (res.data && res.data.list && res.data.list.length > 0) {
        // 前端再次过滤，只匹配标题和内容
        const filtered = res.data.list.filter((p: any) => {
          const title = (p.title || '').toLowerCase();
          const content = (p.content || p.summary || '').toLowerCase();

          return title.includes(keyword) || content.includes(keyword);
        });

        posts.value = filtered.map((p: any) => {
          // 判断是否是当前用户发布的帖子
          const userInfo = p.user || {};
          // 兼容不同字段命名，避免昵称缺失
          const hasNameField =
            p.userName ||
            p.username ||
            p.user_name ||
            p.authorName ||
            p.displayName ||
            p.name ||
            p.nickname ||
            p.nickName ||
            p.userNickname ||
            p.user_nickname ||
            userInfo.userName ||
            userInfo.username ||
            userInfo.user_name ||
            userInfo.displayName ||
            userInfo.name ||
            userInfo.nickname ||
            userInfo.nickName ||
            userInfo.userNickname ||
            userInfo.user_nickname;
          const authorName =
            p.userName ||
            p.username ||
            p.user_name ||
            p.authorName ||
            p.displayName ||
            p.name ||
            p.nickname ||
            p.nickName ||
            p.userNickname ||
            p.user_nickname ||
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
          let avatarUrl =
            p.userAvatar ||
            p.authorAvatar ||
            p.avatarUrl ||
            p.avatar ||
            userInfo.avatarUrl ||
            userInfo.userAvatar ||
            userInfo.avatar;

          if (currentUser.value && p.userId === currentUser.value.userId) {
            avatarUrl = currentUser.value.avatarUrl || avatarUrl;
          }

          // 检查点赞状态（优先使用接口返回的，其次使用本地存储的）
          let isLiked = false;
          if (typeof p.isLiked === 'boolean') {
            isLiked = p.isLiked;
          } else if (typeof p.liked === 'boolean') {
            isLiked = p.liked;
          } else {
            // 如果接口没有返回点赞状态，从本地存储读取
            const likedPosts = JSON.parse(localStorage.getItem('likedPosts') || '[]') as number[];
            isLiked = likedPosts.includes(p.id || p.postId);
          }

          const mapped: Post = {
            id: p.id || p.postId,
            userId: p.userId,
            author: authorName,
            avatarInitial: avatarInitial,
      avatarUrl: avatarUrl,
      timeAgo: formatDateTime(p.createTime || p.timeAgo || new Date()),
            title: p.title || '无标题',
            content: p.content || p.summary || '',
            images: p.images || [],
            likes: p.likes || p.likeCount || 0,
            comments: p.comments || p.commentCount || 0,
            views: p.views || p.viewCount || '0',
            isLiked: isLiked
          };
          if (!hasNameField && mapped.userId) {
            void ensureAuthorFromUser(mapped.userId);
          }
          return mapped;
        });
        totalCount.value = posts.value.length;
      } else {
      // 如果后端搜索无结果，尝试从前端已加载的帖子中搜索
      const allPosts = await getAllPostsForSearch();
      const filtered = allPosts.filter((p: any) => {
        const title = (p.title || '').toLowerCase();
        const content = (p.content || p.summary || '').toLowerCase();

        return title.includes(keyword) || content.includes(keyword);
      });

        posts.value = mapPosts(filtered);
      }
      // 搜索后重置到第一页
      currentPage.value = 1;
    } catch (error) {
      console.error('搜索帖子失败:', error);
      // 搜索失败，尝试从前端已加载的帖子中搜索
      try {
        const allPosts = await getAllPostsForSearch();
        const filtered = allPosts.filter((p: any) => {
          const title = (p.title || '').toLowerCase();
          const content = (p.content || p.summary || '').toLowerCase();

          return title.includes(keyword) || content.includes(keyword);
        });

        posts.value = mapPosts(filtered);
        totalCount.value = posts.value.length;
        // 搜索后重置到第一页
        currentPage.value = 1;
      } catch (e) {
        console.error('前端搜索也失败:', e);
        posts.value = [];
        totalCount.value = 0;
        currentPage.value = 1;
      }
    } finally {
      isSearching.value = false;
    }
  } else {
    // 搜索用户
    isSearching.value = true;
    try {
      // 通过搜索帖子来找到匹配的用户
      const res = await searchPosts({
        keyword: keyword,
        page: 1,
        pageSize: 100
      });

      const userMap = new Map<number, UserSearchResult>();

      if (res.data && res.data.list) {
        res.data.list.forEach((p: any) => {
          const userId = p.userId;
          const userInfo = p.user || {};
          const userName =
            p.authorName ||
            p.userName ||
            p.username ||
            p.user_name ||
            p.nickname ||
            p.nickName ||
            p.userNickname ||
            p.user_nickname ||
            userInfo.userName ||
            userInfo.username ||
            userInfo.user_name ||
            userInfo.nickname ||
            userInfo.nickName ||
            userInfo.userNickname ||
            userInfo.user_nickname ||
            '未知用户';
          const avatarUrl =
            p.userAvatar ||
            p.avatarUrl ||
            p.avatar ||
            userInfo.avatarUrl ||
            userInfo.userAvatar ||
            userInfo.avatar;
          const userNameLower = userName.toLowerCase();
          const userIdStr = String(userId || '');

          // 如果用户名或用户ID包含关键词
          if (userId && (userNameLower.includes(keyword) || userIdStr.includes(keyword))) {
            if (!userMap.has(userId)) {
              userMap.set(userId, {
                userId: userId,
                userName: userName,
                avatarInitial: userName[0] || '用',
                avatarUrl
              });
            }
          }
        });
      }

      // 如果后端搜索无结果，尝试从前端已加载的帖子中搜索
      if (userMap.size === 0) {
        const allPosts = await getAllPostsForSearch();
        allPosts.forEach((p: any) => {
          const userId = p.userId;
          const userName =
            p.authorName ||
            p.userName ||
            p.username ||
            p.user_name ||
            p.nickname ||
            p.nickName ||
            p.userNickname ||
            p.user_nickname ||
            '未知用户';
          const avatarUrl =
            p.userAvatar ||
            p.avatarUrl ||
            p.avatar ||
            (p.user && (p.user.avatarUrl || p.user.userAvatar || p.user.avatar));
          const userNameLower = (userName || '').toLowerCase();
          const userIdStr = String(userId || '');

          if (userId && (userNameLower.includes(keyword) || userIdStr.includes(keyword))) {
            if (!userMap.has(userId)) {
              userMap.set(userId, {
                userId: userId,
                userName: userName,
                avatarInitial: userName[0] || '用',
                avatarUrl
              });
            }
          }
        });
      }

      searchResults.value.users = Array.from(userMap.values());
    } catch (error) {
      console.error('搜索用户失败:', error);
      searchResults.value.users = [];
    } finally {
      isSearching.value = false;
    }
  }
};

// 获取所有帖子用于前端搜索（当后端搜索失败时）
const getAllPostsForSearch = async (): Promise<any[]> => {
  try {
    const res = await getPostList({ page: 1, pageSize: 1000 });
    let list: any[] = [];

    if (res.data) {
      if (Array.isArray(res.data)) {
        list = res.data;
      } else if (Array.isArray((res.data as any).list)) {
        list = (res.data as any).list;
      } else if (Array.isArray((res.data as any).records)) {
        list = (res.data as any).records;
      }
    }

    return list;
  } catch (error) {
    console.error('获取帖子列表失败:', error);
    return [];
  }
};

// 跳转到帖子详情，携带当前页码，便于返回时还原
const goToPostDetail = (postId: number) => {
  stopAllVideos(); // 导航前停止所有视频播放
  const targetPost = posts.value.find(p => p.id === postId);
  const likes = targetPost?.likes ?? 0;
  const comments = targetPost?.comments ?? 0;
  const views = targetPost?.views ?? 0;
  // 记录当前页，便于详情页“返回论坛”时恢复
  sessionStorage.setItem('forumLastPage', currentPage.value.toString());
  router.push({
    name: 'PostDetail',
    params: { id: postId.toString() },
    query: {
      fromPage: currentPage.value.toString(),
      likes: likes.toString(),
      comments: comments.toString(),
      views: views.toString(),
      liked: targetPost?.isLiked ? '1' : '0',
      title: targetPost?.title || '',
      content: targetPost?.content || '',
      images: targetPost?.images ? JSON.stringify(targetPost.images) : '',
      author: targetPost?.author || '',
      avatarUrl: targetPost?.avatarUrl || '',
      userId: targetPost?.userId?.toString() || ''
    }
  });
};

// 跳转到发布帖子页面
const goToPostCreation = () => {
  // 保存当前页码，以便发布成功后返回
  router.push({
    name: 'PostNew',
    query: { fromPage: currentPage.value.toString() }
  });
};

// 跳转到用户主页
const goToUserProfile = (userId: number) => {
  router.push({ name: 'UserProfile', params: { userId } });
};

// 分页导航函数
const handlePageClick = (page: number) => {
  goToPage(page);
};

const goToPage = async (page: number) => {
  if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
    await loadPosts(page);
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

const prevPage = () => {
  if (currentPage.value > 1) {
    goToPage(currentPage.value - 1);
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    goToPage(currentPage.value + 1);
  }
};

// 通过 userId 请求用户信息，补全昵称/头像，带缓存防重复
const ensureAuthorFromUser = async (userId: number) => {
  if (userNameCache.has(userId)) {
    const cachedName = userNameCache.get(userId);
    const cachedAvatar = userAvatarCache.get(userId);
    posts.value = posts.value.map(p => {
      if (p.userId === userId) {
        return {
          ...p,
          author: cachedName || p.author,
          avatarInitial: cachedName?.[0] || p.avatarInitial || '用',
          avatarUrl: p.avatarUrl || cachedAvatar
        };
      }
      return p;
    });
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
        posts.value = posts.value.map(p => {
          if (p.userId === userId) {
            return {
              ...p,
              author: name,
              avatarInitial: name[0] || p.avatarInitial || '用',
              avatarUrl: p.avatarUrl || avatar
            };
          }
          return p;
        });
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

// 验证跳转页码输入
// 处理页码跳转
const handleJumpPage = () => {
  if (jumpPageInput.value === null || jumpPageInput.value === undefined) {
    return;
  }

  let targetPage = jumpPageInput.value;

  // 自动修正超出范围的值
  if (targetPage < 1) {
    targetPage = 1;
  } else if (targetPage > totalPages.value) {
    targetPage = totalPages.value;
  }

  // 执行跳转
  if (targetPage >= 1 && targetPage <= totalPages.value) {
    goToPage(targetPage);
  }

  // 跳转后清空输入框
  jumpPageInput.value = null;
};

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  if (target) {
    target.style.display = 'none';
  }
};

// 头像加载失败时回退为首字
const handleAvatarError = (event: Event, post: Post) => {
  const target = event.target as HTMLImageElement;
  if (target) {
    target.style.display = 'none';
  }
  post.avatarUrl = undefined;
  if (!post.avatarInitial && post.author) {
    post.avatarInitial = post.author[0] || '用';
  }
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

// 点赞/取消点赞
const toggleLike = async (post: Post, event?: Event) => {
  // 阻止事件冒泡，避免触发卡片点击
  if (event) {
    event.stopPropagation();
  }

  const previousLiked = post.isLiked ?? false;
  const previousLikes = post.likes || 0;

  // 乐观更新UI
  post.isLiked = !previousLiked;
  if (post.isLiked) {
    // 如果之前未点赞，现在点赞，数量+1
    post.likes = (post.likes || 0) + 1;
  } else {
    // 如果之前已点赞，现在取消点赞，数量-1
    post.likes = Math.max(0, (post.likes || 0) - 1);
  }

  // 更新本地存储
  updateLikedPostsStorage(post.id, post.isLiked);

  try {
    await likePostApi(post.id);
  } catch (error: any) {
    // 回滚UI状态
    post.isLiked = previousLiked;
    post.likes = previousLikes;
    // 回滚本地存储
    updateLikedPostsStorage(post.id, previousLiked);
    console.error('点赞失败:', error);
  }
};

onBeforeUnmount(() => {
  stopAllVideos();
});

onDeactivated(() => {
  stopAllVideos();
});

onMounted(async () => {
  // 先加载当前用户信息
  await loadCurrentUser();

  // 检查URL参数中是否有页码，如果有则跳转到那一页
  const pageParam = route.query.page;
  if (pageParam && typeof pageParam === 'string') {
    const pageNum = parseInt(pageParam, 10);
    if (!isNaN(pageNum) && pageNum > 0) {
      await loadPosts(pageNum);
      return;
    }
  }

  loadPosts();

  // 监听详情页的更新事件，及时合并快照
  window.addEventListener('forum-post-updated', mergeSnapshotsIntoPosts);
});

onUnmounted(() => {
  window.removeEventListener('forum-post-updated', mergeSnapshotsIntoPosts);
});

// 从详情页返回时，尽量同步最新快照
onActivated(() => {
  mergeSnapshotsIntoPosts();
});

watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === '/forum' && oldPath && oldPath.startsWith('/forum/')) {
      mergeSnapshotsIntoPosts();
    }
  }
);
</script>

<style scoped>
:root {
  --primary-color: #FF8C00;
  --text-orange: #FF8C00;
  --bg-color: #F8F9FB;
  --card-bg: #FFFFFF;
  --text-main: #333333;
  --text-sub: #666666;
  --border-color: #e0e0e0;
  --radius: 12px;
  --shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.forum-main {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}

.welcome-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
  color: var(--text-main);
}

.page-subtitle {
  color: var(--text-sub);
  font-size: 14px;
  margin-bottom: 30px;
}

/* 搜索栏 */
.search-container {
  background: var(--card-bg);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 20px;
  margin-bottom: 30px;
}

.search-type-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-tab {
  padding: 8px 20px;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  background: white;
  color: var(--text-sub);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.search-tab:hover {
  border-color: #FF8C00;
  color: #FF8C00;
}

.search-tab.active {
  background: #FF8C00 !important;
  color: white !important;
  border-color: #FF8C00 !important;
}

.search-box {
  display: flex;
  gap: 10px;
  padding: 12px;
  border: 2px solid #FF8C00 !important;
  border-radius: 16px;
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  border: 1px solid var(--border-color);
  border-radius: 30px;
  font-size: 15px;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #FF8C00 !important;
  outline: none;
}

.search-btn {
  padding: 12px 25px;
  background: #FF8C00 !important;
  color: white !important;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  background: #E67A2A !important;
  transform: translateY(-2px);
}

/* 帖子列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  background: var(--card-bg);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 25px;
  transition: transform 0.3s;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-sub);
  font-size: 14px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
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
  font-size: 20px;
  font-weight: 700;
  color: #FF8C00 !important;
  line-height: 1.4;
}

.post-content {
  color: var(--text-main);
  margin-bottom: 15px;
  line-height: 1.7;
}

.post-content.clamped {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

/* 帖子图片和视频 */
.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  margin: 15px 0;
}

.post-image,
.post-media {
  width: 100%;
  height: 150px;
  border-radius: var(--radius);
  object-fit: cover;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
  overflow: hidden;
  position: relative;
}

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

.post-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-media .post-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-stats {
  display: flex;
  gap: 20px;
  color: var(--text-sub);
  font-size: 14px;
  border-top: 1px solid var(--border-color);
  padding-top: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
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

/* 发布帖子按钮 */
.new-post-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: #FF8C00 !important;
  color: white !important;
  border: none;
  border-radius: 30px;
  padding: 15px 25px;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 4px 20px rgba(255, 140, 66, 0.4);
  cursor: pointer;
  transition: all 0.3s;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 8px;
}

.new-post-btn:hover {
  background: #E67A2A !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(255, 140, 66, 0.5);
}

/* 用户搜索结果 */
.user-results {
  background: var(--card-bg);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 25px;
  margin-bottom: 30px;
}

.results-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: var(--text-main);
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-card:hover {
  background: #f0f0f0;
  transform: translateX(5px);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #FF8C00;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 4px;
}

.user-meta {
  font-size: 13px;
  color: var(--text-sub);
}

.no-results {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-sub);
  font-size: 16px;
}

/* 分页控件样式 */
.pagination-container {
  margin-top: 40px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.pagination-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 14px;
}

.pagination-btn:hover:not(.disabled) {
  background: #FF8C00;
  border-color: #FF8C00;
  color: white;
}

.pagination-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-pages {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-page-btn {
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.pagination-page-btn:hover:not(.disabled):not(.active) {
  background: #fff5e6;
  border-color: #FF8C00;
  color: #FF8C00;
}

.pagination-page-btn.active {
  background: #FF8C00;
  border-color: #FF8C00;
  color: white;
  font-weight: 600;
  outline: 2px solid #FF8C00 !important;
  box-shadow: 0 0 0 2px #FF8C00 !important;
}

.pagination-info {
  margin-left: 16px;
  color: #666;
  font-size: 14px;
  white-space: nowrap;
}

.pagination-jump {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
  padding-left: 16px;
  border-left: 1px solid #e0e0e0;
}

.jump-label {
  color: #666;
  font-size: 14px;
  white-space: nowrap;
}

.jump-input {
  width: 60px;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
  color: #333;
  transition: all 0.3s;
}

.jump-input:focus {
  outline: none;
  border-color: #FF8C00;
  box-shadow: 0 0 0 2px rgba(255, 140, 0, 0.1);
}

.jump-input::-webkit-inner-spin-button,
.jump-input::-webkit-outer-spin-button {
  opacity: 1;
  cursor: pointer;
}

.jump-btn {
  padding: 8px 16px;
  height: 40px;
  border: 1px solid #FF8C00;
  background: #FF8C00;
  color: white;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.jump-btn:hover {
  background: #E67A2A;
  border-color: #E67A2A;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(255, 140, 0, 0.3);
}

.jump-btn:active {
  transform: translateY(0);
}

/* 响应式 */
@media (max-width: 850px) {
  .new-post-btn {
    padding: 12px 20px;
    font-size: 14px;
    bottom: 20px;
    right: 20px;
  }

  .search-type-tabs {
    flex-wrap: wrap;
  }

  .search-tab {
    padding: 6px 16px;
    font-size: 13px;
  }

  .pagination-wrapper {
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 16px;
  }

  .pagination-info {
    width: 100%;
    text-align: center;
    margin-left: 0;
    margin-top: 8px;
  }

  .pagination-btn,
  .pagination-page-btn {
    width: 36px;
    height: 36px;
    font-size: 13px;
    min-width: 36px;
  }

  .pagination-jump {
    width: 100%;
    justify-content: center;
    margin-left: 0;
    margin-top: 12px;
    padding-left: 0;
    padding-top: 12px;
    border-left: none;
    border-top: 1px solid #e0e0e0;
  }

  .jump-input {
    width: 50px;
    height: 36px;
    font-size: 13px;
  }

  .jump-btn {
    height: 36px;
    padding: 6px 14px;
    font-size: 13px;
  }
}
</style>
