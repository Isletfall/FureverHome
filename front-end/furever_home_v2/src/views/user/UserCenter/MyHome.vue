<template>
  <div>
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2" style="color: #333333;">欢迎回来，{{ userName }}！</h1>
      <p class="text-sm" style="color: #666666;">这是您的个人简报。</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-3 gap-5 mb-10">
      <div class="bg-white p-6 rounded-xl shadow-sm flex flex-col justify-between h-[120px]">
        <span class="text-sm font-medium" style="color: #6B7280;">我的帖子（已通过审核）</span>
        <span class="text-4xl font-bold mt-2.5" style="color: #111;">{{ stats.postCount }}</span>
      </div>
      <div class="bg-white p-6 rounded-xl shadow-sm flex flex-col justify-between h-[120px]">
        <span class="text-sm font-medium" style="color: #6B7280;">我的短期宠物（已通过审核）</span>
        <span class="text-4xl font-bold mt-2.5" style="color: #111;">{{ stats.shortTermPetCount }}</span>
      </div>
      <div class="bg-white p-6 rounded-xl shadow-sm flex flex-col justify-between h-[120px]">
        <span class="text-sm font-medium" style="color: #6B7280;">我的长期宠物（已通过审核）</span>
        <span class="text-4xl font-bold mt-2.5" style="color: #111;">{{ stats.longTermPetCount }}</span>
      </div>
      <div class="bg-white p-6 rounded-xl shadow-sm flex flex-col justify-between h-[120px]">
        <span class="text-sm font-medium" style="color: #6B7280;">我的申请</span>
        <span class="text-4xl font-bold mt-2.5" style="color: #111;">{{ stats.myApplicationsCount }}</span>
      </div>
      <div class="bg-white p-6 rounded-xl shadow-sm flex flex-col justify-between h-30">
        <span class="text-sm font-medium" style="color: #6B7280;">我的待办</span>
        <span class="text-4xl font-bold mt-2.5" style="color: #111;">{{ stats.myTodosCount }}</span>
      </div>
      <div class="bg-white p-6 rounded-xl shadow-sm flex flex-col justify-between h-30">
        <span class="text-sm font-medium" style="color: #6B7280;">信誉积分</span>
        <span class="text-4xl font-bold mt-2.5" style="color: #111;">{{ stats.ratingAverage }}/5</span>
        <span class="text-xs font-medium ml-2" style="color: #9CA3AF;">({{ stats.ratingCount }}人评分)</span>
      </div>
    </div>

    <!-- 后台通知 -->
    <div class="text-xl font-bold mb-5" style="color: #333333;">后台通知</div>
    <div class="flex flex-col gap-4">
      <div
        v-for="item in pagedNotifications"
        :key="item.id"
        class="bg-white p-5 rounded-xl shadow-sm flex items-center gap-5"
      >
        <div
          class="w-12 h-12 rounded-full flex items-center justify-center text-xl flex-shrink-0"
          :style="{ backgroundColor: item.iconBg, color: item.iconColor }"
        >
          <i :class="item.icon"></i>
        </div>
        <div class="flex-1">
          <div class="text-base font-medium mb-1" style="color: #1F2937;">{{ item.title }}</div>
          <div class="text-sm" style="color: #4B5563;">{{ item.content }}</div>
          <div class="text-xs mt-1" style="color: #9CA3AF;">{{ item.time }}</div>
        </div>
      </div>
      <div v-if="!pagedNotifications.length" class="text-center text-sm text-gray-400 py-4">暂无通知</div>
    </div>
    <div class="flex justify-center mt-6 gap-2 items-center">
      <button
        class="px-3 py-1.5 rounded-lg border border-gray-300 bg-white text-sm hover:border-[#FF8C42] hover:text-[#FF8C42] disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        :disabled="currentPage === 1"
        @click="prevPage"
      >
        上一页
      </button>
      
      <button
        v-for="(page, index) in getDisplayedPages(currentPage, totalPages)"
        :key="index"
        :class="[
          'px-3 py-1.5 border rounded-lg transition-all text-sm',
          page === currentPage
            ? 'bg-[#FF8C42] text-white border-[#FF8C42]'
            : 'border-gray-300 bg-white text-gray-600',
          typeof page === 'string'
            ? 'cursor-default border-transparent'
            : 'cursor-pointer hover:border-[#FF8C42] hover:text-[#FF8C42]'
        ]"
        @click="typeof page === 'number' && goToPage(page)"
      >
        {{ page }}
      </button>

      <button
        class="px-3 py-1.5 rounded-lg border border-gray-300 bg-white text-sm hover:border-[#FF8C42] hover:text-[#FF8C42] disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        :disabled="currentPage >= totalPages"
        @click="nextPage"
      >
        下一页
      </button>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { getCurrentUser, getCurrentUserStats, type CurrentUserInfo, type CurrentUserStats } from '../../../api/userApi';
import { getMyNotifications } from '../../../api/notifyApi';
import { formatDateTime } from '@/utils/format';

const userName = ref('用户');
const stats = ref<Required<CurrentUserStats>>({
  postCount: 0,
  shortTermPetCount: 0,
  longTermPetCount: 0,
  myApplicationsCount: 0,
  myTodosCount: 0,
  ratingAverage: 0,
  ratingCount: 0
});
const creditDisplay = ref('0/5');

interface NotificationItem {
  id: number | string;
  title: string;
  content: string;
  time: string;
  icon: string;
  iconBg: string;
  iconColor: string;
}

const notifications = ref<NotificationItem[]>([]);
const pageSize = 3;
const currentPage = ref(1);
const total = ref(0);
const totalPages = computed(() => Math.max(1, Math.ceil((total.value || 0) / pageSize)));
const pagedNotifications = computed(() => notifications.value);

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

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    loadNotifications(page)
  }
}

async function loadCurrentUser() {
  try {
    const res = await getCurrentUser();
    if ((res.code === 0 || res.code === 200) && res.data) {
      const data: CurrentUserInfo = res.data;
      userName.value = data.userName || userName.value;
    } else {
      const cachedName = localStorage.getItem('userName');
      if (cachedName) userName.value = cachedName;
    }
  } catch (error) {
    const cachedName = localStorage.getItem('userName');
    if (cachedName) userName.value = cachedName;
    console.error('获取当前用户信息失败（MyHome）', error);
  }
}

async function loadStats() {
  try {
    const res = await getCurrentUserStats();
    if ((res.code === 0 || res.code === 200) && res.data) {
      const data = res.data;
      stats.value = {
        postCount: data.postCount ?? 0,
        shortTermPetCount: data.shortTermPetCount ?? 0,
        longTermPetCount: data.longTermPetCount ?? 0,
        myApplicationsCount: data.myApplicationsCount ?? 0,
        myTodosCount: data.myTodosCount ?? 0,
        ratingAverage: data.ratingAverage ?? 0,
        ratingCount: data.ratingCount ?? 0
      };
      creditDisplay.value = `${stats.value.ratingAverage ?? 0}/5`;
    }
  } catch (error) {
    console.error('获取用户统计数据失败（MyHome）', error);
  }
}

const normalizeTokenValue = (value?: string | null) => {
  if (!value) return '';
  return value.startsWith('Bearer ') ? value.slice(7) : value;
};

const getStoredToken = () => {
  const raw =
    localStorage.getItem('saTokenValue') ||
    localStorage.getItem('bearerToken') ||
    localStorage.getItem('token') ||
    localStorage.getItem('Authorization');
  return normalizeTokenValue(raw);
};

const wsRef = ref<WebSocket | null>(null);

// 根据通知数据生成消息内容
function generateNotificationContent(item: any): string {
  const targetType = item.targetType || '';
  const event = item.event || '';
  
  // 辅助函数：尝试获取宠物名称
  const getPetName = (data: any) => data.animalName || data.petName || data.targetName || '未知宠物';
  // 辅助函数：尝试获取帖子标题
  const getPostTitle = (data: any) => data.postTitle || data.title || '未知标题';
  
  if (targetType === 'animal') {
    const animalName = getPetName(item);
    return `您的名为"${animalName}"的宠物的发布或修改已被管理员${event}`;
  } else if (targetType === 'post') {
    const postTitle = getPostTitle(item);
    if (event === '通过') {
      return `您的标题为"${postTitle}"的帖子已被管理员通过，已成功发布`;
    } else if (event === '评论') {
      return `您的标题为"${postTitle}"的帖子收到了新的评论`;
    } else if (event === '回复') {
      return `您在标题为"${postTitle}"的帖子下的评论已被回复`;
    } else if (event === '删除') {
      return `您的标题为"${postTitle}"的帖子已被管理员删除`;
    }
    return `您的标题为"${postTitle}"的帖子的发布或修改已被管理员${event}`;
  } else if (targetType === 'adopt') {
    const animalName = getPetName(item);
    if (event === '通过') {
      return `您对名为"${animalName}"的宠物的申请已被管理员通过，已成功发送至被申请者处`;
    } else if (event === '拒绝') {
      return `您对名为"${animalName}"的宠物的申请已被管理员拒绝，请重新申请`;
    } else if (event === '申请成功') {
      return `您对名为"${animalName}"的宠物的申请已被对方同意`;
    } else if (event === '申请失败') {
      return `您对名为"${animalName}"的宠物的申请已被对方拒绝，请重新申请`;
    }
  } else if (targetType === 'review') {
    const animalName = getPetName(item);
    if (event === '新的待办事项') {
      return `您的名为"${animalName}"的宠物正在被用户"${item.applicantUserName}"申请，请及时处理`;
    }
  }
  
  // 默认情况：使用原有的content或event
  // 如果没有足够的信息，返回空字符串
  return item.content || item.extraInfo || event || '';
}

const resolveWsUrl = (base?: string) => {
  if (!base) return '';
  if (base.startsWith('ws://') || base.startsWith('wss://')) return base;
  const origin = window.location.origin.replace(/^http/, 'ws');
  return `${origin}${base}`;
};

const handleWsMessage = (event: MessageEvent) => {
  try {
    if (event.data === 'ping' || event.data === 'pong' || event.data === 'heartbeat') {
      return;
    }

    const payload = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
    const data = payload?.data || payload;

    // 过滤无效数据或空数据
    if (!data || Object.keys(data).length === 0) {
      return;
    }

    // 过滤心跳包
    if (data.type === 'heartbeat' || data.type === 'ping') {
      return;
    }

    const title = data?.title || '系统通知';
    const content = generateNotificationContent(data);
    
    // 如果没有有效内容，忽略该消息
    if (!content) return;

    const time = formatDateTime(data?.createTime || data?.time || new Date().toISOString());
    const newItem: NotificationItem = {
      id: data?.activityId ?? data?.id ?? Date.now(),
      title,
      content,
      time,
      icon: 'fa-solid fa-bell',
      iconBg: '#E5F3FF',
      iconColor: '#2563EB'
    };

    // 简单去重：如果列表中已存在相同ID的消息，则忽略
    if (notifications.value.some(n => n.id == newItem.id)) {
      return;
    }

    // 刷新第一页数据，让最新通知可见
    notifications.value = [newItem, ...notifications.value].slice(0, pageSize);
    total.value += 1;
    currentPage.value = 1;
    // 通知弹窗已移至全局，这里不再显示
  } catch (err) {
    // console.error('解析通知消息失败', err, event.data);
  }
};

const initWs = () => {
  const base = import.meta.env.VITE_CHAT_WS_URL || '/api/ws/common';
  const token = getStoredToken();
  let url = resolveWsUrl(base);
  if (token) {
    url += url.includes('?') ? `&token=${encodeURIComponent(token)}` : `?token=${encodeURIComponent(token)}`;
  }
  try {
    const ws = new WebSocket(url);
    wsRef.value = ws;
    ws.onmessage = handleWsMessage;
    ws.onerror = err => {
      console.error('通知WebSocket错误', err);
    };
  } catch (err) {
    console.error('通知WebSocket连接失败', err);
  }
};

async function loadNotifications(page = 1) {
  try {
    const res = await getMyNotifications({ page, pageSize, onlyUnread: false });
    if ((res.code === 0 || res.code === 200) && res.data) {
      const records = res.data.records || [];
      total.value = res.data.total ?? records.length ?? 0;
      notifications.value = records.map(item => ({
        id: item.activityId ?? item.targetId ?? Date.now(),
        title: item.targetType + '·' + item.event || '系统通知',
        content: generateNotificationContent(item),
        time: formatDateTime(item.createTime || new Date().toISOString()),
        icon: 'fa-solid fa-bell',
        iconBg: '#E5F3FF',
        iconColor: '#2563EB'
      }));
      currentPage.value = page;
    }
  } catch (error) {
    console.error('获取通知列表失败', error);
  }
}

function prevPage() {
  if (currentPage.value > 1) {
    loadNotifications(currentPage.value - 1);
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    loadNotifications(currentPage.value + 1);
  }
}

onMounted(() => {
  loadCurrentUser();
  loadStats();
  loadNotifications();
  initWs();
});

onBeforeUnmount(() => {
  if (wsRef.value) {
    wsRef.value.close();
    wsRef.value = null;
  }
});
</script>

<style scoped>
</style>

