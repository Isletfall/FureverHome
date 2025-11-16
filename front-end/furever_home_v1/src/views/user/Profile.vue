<template>
  <main class="container mx-auto px-4 sm:px-6 lg:px-8 py-8 lg:py-12">
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 左侧栏 -->
      <aside class="lg:col-span-1 space-y-8">
        <!-- 用户卡片 -->
        <div class="bg-orange-50 dark:bg-gray-800 p-6 rounded-lg shadow">
          <div class="flex items-center space-x-4">
            <div class="w-20 h-20 rounded-full bg-yellow-400 flex items-center justify-center text-white text-4xl font-bold">
              {{ user.name.charAt(0) }}
            </div>
            <div>
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">{{ user.name }}</h1>
              <span class="inline-block bg-primary text-white text-xs font-semibold px-3 py-1 rounded-full mt-2">{{ user.title }}</span>
            </div>
          </div>
          <button type="button" class="inline-flex items-center text-sm text-primary dark:text-orange-300 font-semibold mt-6 hover:underline" @click="goBack">
            <span class="material-icons text-base mr-1">arrow_back_ios_new</span>
            返回个人中心
          </button>
          <div class="grid grid-cols-2 gap-4 mt-4">
            <div class="bg-white dark:bg-gray-700 p-4 rounded-lg text-center shadow-sm" v-for="stat in user.stats" :key="stat.key">
              <p class="text-3xl font-bold text-primary">{{ stat.value }}</p>
              <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ stat.label }}</p>
            </div>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="bg-orange-50 dark:bg-gray-800 p-6 rounded-lg shadow">
          <h2 class="text-lg font-bold text-primary mb-4">基本信息</h2>
          <div class="grid grid-cols-2 gap-y-4 text-sm">
            <div v-for="info in baseInfo" :key="info.label" :class="info.spanAll ? 'col-span-2' : ''">
              <p class="text-gray-500 dark:text-gray-400">{{ info.label }}</p>
              <p class="font-medium text-gray-800 dark:text-gray-200 mt-1">{{ info.value }}</p>
            </div>
          </div>
        </div>

        <!-- 勋章 -->
        <div class="bg-orange-50 dark:bg-gray-800 p-6 rounded-lg shadow">
          <h2 class="text-lg font-bold text-primary mb-4">我的勋章</h2>
            <div class="grid grid-cols-2 gap-4">
              <div v-for="badge in badges" :key="badge.id" class="aspect-square bg-gray-200 dark:bg-gray-700 rounded-lg flex items-center justify-center text-gray-500 dark:text-gray-400 text-xs font-medium">
                {{ badge.name }}
              </div>
          </div>
        </div>
      </aside>

      <!-- 右侧主体内容 -->
      <div class="lg:col-span-2 space-y-8">
        <!-- 个人简介 -->
        <section class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
          <h2 class="text-lg font-bold text-primary mb-4">个人简介</h2>
          <p class="text-gray-600 dark:text-gray-300 leading-relaxed">{{ user.bio }}</p>
        </section>

        <!-- 爱宠证明档案 -->
        <section class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-bold text-primary">爱宠证明档案</h2>
          </div>
          <h3 class="font-semibold text-gray-700 dark:text-gray-200 mb-2">养宠经历</h3>
          <ul class="list-disc list-inside space-y-2 text-gray-600 dark:text-gray-300 mb-6">
            <li v-for="exp in experiences" :key="exp.id">{{ exp.text }}</li>
          </ul>
          <div class="flex justify-between items-center mb-3">
            <h3 class="font-semibold text-gray-700 dark:text-gray-200">证明材料</h3>
            <button type="button" class="text-sm bg-primary/10 text-primary dark:bg-primary/20 dark:text-orange-300 px-3 py-1 rounded-md hover:bg-primary/20 transition-colors" @click="uploadProof">上传新证明</button>
          </div>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
            <div v-for="proof in proofs" :key="proof.id" class="relative cursor-pointer" @click="handleProofClick(proof)">
              <div class="aspect-video bg-gray-100 dark:bg-gray-700 rounded-lg flex items-center justify-center text-gray-400 dark:text-gray-500 text-xs">{{ proof.title }}</div>
              <span :class="['absolute top-2 right-2 text-xs font-semibold px-2 py-0.5 rounded-full', statusClass(proof.status)]">[{{ statusLabel(proof.status) }}]</span>
            </div>
          </div>
        </section>

        <!-- 信誉积分 -->
        <section class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
          <h2 class="text-lg font-bold text-primary mb-4">信誉积分</h2>
          <div class="flex items-center mb-6">
            <span class="text-5xl font-bold text-gray-800 dark:text-gray-100">{{ rating.score.toFixed(1) }}</span>
            <div class="ml-4">
              <div class="flex text-yellow-400">
                <span v-for="i in 5" :key="i" class="material-icons" :class="starIcon(i)">{{ starIcon(i) }}</span>
              </div>
              <p class="text-sm text-gray-500 dark:text-gray-400">基于{{ rating.total }}条评价</p>
            </div>
          </div>
          <h3 class="font-semibold text-gray-700 dark:text-gray-200 mb-4">他人评价</h3>
          <div class="space-y-6">
            <div v-for="eva in evaluations" :key="eva.id" class="border-b border-gray-200 dark:border-gray-700 pb-4">
              <div class="flex justify-between items-start">
                <div>
                  <p class="font-semibold text-gray-800 dark:text-gray-200">{{ eva.author }}</p>
                  <div class="flex text-yellow-400 my-1">
                    <span v-for="i in 5" :key="i" class="material-icons text-sm">{{ i <= eva.stars ? 'star' : 'star_border' }}</span>
                  </div>
                  <p class="text-gray-600 dark:text-gray-300">{{ eva.content }}</p>
                </div>
                <div class="flex flex-col items-end flex-shrink-0 ml-4">
                  <p class="text-xs text-gray-400 dark:text-gray-500 mb-2">{{ eva.date }}</p>
                  <button v-if="eva.appealable" type="button" class="text-xs text-blue-600 dark:text-blue-400 hover:underline" @click="appeal(eva)">【申诉】</button>
                </div>
              </div>
            </div>
          </div>
          <button type="button" class="mt-6 w-full sm:w-auto bg-primary text-white font-semibold py-2 px-6 rounded-lg hover:opacity-90 transition-opacity" @click="addEvaluation">添加评价</button>
        </section>

        <!-- 领养情况 (使用 PetCard) -->
        <section class="space-y-8">
          <div>
            <h2 class="text-lg font-bold text-primary mb-4">短期领养</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <PetCard
                v-for="pet in shortTermAdoptions"
                :key="pet.id"
                :name="pet.name"
                :desc="pet.desc"
                :gender="pet.gender"
                :status="pet.status"
                :status-label="pet.statusLabel"
                :days="pet.days"
                variant="short"
                :title-class="pet.titleClass"
                :bg-class="pet.bgClass"
              />
            </div>
          </div>
          <div>
            <h2 class="text-lg font-bold text-primary mb-4">长期领养</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <PetCard
                v-for="pet in longTermAdoptions"
                :key="pet.id"
                :name="pet.name"
                :desc="pet.desc"
                :gender="pet.gender"
                :status="pet.status"
                :status-label="pet.statusLabel"
                :days="pet.days"
                variant="long"
                :title-class="pet.titleClass"
                :bg-class="pet.bgClass"
                @badge-click="pet.status==='rejected' && showReject(pet)"
              />
            </div>
          </div>
        </section>

        <!-- 最近帖子 -->
        <section class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow">
          <h2 class="text-lg font-bold text-primary mb-4">最近发布的帖子</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div v-for="post in recentPosts" :key="post.id" class="border border-gray-200 dark:border-gray-700 p-4 rounded-lg">
              <div class="flex justify-between items-center mb-2">
                <h3 :class="['font-bold', post.colorClass]">{{ post.title }}</h3>
                <span :class="['text-xs font-semibold px-2 py-0.5 rounded-full cursor-pointer', statusClass(post.status)]" @click="post.status==='rejected' && showReject(post)">[{{ post.statusLabel }}]</span>
              </div>
              <p class="text-xs text-gray-400 dark:text-gray-500 my-2">{{ post.date }}</p>
              <p class="text-sm text-gray-600 dark:text-gray-300 leading-relaxed">{{ post.summary }}</p>
            </div>
          </div>
        </section>
      </div>
    </div>
  </main>

  <footer class="bg-gray-800 dark:bg-black text-gray-300 dark:text-gray-400">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div>
          <h3 class="text-white font-bold mb-2">数据统计</h3>
          <p class="text-sm leading-loose">我们与多家救助站建立了长期合作关系，致力于为流浪动物提供更好的临时安置和长期领养服务。</p>
          <RouterLink to="/shelters" class="text-sm text-primary hover:underline mt-2 inline-block">查看合作救助站 →</RouterLink>
        </div>
        <div>
          <h3 class="text-white font-bold mb-2">联系开发团队</h3>
          <p class="text-sm leading-loose">如果您有任何问题、建议或合作意向，请随时联系我们的开发团队。</p>
          <div class="flex space-x-4 mt-3" aria-label="社交图标">
            <span class="w-6 h-6" aria-hidden="true">💬</span>
            <span class="w-6 h-6" aria-hidden="true">💌</span>
            <span class="w-6 h-6" aria-hidden="true">🌐</span>
          </div>
        </div>
      </div>
      <div class="border-t border-gray-700 dark:border-gray-600 mt-8 pt-6 text-center text-sm">
        <p>2025 FUREVERHOME流浪动物领养平台 - 让每个生命都有温暖的家</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import PetCard from '@/components/common/PetCard.vue';
import { RouterLink, useRouter } from 'vue-router';

interface Stat { key: string; label: string; value: number; }
interface Experience { id: number; text: string; }
interface Proof { id: number; title: string; status: 'approved' | 'pending' | 'rejected'; reason?: string; }
interface Evaluation { id: number; author: string; stars: number; content: string; date: string; appealable?: boolean; }
interface AdoptionPet { id: number; name: string; gender: string; desc: string; days: number; status: 'approved' | 'pending' | 'rejected'; statusLabel: string; bgClass: string; titleClass: string; reason?: string; }
interface Post { id: number; title: string; date: string; summary: string; status: 'approved' | 'pending' | 'rejected'; statusLabel: string; colorClass: string; reason?: string; }
interface Badge { id: number; name: string; }

const router = useRouter();

const user = ref({
  name: '李同学',
  title: '爱心铲屎官',
  bio: '大家好！我是一名大学生，也是一名热爱动物的志愿者。我致力于校园流浪动物的救助与临时寄养，希望成为这些小生命寻找温暖的家。有3年养宠经验，有护宠证书，有爱心有耐心。',
  stats: [
    { key: 'helpTimes', label: '帮助次数', value: 96 },
    { key: 'rescues', label: '救助宠物', value: 5 }
  ] as Stat[]
});

const baseInfo = ref([
  { label: '所在地区', value: '大学城校区' },
  { label: '注册时间', value: '2023年3月' },
  { label: '救助经验', value: '2年', spanAll: true }
]);

const badges = ref<Badge[]>([
  { id: 1, name: '勋章 1' },
  { id: 2, name: '勋章 2' },
  { id: 3, name: '勋章 3' }
]);

const experiences = ref<Experience[]>([
  { id: 1, text: '2021-2023年 饲养金毛犬“旺财” - 负责日常护理与定期体检' },
  { id: 2, text: '2020-至今 救助并寄养校园流浪猫狗 - 协助寻找合适领养家庭' }
]);

const proofs = ref<Proof[]>([
  { id: 1, title: '护宠证书照片', status: 'approved' },
  { id: 2, title: '宠物饲养保证书', status: 'pending' },
  { id: 3, title: '动物救助服务证书', status: 'rejected', reason: '照片模糊不清，请重新上传清晰的证书照片。' }
]);

const rating = ref({ score: 4.9, total: 28 });

const evaluations = ref<Evaluation[]>([
  { id: 1, author: '张同学', stars: 5, content: '李同学非常有爱心，对小橘照顾得无微不至，定期分享小橘的成长动态，让我们很放心。', date: '2023-10-15' },
  { id: 2, author: '王老师', stars: 4, content: '感谢李同学救助了校园里的流浪猫，并帮助它们找到了温暖的家，非常有责任心。', date: '2023-09-28' },
  { id: 3, author: '匿名用户', stars: 2, content: '沟通不是很及时，希望改进。', date: '2023-09-10', appealable: true }
]);

const shortTermAdoptions = ref<AdoptionPet[]>([
  { id: 1, name: '小橘', gender: '公', desc: '橘猫 · 9个月', days: 45, status: 'approved', statusLabel: '已通过', bgClass: 'bg-orange-200 dark:bg-orange-900/50 text-orange-500 dark:text-orange-400', titleClass: 'text-orange-500', reason: '' },
  { id: 2, name: '小白', gender: '母', desc: '比熊犬 · 1岁', days: 12, status: 'pending', statusLabel: '审核中', bgClass: 'bg-blue-100 dark:bg-blue-900/50 text-blue-500 dark:text-blue-400', titleClass: 'text-blue-500', reason: '' }
]);

const longTermAdoptions = ref<AdoptionPet[]>([
  { id: 3, name: '花花', gender: '母', desc: '三花猫 · 2岁', days: 120, status: 'rejected', statusLabel: '已拒绝', bgClass: 'bg-pink-100 dark:bg-pink-900/50 text-pink-500 dark:text-pink-400', titleClass: 'text-pink-500', reason: '信息不完整，缺少疫苗记录。' }
]);

const recentPosts = ref<Post[]>([
  { id: 1, title: '小橘的近况更新', date: '2023-11-05', summary: '小橘最近状态很好，体重增加了，也越来越亲人了。每天都会在门口迎接我回家...', status: 'approved', statusLabel: '已通过', colorClass: 'text-orange-500' },
  { id: 2, title: '寻找小白的新家', date: '2023-10-20', summary: '小白是一只非常温顺的比熊犬，已完成疫苗接种，正在寻找一个有爱的永久家庭...', status: 'rejected', statusLabel: '已拒绝', colorClass: 'text-blue-500', reason: '帖子包含个人联系方式，不符合社区规定。' }
]);

function statusClass(status: string) {
  switch (status) {
    case 'approved': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
    case 'pending': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
    case 'rejected': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
    default: return 'bg-gray-100 text-gray-800';
  }
}

function statusLabel(status: string) {
  switch (status) {
    case 'approved': return '已通过';
    case 'pending': return '审核中';
    case 'rejected': return '已拒绝';
    default: return '未知';
  }
}

function starIcon(i: number) {
  const full = Math.floor(rating.value.score);
  if (i <= full) return 'star';
  if (i === full + 1 && rating.value.score % 1 >= 0.5) return 'star_half';
  return 'star_border';
}

function handleProofClick(proof: Proof) {
  if (proof.status === 'rejected') {
    alert('拒绝理由：' + proof.reason + '\n您可以点击“重新提交”按钮再次上传。');
  }
}

function showReject(item: { reason?: string }) {
  if (item.reason) {
    alert('拒绝理由：' + item.reason + '\n您可以点击“重新提交”按钮修改后再次提交。');
  }
}

function uploadProof() {
  alert('上传新证明的功能尚未实现，这里将弹出上传对话框。');
}

function addEvaluation() {
  alert('添加评价功能待实现。这里可打开评价输入弹窗。');
}

function appeal(eva: Evaluation) {
  alert('申诉功能待实现：针对评价 #' + eva.id);
}

function goBack() {
  router.push('/home');
}

</script>

<style scoped>
</style>