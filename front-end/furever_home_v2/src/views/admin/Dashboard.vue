<template>
  <div class="flex flex-col gap-8">
    <header class="flex flex-wrap justify-between gap-3">
      <h1 class="text-[#111318] dark:text-white text-4xl font-black leading-tight tracking-[-0.033em] min-w-72">工作台</h1>
    </header>

    <!-- 第一行：总统计 -->
    <section class="grid grid-cols-1 sm:grid-cols-3 gap-6">
      <div 
        class="group flex flex-col gap-2 rounded-xl p-6 bg-[#3498DB] text-white shadow-md hover:shadow-lg transition-shadow cursor-pointer"
        @click="router.push({ name: 'AdminPosts', query: { tab: 'published' } })"
      >
        <div class="flex justify-between items-center">
          <p class="text-base font-medium leading-normal">已发布帖子总量</p>
          <span class="material-symbols-outlined">article</span>
        </div>
        <p class="tracking-light text-4xl font-bold leading-tight">{{ stats.totalPosts }}</p>
        <p class="text-sm text-blue-100">总发布帖子</p>
      </div>
      <div 
        class="group flex flex-col gap-2 rounded-xl p-6 bg-[#2ECC71] text-white shadow-md hover:shadow-lg transition-shadow cursor-pointer"
        @click="router.push({ name: 'AdminPets', query: { tab: 'longTerm' } })"
      >
        <div class="flex justify-between items-center">
          <p class="text-base font-medium leading-normal">长期宠物总量</p>
          <span class="material-symbols-outlined">pets</span>
        </div>
        <p class="tracking-light text-4xl font-bold leading-tight">{{ stats.totalLongTermPets }}</p>
        <p class="text-sm text-green-100">长期领养宠物</p>
      </div>
      <div 
        class="group flex flex-col gap-2 rounded-xl p-6 bg-[#E67E22] text-white shadow-md hover:shadow-lg transition-shadow cursor-pointer"
        @click="router.push({ name: 'AdminPets', query: { tab: 'shortTerm' } })"
      >
        <div class="flex justify-between items-center">
          <p class="text-base font-medium leading-normal">短期宠物总量</p>
          <span class="material-symbols-outlined">pets</span>
        </div>
        <p class="tracking-light text-4xl font-bold leading-tight">{{ stats.totalShortTermPets }}</p>
        <p class="text-sm text-orange-100">短期寄养宠物</p>
      </div>
    </section>

    <!-- 第二行：待审核统计 -->
    <section class="grid grid-cols-1 sm:grid-cols-3 gap-6">
      <div 
        class="group flex flex-col gap-2 rounded-xl p-6 bg-[#3498DB] text-white shadow-md hover:shadow-lg transition-shadow cursor-pointer"
        @click="router.push({ name: 'AdminPosts', query: { tab: 'pending' } })"
      >
        <div class="flex justify-between items-center">
          <p class="text-base font-medium leading-normal">待审核的帖子</p>
          <span class="material-symbols-outlined">article</span>
        </div>
        <p class="tracking-light text-4xl font-bold leading-tight">{{ stats.pendingPosts }}</p>
        <p class="text-sm text-blue-100">点击处理</p>
      </div>
      <div 
        class="group flex flex-col gap-2 rounded-xl p-6 bg-[#2ECC71] text-white shadow-md hover:shadow-lg transition-shadow cursor-pointer"
        @click="router.push({ name: 'AdminPets', query: { tab: 'pending' } })"
      >
        <div class="flex justify-between items-center">
          <p class="text-base font-medium leading-normal">待审核的宠物</p>
          <span class="material-symbols-outlined">pets</span>
        </div>
        <p class="tracking-light text-4xl font-bold leading-tight">{{ stats.pendingPets }}</p>
        <p class="text-sm text-green-100">点击处理</p>
      </div>
      <div 
        class="group flex flex-col gap-2 rounded-xl p-6 bg-[#E74C3C] text-white shadow-md hover:shadow-lg transition-shadow cursor-pointer"
        @click="router.push({ name: 'AdminApplications' })"
      >
        <div class="flex justify-between items-center">
          <p class="text-base font-medium leading-normal">待审核的申请</p>
          <span class="material-symbols-outlined">assignment</span>
        </div>
        <p class="tracking-light text-4xl font-bold leading-tight">{{ stats.pendingApplications }}</p>
        <p class="text-sm text-red-100">点击处理</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getDashboardStatistics, type DashboardStatisticsDto } from '../../api/adminApi';

const router = useRouter();

interface Stats {
  totalPosts: number;
  totalLongTermPets: number;
  totalShortTermPets: number;
  pendingPosts: number;
  pendingPets: number;
  pendingApplications: number;
}

const stats = ref<Stats>({
  totalPosts: 0,
  totalLongTermPets: 0,
  totalShortTermPets: 0,
  pendingPosts: 0,
  pendingPets: 0,
  pendingApplications: 0
});

onMounted(async () => {
  try {
    const res = await getDashboardStatistics();
    if (res && (res.code === 0 || res.code === 200) && res.data) {
      const data: DashboardStatisticsDto = res.data;
      stats.value = {
        totalPosts: data.totalPostCount ?? 0,
        totalLongTermPets: data.longTermPetCount ?? 0,
        totalShortTermPets: data.shortTermPetCount ?? 0,
        pendingPosts: data.pendingPostCount ?? 0,
        pendingPets: data.pendingPetCount ?? 0,
        pendingApplications: data.pendingAdoptCount ?? 0
      };
    } else {
      console.warn('获取首页统计数据失败', res);
    }
  } catch (error) {
    console.error('获取首页统计数据异常', error);
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;900&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 24;
}
</style>

