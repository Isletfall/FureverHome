<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';
import NavBar from './components/common/NavBar.vue';
import GlobalLoading from './components/common/GlobalLoading.vue';
import NotificationToast from './components/common/NotificationToast.vue';
import { useNotification } from './composables/useNotification';

const route = useRoute();

// 判断是否为后台管理页面
const isAdminPage = computed(() => route.path.startsWith('/admin'));

// 登录/注册等认证页面不展示顶部导航栏
const isAuthPage = computed(() => {
  // 所有 /login 开头的路由 + 注册页，都视为认证页面
  if (route.path.startsWith('/login')) return true;
  return ['Register'].includes(route.name as string);
});

// 全局通知
const { showToast, toastTitle, toastContent, initWs, closeWs, hideToast } = useNotification();

// 检查登录状态的函数
const checkAndInitWs = () => {
  const token = 
    localStorage.getItem('saTokenValue') ||
    localStorage.getItem('bearerToken') ||
    localStorage.getItem('token') ||
    localStorage.getItem('Authorization');
  
  if (token) {
    // 延迟一下，确保 token 已保存
    setTimeout(() => {
      initWs();
    }, 500);
  }
};

onMounted(() => {
  // 初始化 WebSocket 连接（如果用户已登录）
  checkAndInitWs();
  
  // 监听路由变化，在非登录页面时尝试重新连接
  watch(() => route.path, (newPath) => {
    if (!newPath.startsWith('/login') && newPath !== '/register') {
      checkAndInitWs();
    }
  });
});

onBeforeUnmount(() => {
  // 关闭 WebSocket 连接
  closeWs();
});
</script>

<template>
  <div>
    <NavBar v-if="!isAdminPage && !isAuthPage" />
    <RouterView v-slot="{ Component }">
      <KeepAlive :include="['PetList', 'PostList']">
        <component :is="Component" />
      </KeepAlive>
    </RouterView>
    <!-- 全局请求加载提示，覆盖前台 + 后台所有页面 -->
    <GlobalLoading />
    <!-- 全局通知弹窗 -->
    <NotificationToast
      :visible="showToast"
      :title="toastTitle"
      :content="toastContent"
      @update:visible="(val) => { if (!val) hideToast() }"
      @close="hideToast"
    />
  </div>
</template>