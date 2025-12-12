<template>
  <div class="relative flex min-h-screen w-full flex-col group/design-root overflow-x-hidden font-display bg-background-light dark:bg-background-dark">
    <div class="flex h-full grow flex-row">
      <!-- 左侧边栏 -->
      <aside class="flex h-screen w-64 flex-col bg-[#0D1117] p-4 text-white sticky top-0">
        <div class="flex flex-1 flex-col gap-y-6">
          <div class="flex gap-4 items-center pl-2">
            <img src="@/assets/icons/logo.svg" alt="Logo" class="size-10 rounded-md bg-white" />
            <h1 class="text-white text-base font-bold leading-normal">管理后台</h1>
          </div>
          <nav class="flex flex-col gap-2 mt-4">
            <router-link 
              :to="{ name: 'AdminDashboard' }"
              class="flex items-center gap-4 px-4 py-2.5 rounded-lg transition-colors"
              :class="isActive('AdminDashboard') ? 'bg-[#2563EB] text-white' : 'text-gray-400 hover:bg-white/10 hover:text-white'"
            >
              <span class="material-symbols-outlined" style="font-size: 24px;">grid_view</span>
              <p class="text-sm font-medium leading-normal">首页</p>
            </router-link>
            <router-link 
              :to="{ name: 'AdminPosts' }"
              class="flex items-center gap-4 px-4 py-2.5 rounded-lg transition-colors"
              :class="isActive('AdminPosts') ? 'bg-[#2563EB] text-white' : 'text-gray-400 hover:bg-white/10 hover:text-white'"
            >
              <span class="material-symbols-outlined" style="font-size: 24px;">article</span>
              <p class="text-sm font-medium leading-normal">帖子管理</p>
            </router-link>
            <router-link 
              :to="{ name: 'AdminPets' }"
              class="flex items-center gap-4 px-4 py-2.5 rounded-lg transition-colors"
              :class="isActive('AdminPets') ? 'bg-[#2563EB] text-white' : 'text-gray-400 hover:bg-white/10 hover:text-white'"
            >
              <span class="material-symbols-outlined" style="font-size: 24px;">pets</span>
              <p class="text-sm font-medium leading-normal">宠物管理</p>
            </router-link>
            <router-link 
              :to="{ name: 'AdminApplications' }"
              class="flex items-center gap-4 px-4 py-2.5 rounded-lg transition-colors"
              :class="isActive('AdminApplications') ? 'bg-[#2563EB] text-white' : 'text-gray-400 hover:bg-white/10 hover:text-white'"
            >
              <span class="material-symbols-outlined" style="font-size: 24px;">assignment</span>
              <p class="text-sm font-medium leading-normal">申请管理</p>
            </router-link>
          </nav>
        </div>
        <div class="mt-auto flex flex-col">
          <a 
            class="flex items-center gap-4 px-4 py-2.5 rounded-lg text-gray-400 hover:bg-white/10 hover:text-white transition-colors cursor-pointer"
            @click="handleLogout"
          >
            <span class="material-symbols-outlined" style="font-size: 24px;">logout</span>
            <p class="text-sm font-medium leading-normal">退出登录</p>
          </a>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="flex-1 p-8">
        <router-view />
      </main>
    </div>

    <!-- 退出登录确认弹窗 -->
    <ConfirmModal
      :visible="showLogoutConfirmModal"
      title="确认退出登录"
      message="确定要退出登录吗？"
      @confirm="confirmLogout"
      @cancel="showLogoutConfirmModal = false"
      @close="showLogoutConfirmModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ConfirmModal from '../../components/admin/ConfirmModal.vue';

const route = useRoute();
const router = useRouter();

const showLogoutConfirmModal = ref(false);

function isActive(routeName: string): boolean {
  return route.name === routeName;
}

function handleLogout() {
  showLogoutConfirmModal.value = true;
}

function confirmLogout() {
  // TODO: 实现退出登录逻辑
  // 清除 token 等登录信息
  localStorage.removeItem('bearerToken');
  localStorage.removeItem('saTokenName');
  localStorage.removeItem('saTokenValue');
  localStorage.removeItem('roles');
  localStorage.removeItem('currentUser');
  
  showLogoutConfirmModal.value = false;
  router.push('/');
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;900&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined');

.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 24;
}
</style>

