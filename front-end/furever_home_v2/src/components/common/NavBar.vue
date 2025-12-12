<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { getCurrentUser, type CurrentUserInfo } from '../../api/userApi'

const route = useRoute()
const router = useRouter()

const navItems = [
  { name: 'Home', label: '首页', path: '/home' },
  { name: 'PetList', label: '宠物列表', path: '/pets' },
  { name: 'Communication', label: '沟通对接', path: '/communication' },
  { name: 'Forum', label: '宠物论坛', path: '/forum' },
  { name: 'PostPet', label: '发布宠物', path: '/post-pet' }
]

const userName = ref<string>('用户')
const avatarUrl = ref<string | undefined>(undefined)
const loadingUser = ref(false)

const userNameInitial = computed(() => {
  return userName.value ? userName.value.charAt(0) : '用'
})

function loadFromCache() {
  try {
    const cached = localStorage.getItem('currentUser')
    if (cached) {
      const me = JSON.parse(cached) as CurrentUserInfo
      userName.value = me.userName || userName.value
      avatarUrl.value = me.avatarUrl
    } else {
      const cachedName = localStorage.getItem('userName')
      const cachedAvatar = localStorage.getItem('avatarUrl')
      if (cachedName) userName.value = cachedName
      if (cachedAvatar) avatarUrl.value = cachedAvatar
    }
  } catch (e) {
    console.error('解析本地缓存用户信息失败', e)
  }
}

async function loadCurrentUser() {
  try {
    loadingUser.value = true
    const res = await getCurrentUser()
    if ((res.code === 0 || res.code === 200) && res.data) {
      const data: CurrentUserInfo = res.data
      userName.value = data.userName || '用户'
      avatarUrl.value = data.avatarUrl
      localStorage.setItem('currentUser', JSON.stringify(data))
      if (data.userName) localStorage.setItem('userName', data.userName)
      if (data.avatarUrl) localStorage.setItem('avatarUrl', data.avatarUrl)
    }
  } catch (error) {
    console.error('获取当前用户信息失败', error)
  } finally {
    loadingUser.value = false
  }
}

function handleUserUpdated() {
  loadFromCache()
}

// 跳转到个人主页：点击时先刷新一次个人信息，再进入个人主页
const handleProfileClick = async () => {
  await loadCurrentUser()
  router.push({ name: 'Profile' })
}

onMounted(() => {
  loadFromCache()
  loadCurrentUser()
  window.addEventListener('current-user-updated', handleUserUpdated)
})

onUnmounted(() => {
  window.removeEventListener('current-user-updated', handleUserUpdated)
})
</script>

<template>
  <header class="h-[60px] flex items-center justify-between px-[10%] shadow-md sticky top-0 z-[100]" style="background-color: #FF8C00; color: white;">
    <RouterLink to="/home" class="flex items-center gap-2 hover:opacity-90 transition-opacity">
      <img src="@/assets/icons/logo.svg" alt="FUREVERHOME" class="h-10 w-auto rounded-md" />
    </RouterLink>
    <nav>
      <ul class="flex gap-8 text-base">
        <li
          v-for="item in navItems"
          :key="item.name"
          class="cursor-pointer opacity-90 py-1 border-b-[3px] border-transparent transition-all hover:opacity-100 hover:font-medium"
          :class="route.name === item.name ? 'opacity-100 font-extrabold border-white' : ''"
        >
          <RouterLink :to="item.path">{{ item.label }}</RouterLink>
        </li>
      </ul>
    </nav>
    <!-- 用户按钮：点击直接进入我的个人主页 -->
    <button
      @click="handleProfileClick"
      class="flex items-center gap-2 text-sm cursor-pointer hover:opacity-80 transition-opacity"
      type="button"
    >
      <div
        v-if="avatarUrl"
        class="w-8 h-8 rounded-full overflow-hidden bg-white/30 flex items-center justify-center"
      >
        <img :src="avatarUrl" alt="用户头像" class="w-full h-full object-cover" />
      </div>
      <div
        v-else
        class="w-8 h-8 rounded-full flex items-center justify-center bg白 text-[#FF8C00] font-bold text-sm"
        style="box-shadow: 0 0 0 2px rgba(255,255,255,0.4);"
      >
        {{ userNameInitial }}
      </div>
      <span class="font-medium">{{ userName }}</span>
    </button>
  </header>
</template>
