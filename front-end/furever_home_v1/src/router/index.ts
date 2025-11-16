import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import Home from '@/views/home/Home.vue';
import PetList from '@/views/pet/PetList.vue';
import Talk from '@/views/talk/Talk.vue';
import PostList from '@/views/forum/PostList.vue';
import Profile from '@/views/user/Profile.vue';
import NotFound from '@/views/error/404.vue';

// 定义路由类型
const routes: RouteRecordRaw[] = [
  {
    path: '/home',
    name: 'home',
    component: Home,
    meta: { title: '首页 - FUREVER HOME' }
  },
  // 根路径重定向到首页，确保新标签页打开 / 时不空白
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/user/profile',
    name: 'userProfile',
    component: Profile,
    meta: { 
      title: '个人主页 - FUREVER HOME',
      hideNav: true
    }
  },
  {
    path: '/petList',
    name: 'petList',
    component: PetList,
    meta: {
      title: '宠物列表 - FUREVER HOME'
    }
  },
  {
    path: '/forum',
    name: 'forum',
    component: PostList,
    meta: {
      title: '宠物论坛 - FUREVER HOME'
    }
  },
  {
    path: '/talk',
    name: 'talk',
    component: Talk,
    meta: {
      title: '沟通对接 - FUREVER HOME'
    }
  },
  // 添加404路由
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: NotFound,
    meta: { title: '页面未找到 - FUREVER HOME' }
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // 添加滚动行为
  scrollBehavior() {
    return { top: 0 };
  }
});

// 全局前置守卫，设置页面标题
router.beforeEach((to, _from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title as string;
  }
  next();
});

export default router;