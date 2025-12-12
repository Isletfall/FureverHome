<template>
  <RouterLink
    :to="{ name: 'PostDetail', params: { id: post.id } }"
    class="post-card group"
  >
    <div class="post-header">
      <div class="post-meta">
        <div class="post-author">
          <div class="author-avatar bg-orange-300 text-white font-semibold flex items-center justify-center rounded-full w-8 h-8 text-sm">
            {{ post.avatarInitial }}
          </div>
          <span class="text-gray-700 font-medium">{{ post.author }}</span>
        </div>
        <span class="text-gray-500 text-sm">· {{ post.timeAgo }}</span>
      </div>
      <h2 class="post-title text-xl font-bold mt-2 text-gray-900 group-hover:text-orange-600 transition duration-300">
        {{ post.title }}
      </h2>
    </div>

    <p class="post-content text-gray-600 mb-4 line-clamp-2">
      {{ post.content }}
    </p>

    <div v-if="post.images && post.images.length" class="post-images grid grid-cols-3 gap-3 mb-4">
      <div v-for="(image, index) in post.images.slice(0, 3)" :key="index" class="post-image bg-gray-200 rounded-lg overflow-hidden h-32 flex items-center justify-center text-gray-500 text-xs">
        {{ image }}
      </div>
    </div>

    <div class="post-stats flex gap-5 pt-3 mt-3 border-t border-gray-100 text-sm text-gray-500">
      <div class="stat-item flex items-center gap-1">
        <i class="fa-solid fa-heart"></i> {{ post.likes }}
      </div>
      <div class="stat-item flex items-center gap-1">
        <i class="fa-solid fa-comment"></i> {{ post.comments }}
      </div>
      <div class="stat-item flex items-center gap-1">
        <i class="fa-solid fa-eye"></i> {{ post.views }}
      </div>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
// ⚠️ 新增：导入 RouterLink 组件
import { RouterLink } from 'vue-router';

// Post 接口定义
interface Post {
  id: number;
  author: string;
  avatarInitial: string;
  timeAgo: string;
  title: string;
  content: string;
  images: string[];
  likes: number;
  comments: number;
  views: string;
}

// 定义组件接收的 props
defineProps<{
  post: Post;
}>();

// 由于 PostList.vue 中定义了 posts 包含 id，我们可以在这里直接使用 props.post.id

</script>

<style scoped>
/* 使用 Tailwind CSS 替代部分自定义样式 */
.post-card {
  display: block; /* RouterLink 默认是 a 标签，需要 display: block/flex 等才能应用 padding/margin */
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  padding: 25px;
  transition: transform 0.3s;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.line-clamp-2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
    -webkit-line-clamp: 2; /* 限制内容最多两行 */
    line-clamp: 2;
}
</style>
