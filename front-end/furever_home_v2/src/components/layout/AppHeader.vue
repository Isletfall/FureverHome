<template>
  <header class="app-header">
    <div class="logo" @click="$router.push('/')">
      <img src="@/assets/icons/logo.svg" alt="FUREVERHOME" style="height: 40px;" />
    </div>
    <nav>
      <ul>
        <li :class="{ active: $route.path === '/' }" @click="$router.push('/')">首页</li>
        <li :class="{ active: $route.path === '/pets' }" @click="$router.push('/pets')">宠物列表</li>
        <li :class="{ active: $route.path === '/communication' }" @click="$router.push('/communication')">沟通对接</li>
        <li :class="{ active: $route.path === '/forum' }" @click="$router.push('/forum')">宠物论坛</li>
        <li :class="{ active: $route.path === '/post-pet' }" @click="$router.push('/post-pet')">发布宠物</li>
      </ul>
    </nav>
    <div class="user-section" @click.stop="toggleUserMenu">
      <div class="user-avatar">👤</div>
      <span>用户</span>
      <div class="user-menu" :class="{ show: showUserMenu }">
        <a href="#" @click.prevent="$router.push('/user-center')">用户中心</a>
        <a href="#" @click.prevent="handleLogout">退出登录</a>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const showUserMenu = ref(false)

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleLogout = () => {
  // TODO: 实现登出逻辑
  console.log('退出登录')
}

const closeUserMenu = () => {
  showUserMenu.value = false
}

onMounted(() => {
  document.addEventListener('click', closeUserMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeUserMenu)
})
</script>

<style scoped>
.app-header {
  background-color: #FF8C00;
  color: white;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-weight: 800;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

nav ul {
  display: flex;
  gap: 30px;
  font-size: 16px;
  list-style: none;
}

nav ul li {
  cursor: pointer;
  opacity: 0.9;
  padding: 5px 0;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
}

nav ul li:hover {
  opacity: 1;
  font-weight: 500;
}

nav ul li.active {
  opacity: 1;
  font-weight: 800;
  border-bottom-color: white;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: transform 0.3s;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.user-menu {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 8px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  display: none;
  flex-direction: column;
  min-width: 140px;
  padding: 8px 0;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

.user-menu.show {
  display: flex;
}

.user-menu a {
  padding: 10px 16px;
  font-size: 14px;
  color: #333;
  text-decoration: none;
  transition: background 0.2s;
}

.user-menu a:hover {
  background: #f3f4f6;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 850px) {
  .app-header {
    padding: 0 20px;
  }
  nav {
    display: none;
  }
}
</style>
