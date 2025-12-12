<template>
  <div>
    <h2 class="text-2xl font-bold mb-5" style="color: #111;">我的信誉积分</h2>
    
    <!-- 积分概览卡片 -->
    <div class="bg-white rounded-xl p-6 shadow-sm mb-10">
      <div class="bg-gray-50 rounded-lg p-6 flex items-center gap-5">
        <span class="text-5xl font-extrabold leading-none" style="color: #E67E22;">
          {{ creditScore.toFixed(1) }}<span class="text-2xl font-medium" style="color: #9CA3AF;">/5</span>
          <span class="text-xs font-medium ml-2" style="color: #9CA3AF;">({{ creditScoreCount }}人评分)</span>
        </span>
        <!-- <div class="flex flex-col gap-1">
          <div class="text-lg font-bold" style="color: #111;">信誉良好</div>
          <div class="text-sm" style="color: #6B7280;">您的信誉积分很高，有助于提高领养成功率。</div>
        </div> -->
      </div>
    </div>

    <h2 class="text-2xl font-bold mb-5" style="color: #111;">他人评价</h2>
    
    <!-- 评价列表 -->
    <div v-if="reviews.length" class="flex flex-col gap-5">
      <div 
        v-for="review in reviews" 
        :key="review.id"
        class="bg-white rounded-xl p-6 shadow-sm flex flex-col gap-4"
      >
        <div class="flex justify-between items-start">
          <div class="flex items-center gap-3">
            <div 
              class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold overflow-hidden bg-[#F3C697]"
            >
              <img
                v-if="review.avatarUrl"
                :src="review.avatarUrl"
                alt="avatar"
                class="w-full h-full object-cover"
              />
              <span v-else>{{ review.name?.charAt(0) || '用' }}</span>
            </div>
            <div class="flex flex-col">
              <span 
                class="text-base font-bold cursor-pointer transition-colors hover:text-[#FF8C42]" 
                style="color: #111;"
                @click="router.push({ name: 'UserProfile', params: { userId: review.nameId || getUserIdByName(review.name) } })"
              >
                {{ review.name }}
              </span>
              <span class="text-xs" style="color: #9CA3AF;">{{ review.time }}</span>
            </div>
          </div>
          <div class="text-sm" style="color: #F59E0B; letter-spacing: 2px;">
            <i 
              v-for="i in 5" 
              :key="i"
              :class="i <= review.stars ? 'fa-solid fa-star' : 'fa-regular fa-star'"
            ></i>
          </div>
        </div>
        <div class="text-sm leading-relaxed" style="color: #4B5563;">
          {{ review.content }}
        </div>
      </div>
    </div>

    <div v-else class="mt-8 text-center text-sm" style="color: #9CA3AF;">
      当前还没有收到评价
    </div>

    <!-- 分页：统一样式，列表为空也显示，至少一页 -->
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getReceivedRatings } from '@/api/ratingApi';
import { getCurrentUser } from '@/api/userApi';

const normalizeImageUrl = (url?: string | null): string | undefined => {
  if (!url) return undefined;
  const trimmed = String(url).trim();
  if (!trimmed) return undefined;
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) return trimmed;
  if (trimmed.startsWith('/api/')) return trimmed;
  return `/api/storage/image/${trimmed.replace(/^\/+/, '')}`;
};

const router = useRouter();

interface Review {
  id: number;
  name: string;
  nameId?: number;
  time: string;
  stars: number;
  content: string;
  avatarUrl?: string;
}

const reviews = ref<Review[]>([]);
const currentPage = ref(1);
// 每页 3 条评价
const pageSize = 3;
const total = ref(0);
const creditScore = ref(0); // 信誉积分
const creditScoreCount = ref(0); // 评分人数

const totalPages = computed(() => {
  return Math.max(1, Math.ceil((total.value || 0) / pageSize));
});

async function loadReviews(page = 1) {
  try {
    const res = await getReceivedRatings({ page, pageSize });
    if (res.code === 200 && res.data) {
      total.value = res.data.total ?? 0;
      const records = res.data.records ?? [];
      reviews.value = records.map((item: any, index: number) => {
        const id = item.ratingId ?? index + 1;
        const name = item.otherUserName || '匿名用户';
        const nameId = item.otherUserId;
        const time = item.createTime || '';
        const stars = item.score ?? 0;
        const content = item.content || '';
        const avatarUrl = normalizeImageUrl(item.otherUserAvatar);
        return {
          id,
          name,
          nameId,
          time,
          stars,
          content,
          avatarUrl
        } as Review;
      });
    } else {
      console.error('获取评价列表失败', res);
    }
  } catch (err) {
    console.error('获取评价列表接口异常', err);
  }
}

async function loadCreditScore() {
  try {
    const res = await getCurrentUser();
    if ((res.code === 0 || res.code === 200) && res.data) {
      creditScore.value = res.data.creditScore ?? 0;
      creditScoreCount.value = res.data.creditScoreCount ?? 0;
    }
  } catch (err) {
    console.error('获取信誉积分失败', err);
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

function goPage(page: number) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  loadReviews(page);
}

// 根据用户名获取用户ID（这里应该从API获取，暂时使用映射）
function getUserIdByName(name: string): number {
  const nameToIdMap: Record<string, number> = {
    '张伟': 2,
    '李静': 4,
    '王强': 5,
    '张同学': 2,
    '王老师': 3,
    '刘同学': 4,
    '陈学姐': 5,
    '赵同学': 6,
    '孙老师': 7,
    '周同学': 8,
    '吴同学': 9,
    '郑同学': 10,
    '钱老师': 11,
    '李同学': 1
  };
  return nameToIdMap[name] || 1;
}

onMounted(() => {
  loadReviews(currentPage.value);
  loadCreditScore();
});
</script>

<style scoped>
</style>

