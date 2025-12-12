<template>
  <div class="min-h-screen" style="background-color: #F8F9FB;">
    <!-- 返回按钮 -->
    <div class="max-w-6xl mx-auto pt-6 px-5">
      <button 
        @click="router.back()" 
        class="flex items-center gap-2 text-[#FF8C00] hover:text-[#e6722a] transition-colors"
        title="返回"
      >
        <i class="fa-solid fa-arrow-left text-lg"></i>
        <span class="font-medium">返回</span>
      </button>
    </div>

    <!-- 主体布局 -->
    <div class="max-w-6xl mx-auto mt-4 mb-8 px-5 flex gap-8">
      <!-- 左侧侧边栏 -->
      <aside class="w-[260px] bg-white rounded-xl p-8 shadow-sm h-fit flex flex-col gap-8">
        <div class="flex items-center gap-4 pb-2.5">
          <div class="w-12 h-12 rounded-full flex items-center justify-center text-white overflow-hidden" style="background-color: #F3C697;">
            <img
              v-if="avatarUrl"
              :src="avatarUrl"
              alt="用户头像"
              class="w-full h-full object-cover"
            />
            <i v-else class="fa-regular fa-circle text-2xl"></i>
          </div>
          <div>
            <h3 class="text-base font-bold" style="color: #333333;">{{ userName }}</h3>
          </div>
        </div>

        <div class="flex flex-col gap-1">
          <div 
            v-for="item in menuItems" 
            :key="item.key"
            class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm cursor-pointer transition-colors"
            :class="[
              activeMenu === item.key 
                ? 'font-semibold' 
                : 'text-gray-600',
              item.isLogout ? 'text-red-500 mt-5' : '',
              activeMenu === item.key && !item.isLogout
                ? 'bg-[#FFE4CF] text-[#D97706]'
                : item.isLogout 
                  ? 'hover:bg-[#FEF2F2]'
                  : 'hover:bg-[#F3F4F6]'
            ]"
            @click="handleMenuClick(item)"
          >
            <i :class="item.icon" class="w-5 text-center"></i>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="flex-1">
        <component :is="currentComponent" />
      </main>
    </div>

    <!-- 退出登录确认弹窗 -->
    <ConfirmModal
      :visible="showLogoutConfirmModal"
      title="确认操作"
      message="确定要退出登录吗？"
      @confirm="confirmLogout"
      @cancel="closeLogoutConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ConfirmModal from '../../components/common/ConfirmModal.vue';
import MyHome from './UserCenter/MyHome.vue';
import MyPets from './UserCenter/MyPets.vue';
import MyTodo from './UserCenter/MyTodo.vue';
import MyPosts from './UserCenter/MyPosts.vue';
import MyApplications from './UserCenter/MyApplications.vue';
import BasicInfo from './UserCenter/BasicInfo.vue';
import CreditScore from './UserCenter/CreditScore.vue';
import { getCurrentUser, type CurrentUserInfo } from '../../api/userApi';
import { userLogout } from '../../api/authApi';

const route = useRoute();
const router = useRouter();

const userName = ref('用户');
const avatarUrl = ref<string | null>(null);

interface MenuItem {
  key: string;
  label: string;
  icon: string;
  component: any;
  isLogout?: boolean;
}

const menuItems: MenuItem[] = [
  { key: 'home', label: '我的主页', icon: 'fa-solid fa-house', component: MyHome },
  { key: 'pets', label: '我的宠物', icon: 'fa-solid fa-paw', component: MyPets },
  { key: 'todo', label: '我的待办', icon: 'fa-solid fa-list-check', component: MyTodo },
  { key: 'posts', label: '我的帖子', icon: 'fa-regular fa-file-lines', component: MyPosts },
  { key: 'applications', label: '我的申请', icon: 'fa-regular fa-paste', component: MyApplications },
  { key: 'basic-info', label: '基本信息', icon: 'fa-regular fa-address-card', component: BasicInfo },
  { key: 'credit-score', label: '信誉积分', icon: 'fa-regular fa-star', component: CreditScore },
  { key: 'logout', label: '退出登录', icon: 'fa-solid fa-arrow-right-from-bracket', component: null, isLogout: true }
];

const activeMenu = ref<string>((route.query.menu as string) || 'home');

const currentComponent = computed(() => {
  const item = menuItems.find(i => i.key === activeMenu.value);
  return item?.component || MyHome;
});

const showLogoutConfirmModal = ref(false);

function loadFromCache() {
  try {
    const cached = localStorage.getItem('currentUser');
    if (cached) {
      const data = JSON.parse(cached) as CurrentUserInfo;
      userName.value = data.userName || userName.value;
      avatarUrl.value = data.avatarUrl || avatarUrl.value;
    } else {
      const cachedName = localStorage.getItem('userName');
      const cachedAvatar = localStorage.getItem('avatarUrl');
      if (cachedName) userName.value = cachedName;
      if (cachedAvatar) avatarUrl.value = cachedAvatar;
    }
  } catch (error) {
    console.error('解析本地缓存用户信息失败(UserCenter)', error);
  }
}

async function loadUserFromApi() {
  try {
    const res = await getCurrentUser();
    if ((res.code === 0 || res.code === 200) && res.data) {
      const data = res.data;
      userName.value = data.userName || userName.value;
      avatarUrl.value = data.avatarUrl || avatarUrl.value;
      localStorage.setItem('currentUser', JSON.stringify(data));
      if (data.userName) localStorage.setItem('userName', data.userName);
      if (data.avatarUrl) localStorage.setItem('avatarUrl', data.avatarUrl);
    }
  } catch (error) {
    console.error('获取当前用户信息失败(UserCenter)', error);
  }
}

function handleUserUpdated() {
  loadFromCache();
}

function handleMenuClick(item: MenuItem) {
  if (item.isLogout) {
    showLogoutConfirmModal.value = true;
    return;
  }
  activeMenu.value = item.key;
  router.push({ path: '/user-center', query: { menu: item.key } });
}

async function confirmLogout() {
  try {
    await userLogout();
  } catch (error) {
    console.error('退出登录请求失败', error);
  } finally {
    // 清除本地存储的 token 和用户信息
    localStorage.removeItem('token');
    localStorage.removeItem('saTokenValue');
    localStorage.removeItem('bearerToken');
    localStorage.removeItem('Authorization');
    localStorage.removeItem('currentUser');
    localStorage.removeItem('userName');
    localStorage.removeItem('avatarUrl');
    
    // 跳转到登录页
    router.push('/login');
    showLogoutConfirmModal.value = false;
  }
}

function closeLogoutConfirm() {
  showLogoutConfirmModal.value = false;
}

// 监听路由变化
watch(() => route.query.menu, (newMenu) => {
  if (newMenu) {
    activeMenu.value = newMenu as string;
  }
}, { immediate: true });

onMounted(() => {
  loadFromCache();
  loadUserFromApi();
  window.addEventListener('current-user-updated', handleUserUpdated);
});

onUnmounted(() => {
  window.removeEventListener('current-user-updated', handleUserUpdated);
});
</script>

<style scoped>
/* Font Awesome 图标需要引入，这里假设已经全局引入 */
</style>

