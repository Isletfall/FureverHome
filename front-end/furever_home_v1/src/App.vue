<!-- 定义Vue组件的模板部分 -->
<template>
  <!-- 根容器，设置基本样式：背景色、字体和文本颜色，支持深色模式 -->
  <div class="bg-background-light dark:bg-background-dark font-display text-gray-700 dark:text-gray-300 min-h-screen">
    <!-- 仅非隐藏布局时显示导航 -->
    <header v-if="!hideChrome" class="sticky top-0 z-50 bg-white/95 backdrop-blur border-b border-gray-100 shadow-sm">
      <nav class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <span class="material-icons text-primary text-3xl">pets</span>
          <span class="text-xl font-bold tracking-wide text-gray-800">FUREVER HOME</span>
        </div>
        <div class="hidden md:flex items-center space-x-6 text-sm font-medium">
          <RouterLink to="/home" class="text-gray-600 hover:text-primary transition-colors relative">首页</RouterLink>
          <RouterLink to="/petList" class="text-gray-600 hover:text-primary transition-colors relative">宠物列表</RouterLink>
          <RouterLink to="/forum" class="text-gray-600 hover:text-primary transition-colors relative">宠物论坛</RouterLink>
          <RouterLink to="/talk" class="text-gray-600 hover:text-primary transition-colors relative">沟通对接</RouterLink>
        </div>
        <button type="button" @click="openProfile" class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium text-gray-600 hover:text-primary hover:bg-gray-50 transition-colors">
          <span class="material-icons text-lg">account_circle</span>
          <span class="hidden sm:inline">用户</span>
        </button>
      </nav>
    </header>
    <!-- 主内容：Profile 全屏占位（去除内边距与限制） -->
    <main :class="hideChrome ? 'min-h-screen' : 'container mx-auto px-6 py-8'">
      <RouterView />
    </main>
    <!-- 非隐藏布局显示页脚 -->
    <footer v-if="!hideChrome" class="bg-slate-800 dark:bg-zinc-950 text-slate-300 mt-12">
      <div class="container mx-auto px-6 py-12">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div>
            <h3 class="text-lg font-semibold text-white">数据统计</h3>
            <p class="mt-2 text-sm max-w-md">我们与多家救助站建立了长期合作关系，致力于为流浪动物提供更好的临时安置和长期领养服务。</p>
            <RouterLink class="mt-4 inline-block text-primary hover:text-orange-300 transition-colors text-sm" to="/shelters">查看合作救助站 →</RouterLink>
          </div>
          <div>
            <h3 class="text-lg font-semibold text-white">联系开发团队</h3>
            <p class="mt-2 text-sm max-w-md">如果您有任何问题、建议或合作意向，请随时联系我们的开发团队。</p>
            <div class="mt-4 flex space-x-4">
              <RouterLink class="text-2xl hover:text-white" to="/chat">💬</RouterLink>
              <RouterLink class="text-2xl hover:text-white" to="/contact">💌</RouterLink>
              <RouterLink class="text-2xl hover:text-white" to="/about">🌐</RouterLink>
            </div>
          </div>
        </div>
        <div class="border-t border-slate-700 dark:border-zinc-800 mt-8 pt-6 text-center text-sm text-slate-400">
          <p>© 2025 FUREVERHOME流浪动物领养平台 - 让每个生命都有温暖的家</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<!-- Vue组件的脚本部分，使用TypeScript -->
<script setup lang="ts">
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router';
import { computed } from 'vue';

const router = useRouter();
const route = useRoute();

const hideChrome = computed(() => route.meta.hideNav === true);

function openProfile() {
  const r = router.resolve('/user/profile');
  window.open(r.href, '_blank', 'noopener');
}
</script>

<!-- Vue组件的样式部分，使用scoped作用域 -->
<style scoped>
  /* 移除内联样式，统一使用Tailwind类 */
  /* Material Icons字体已在main.css中导入 */

  /* 为RouterLink组件添加与a标签相同的hover效果 */
  .router-link-active {
    color: rgb(146, 37, 37);
  }

  .router-link-exact-active {
    font-weight: 500;
  }

/* CDN Tailwind 不支持 @apply，这里不使用辅助类 */
</style>