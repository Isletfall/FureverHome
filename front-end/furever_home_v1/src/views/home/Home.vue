<template>
  <div class="home-container">
    <!-- Hero 标题区域 -->
    <section class="text-center py-12">
      <h1 class="text-4xl font-bold tracking-wide text-primary mb-4">
        为这些可爱的小生命寻找一个永久的家
      </h1>
      <p class="mt-2 text-lg text-gray-600 dark:text-gray-400 max-w-3xl mx-auto">
        领养代替购买，给流浪动物一个温暖的家
      </p>
    </section>

    <!-- 精简版筛选条件区域 -->
    <section class="bg-white dark:bg-zinc-900 p-6 rounded-lg shadow-sm mb-10">
      <div class="flex flex-wrap items-center gap-4">
        <span class="font-medium text-gray-800 dark:text-gray-200">筛选条件:</span>
        <button class="px-4 py-2 text-sm font-medium rounded-full bg-orange-100 dark:bg-orange-900/50 text-primary" @click="setQuickFilter('recommended')">推荐</button>
        <button class="px-4 py-2 text-sm font-medium rounded-full bg-gray-100 dark:bg-zinc-800 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-zinc-700" @click="setQuickFilter('hot')">热门推荐</button>
        <div class="flex items-center space-x-2 bg-gray-100 dark:bg-zinc-800 px-3 py-1 rounded-full cursor-pointer hover:bg-gray-200 dark:hover:bg-zinc-700" @click="togglePicker('region')">
          <span class="text-sm text-gray-600 dark:text-gray-300">地区</span>
          <span class="material-icons text-sm">expand_more</span>
        </div>
        <div class="flex items-center space-x-2 bg-gray-100 dark:bg-zinc-800 px-3 py-1 rounded-full cursor-pointer hover:bg-gray-200 dark:hover:bg-zinc-700" @click="togglePicker('gender')">
          <span class="text-sm text-gray-600 dark:text-gray-300">性别</span>
          <span class="material-icons text-sm">expand_more</span>
        </div>
        <div class="flex items-center space-x-2 bg-gray-100 dark:bg-zinc-800 px-3 py-1 rounded-full cursor-pointer hover:bg-gray-200 dark:hover:bg-zinc-700" @click="togglePicker('breed')">
          <span class="text-sm text-gray-600 dark:text-gray-300">品种</span>
          <span class="material-icons text-sm">expand_more</span>
        </div>
        <div class="flex items-center space-x-2 bg-gray-100 dark:bg-zinc-800 px-3 py-1 rounded-full cursor-pointer hover:bg-gray-200 dark:hover:bg-zinc-700" @click="togglePicker('age')">
          <span class="text-sm text-gray-600 dark:text-gray-300">年龄</span>
          <span class="material-icons text-sm">expand_more</span>
        </div>
        <div class="ml-auto flex items-center gap-4 w-full md:w-auto">
          <div class="relative flex-grow md:flex-grow-0 md:w-72">
            <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 material-icons text-base">search</span>
            <input v-model="filter.search" type="text" placeholder="输入宠物名字或特征搜索..." class="w-full pl-10 pr-4 py-2 text-sm rounded-md border border-gray-200 dark:border-zinc-700 bg-gray-50 dark:bg-zinc-800 text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-primary" />
          </div>
          <button class="px-4 py-2 text-sm font-medium rounded-full border border-primary text-primary hover:bg-primary hover:text-white transition-colors" @click="applyFilters">应用筛选</button>
          <button class="px-4 py-2 text-sm font-medium rounded-full bg-primary text-white hover:bg-orange-500 transition-colors" @click="resetFilters">重置</button>
        </div>
      </div>
    </section>

    <!-- 宠物列表 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
      <!-- 使用v-for循环渲染宠物卡片 -->
      <div 
        v-for="pet in filteredPets" 
        :key="pet.id"
        class="bg-white dark:bg-zinc-900 rounded-lg shadow-sm overflow-hidden flex flex-col group hover:shadow-md transition-shadow"
      >
        <!-- 宠物图片 -->
        <div class="relative h-56 overflow-hidden bg-orange-100 dark:bg-orange-900/20 flex items-center justify-center">
          <img 
            :src="pet.imageUrl" 
            :alt="pet.name" 
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" 
            @error="onImgError($event)"
          >
          <!-- 宠物状态标签 -->
          <div 
            :class="['absolute top-3 left-3 px-2 py-1 rounded-full text-xs font-medium', getStatusClass(pet.status)]"
          >
            {{ getStatusText(pet.status) }}
          </div>
        </div>
        
        <!-- 宠物信息 -->
        <div class="p-6 flex flex-col flex-grow">
          <div class="flex justify-between items-start mb-2">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white">{{ pet.name }}</h3>
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ pet.age }}</span>
          </div>
          
          <p class="text-sm text-gray-600 dark:text-gray-300 mb-4 line-clamp-2 leading-relaxed">
            {{ pet.description }}
          </p>
          
          <div class="mt-auto flex justify-between items-center pt-2">
            <div class="flex items-center space-x-3 text-sm text-gray-500 dark:text-gray-400">
              <span>{{ pet.gender === 'male' ? '公' : '母' }}</span>
              <span>{{ pet.type === 'cat' ? '猫' : pet.type === 'dog' ? '狗' : '其他' }}</span>
            </div>
            <button class="flex items-center gap-2 px-4 py-2 text-sm rounded-md bg-primary text-white hover:bg-orange-500 transition-colors">
              <span class="material-icons text-base">chat_bubble_outline</span>
              联系TA
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 无数据提示 -->
    <div v-if="filteredPets.length === 0" class="text-center py-16">
      <p class="text-gray-500 dark:text-gray-400">没有找到符合条件的宠物</p>
      <button 
        @click="resetFilters" 
        class="mt-4 px-6 py-2 bg-primary hover:bg-orange-400 text-white rounded-md transition-colors"
      >
        查看全部宠物
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
// 导入必要的模块
import { ref, computed } from 'vue';

// 定义宠物数据接口
interface Pet {
  id: number;
  name: string;
  type: 'cat' | 'dog' | 'other';
  gender: 'male' | 'female';
  age: string;
  ageRange: 'baby' | 'young' | 'adult' | 'senior';
  description: string;
  imageUrl: string;
  status: 'available' | 'pending' | 'adopted';
}

// 定义筛选条件接口
interface FilterOptions {
  type: string;
  ageRange: string;
  gender: string;
  search: string;
}

// 初始化宠物数据
const pets = ref<Pet[]>([
  {
    id: 1,
    name: '小白',
    type: 'cat',
    gender: 'female',
    age: '1岁',
    ageRange: 'young',
    description: '温顺可爱的英短银渐层，非常亲人，喜欢被抚摸。',
    imageUrl: 'https://placekitten.com/400/300',
    status: 'available'
  },
  {
    id: 2,
    name: '小黑',
    type: 'dog',
    gender: 'male',
    age: '2岁',
    ageRange: 'young',
    description: '活泼开朗的拉布拉多，训练良好，对人友善。',
    imageUrl: 'https://placedog.net/400/300',
    status: 'available'
  },
  {
    id: 3,
    name: '花花',
    type: 'cat',
    gender: 'female',
    age: '3岁',
    ageRange: 'adult',
    description: '优雅的三花猫咪，独立但亲人，适合有耐心的主人。',
    imageUrl: 'https://placekitten.com/401/300',
    status: 'pending'
  },
  {
    id: 4,
    name: '阿黄',
    type: 'dog',
    gender: 'male',
    age: '5岁',
    ageRange: 'adult',
    description: '稳重的中华田园犬，忠诚护主，已绝育。',
    imageUrl: 'https://placedog.net/401/300',
    status: 'available'
  },
  {
    id: 5,
    name: '灰灰',
    type: 'cat',
    gender: 'male',
    age: '8个月',
    ageRange: 'baby',
    description: '活泼好动的美短虎斑，喜欢玩耍，需要有耐心的主人。',
    imageUrl: 'https://placekitten.com/402/300',
    status: 'available'
  },
  {
    id: 6,
    name: '朵朵',
    type: 'other',
    gender: 'female',
    age: '1岁',
    ageRange: 'young',
    description: '可爱的垂耳兔，毛色柔顺，性格温顺。',
    imageUrl: 'https://placekitten.com/403/300', // 临时使用猫咪图片
    status: 'adopted'
  }
]);

// 初始化筛选条件
const filter = ref<FilterOptions>({
  type: '',
  ageRange: '',
  gender: '',
  search: ''
});

// 计算过滤后的宠物列表
const filteredPets = computed(() => {
  return pets.value.filter(pet => {
    // 类型筛选
    if (filter.value.type && pet.type !== filter.value.type) return false;
    
    // 年龄范围筛选
    if (filter.value.ageRange && pet.ageRange !== filter.value.ageRange) return false;
    
    // 性别筛选
    if (filter.value.gender && pet.gender !== filter.value.gender) return false;
    
    // 搜索筛选
    if (filter.value.search) {
      const searchLower = filter.value.search.toLowerCase();
      return (
        pet.name.toLowerCase().includes(searchLower) ||
        pet.description.toLowerCase().includes(searchLower)
      );
    }
    
    return true;
  });
});

// 获取状态对应的CSS类
function getStatusClass(status: Pet['status']): string {
  switch (status) {
    case 'available':
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    case 'pending':
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
    case 'adopted':
      return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
    default:
      return '';
  }
}

// 获取状态文本
function getStatusText(status: Pet['status']): string {
  switch (status) {
    case 'available':
      return '可领养';
    case 'pending':
      return '待领养';
    case 'adopted':
      return '已领养';
    default:
      return '';
  }
}

// 应用筛选
function applyFilters(): void {
  // 筛选逻辑已在computed中实现
  console.log('应用筛选条件:', filter.value);
}

// 重置筛选条件
function resetFilters(): void {
  filter.value = {
    type: '',
    ageRange: '',
    gender: '',
    search: ''
  };
}

// 快速筛选占位逻辑，可扩展实际筛选条件
function setQuickFilter(kind: string): void {
  console.log('快速筛选:', kind);
}

// 模拟打开其它筛选器（地区/品种等）
function togglePicker(name: string): void {
  console.log('打开筛选器面板:', name);
}

// 图片失败占位
function onImgError(e: Event): void {
  const el = e.target as HTMLImageElement;
  el.src = 'https://placehold.co/400x300?text=No+Image';
}
</script>

<style scoped>
.home-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* 宠物卡片的动画效果 */
.home-container > div.grid > div {
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .home-container > div.grid {
    grid-template-columns: 1fr;
  }
}
</style>