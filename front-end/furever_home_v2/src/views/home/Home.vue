<template>
  <div class="bg-[#F8F9FB]">
    <!-- Hero 区域 -->
    <section class="flex items-center justify-between px-[10%] pt-[60px] pb-[40px] min-h-[70vh] bg-gradient-to-br from-[#F8F9FB] to-[#FFEEDD] relative overflow-hidden">
      <div class="max-w-[600px] z-[2]">
        <h1 class="text-5xl font-bold leading-tight mb-5 text-[#333333]">
          让每个小生命都有<br>
          <span class="text-[#FF8C00]">温暖的家</span>
        </h1>
        <p class="text-lg text-[#666666] mb-8">
          FUREVERHOME 动物领养系统，连接流浪动物与有爱心的你。领养代替购买，让爱心循环，让生命不再流浪。
        </p>
        <div class="flex gap-4 flex-wrap">
          <button
            class="inline-flex items-center gap-2 px-7 py-3 bg-[#FF8C00] text-white rounded-lg text-base font-medium shadow-[0_2px_8px_rgba(255,140,0,0.3)] transition-all hover:bg-[#E67A2A] hover:-translate-y-0.5 hover:shadow-[0_4px_12px_rgba(255,140,0,0.4)] cursor-pointer border-none"
            @click="goToPetList"
          >
            立即领养
          </button>
          <button
            class="inline-flex items-center gap-2 px-7 py-3 bg-transparent border-2 border-[#FF8C00] text-[#FF8C00] rounded-lg text-base font-medium transition-all hover:bg-[#FF8C00] hover:text-white cursor-pointer"
            @click="goToPostPet"
          >
            发布救助
          </button>
        </div>
      </div>
      <div class="relative w-[420px] h-[420px] z-[2]">
        <img
          src="https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=60"
          alt="可爱猫咪"
          class="w-full h-full object-cover rounded-xl shadow-lg"
        />
      </div>
    </section>

    <!-- 数字展示 -->
    <!-- <div class="flex justify-center gap-[60px] py-[60px] bg-white mx-[10%] my-10 rounded-xl shadow-sm relative before:content-[''] before:absolute before:top-0 before:left-0 before:right-0 before:h-1 before:bg-[#FF8C00] before:rounded-t-xl">
      <div class="text-center relative">
        <div class="text-[40px] font-bold text-[#FF8C00] mb-2">1,286</div>
        <div class="text-sm text-[#666666]">成功领养</div>
      </div>
      <div class="text-center relative before:content-[''] before:absolute before:left-[-30px] before:top-1/2 before:-translate-y-1/2 before:w-px before:h-10 before:bg-[#eee]">
        <div class="text-[40px] font-bold text-[#FF8C00] mb-2">3,420</div>
        <div class="text-sm text-[#666666]">发布动物</div>
      </div>
      <div class="text-center relative before:content-[''] before:absolute before:left-[-30px] before:top-1/2 before:-translate-y-1/2 before:w-px before:h-10 before:bg-[#eee]">
        <div class="text-[40px] font-bold text-[#FF8C00] mb-2">8,950</div>
        <div class="text-sm text-[#666666]">活跃用户</div>
      </div>
      <div class="text-center relative before:content-[''] before:absolute before:left-[-30px] before:top-1/2 before:-translate-y-1/2 before:w-px before:h-10 before:bg-[#eee]">
        <div class="text-[40px] font-bold text-[#FF8C00] mb-2">156</div>
        <div class="text-sm text-[#666666]">合作救助站</div>
      </div>
    </div> -->

    <!-- 核心功能 -->
    <section class="py-20 px-[10%]">
      <h2 class="text-[32px] text-center mb-[50px] relative text-[#333333] after:content-[''] after:absolute after:bottom-[-10px] after:left-1/2 after:-translate-x-1/2 after:w-[60px] after:h-[3px] after:bg-[#FF8C00] after:rounded">
        核心功能
      </h2>
      <div class="grid grid-cols-[repeat(auto-fit,minmax(260px,1fr))] gap-8">
        <div 
          class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1.5 relative overflow-hidden before:content-[''] before:absolute before:top-0 before:left-0 before:right-0 before:h-1 before:bg-[#FF8C00] cursor-pointer"
          @click="goToPetList"
        >
          <div class="text-5xl mb-4 inline-block p-4 bg-[rgba(255,140,0,0.1)] rounded-full text-[#FF8C00]">
            <i class="fa-solid fa-list"></i>
          </div>
          <h3 class="text-xl mb-2.5 text-[#333333]">宠物列表</h3>
          <p class="text-[15px] text-[#666666]">浏览待领养/已领养宠物，支持多维度筛选与搜索，找到最适合你的小伙伴</p>
        </div>
        <div 
          class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1.5 relative overflow-hidden before:content-[''] before:absolute before:top-0 before:left-0 before:right-0 before:h-1 before:bg-[#FF8C00] cursor-pointer"
          @click="router.push('/communication')"
        >
          <div class="text-5xl mb-4 inline-block p-4 bg-[rgba(255,140,0,0.1)] rounded-full text-[#FF8C00]">
            <i class="fa-solid fa-comments"></i>
          </div>
          <h3 class="text-xl mb-2.5 text-[#333333]">沟通对接</h3>
          <p class="text-[15px] text-[#666666]">实时聊天、领养申请、进度通知，一键完成领养流程</p>
        </div>
        <div 
          class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1.5 relative overflow-hidden before:content-[''] before:absolute before:top-0 before:left-0 before:right-0 before:h-1 before:bg-[#FF8C00] cursor-pointer"
          @click="router.push('/forum')"
        >
          <div class="text-5xl mb-4 inline-block p-4 bg-[rgba(255,140,0,0.1)] rounded-full text-[#FF8C00]">
            <i class="fa-solid fa-users"></i>
          </div>
          <h3 class="text-xl mb-2.5 text-[#333333]">宠物论坛</h3>
          <p class="text-[15px] text-[#666666]">分享日常、求助答疑、点赞评价，共建有爱社区</p>
        </div>
        <div 
          class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1.5 relative overflow-hidden before:content-[''] before:absolute before:top-0 before:left-0 before:right-0 before:h-1 before:bg-[#FF8C00] cursor-pointer"
          @click="goToPostPet"
        >
          <div class="text-5xl mb-4 inline-block p-4 bg-[rgba(255,140,0,0.1)] rounded-full text-[#FF8C00]">
            <i class="fa-solid fa-paw"></i>
          </div>
          <h3 class="text-xl mb-2.5 text-[#333333]">发布动物</h3>
          <p class="text-[15px] text-[#666666]">快速发布流浪/救助动物，填写信息即可上线</p>
        </div>
      </div>
    </section>

    <!-- 救助指南 -->
    <!-- <section class="py-20 px-[10%] bg-[#F8F9FB]">
      <h2 class="text-[32px] text-center mb-[50px] relative text-[#333333] after:content-[''] after:absolute after:bottom-[-10px] after:left-1/2 after:-translate-x-1/2 after:w-[60px] after:h-[3px] after:bg-[#FF8C00] after:rounded">
        救助指南
      </h2>
      <div class="grid grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-8 mt-10">
        <div class="bg-white rounded-xl p-8 shadow-sm text-center relative">
          <div class="absolute -top-4 left-1/2 -translate-x-1/2 w-10 h-10 bg-[#FF8C00] text-white rounded-full flex items-center justify-center font-bold text-lg">
            1
          </div>
          <div class="text-5xl my-5 text-[#FF8C00]">
            <i class="fa-solid fa-search"></i>
          </div>
          <div class="text-lg font-semibold mb-2.5 text-[#333333]">发现动物</div>
          <div class="text-[#666666] text-sm leading-relaxed">在安全的前提下接近动物，观察其健康状况和行为表现</div>
        </div>
        <div class="bg-white rounded-xl p-8 shadow-sm text-center relative">
          <div class="absolute -top-4 left-1/2 -translate-x-1/2 w-10 h-10 bg-[#FF8C00] text-white rounded-full flex items-center justify-center font-bold text-lg">
            2
          </div>
          <div class="text-5xl my-5 text-[#FF8C00]">
            <i class="fa-solid fa-house"></i>
          </div>
          <div class="text-lg font-semibold mb-2.5 text-[#333333]">临时安置</div>
          <div class="text-[#666666] text-sm leading-relaxed">提供安全的临时住所，准备食物、饮水和基本护理</div>
        </div>
        <div class="bg-white rounded-xl p-8 shadow-sm text-center relative">
          <div class="absolute -top-4 left-1/2 -translate-x-1/2 w-10 h-10 bg-[#FF8C00] text-white rounded-full flex items-center justify-center font-bold text-lg">
            3
          </div>
          <div class="text-5xl my-5 text-[#FF8C00]">
            <i class="fa-solid fa-stethoscope"></i>
          </div>
          <div class="text-lg font-semibold mb-2.5 text-[#333333]">健康检查</div>
          <div class="text-[#666666] text-sm leading-relaxed">尽快带往宠物医院进行健康检查和必要的治疗</div>
        </div>
        <div class="bg-white rounded-xl p-8 shadow-sm text-center relative">
          <div class="absolute -top-4 left-1/2 -translate-x-1/2 w-10 h-10 bg-[#FF8C00] text-white rounded-full flex items-center justify-center font-bold text-lg">
            4
          </div>
          <div class="text-5xl my-5 text-[#FF8C00]">
            <i class="fa-solid fa-upload"></i>
          </div>
          <div class="text-lg font-semibold mb-2.5 text-[#333333]">发布信息</div>
          <div class="text-[#666666] text-sm leading-relaxed">在平台发布救助信息，寻找合适的领养家庭</div>
        </div>
      </div>
    </section> -->

    <!-- 合作伙伴 -->
    <!-- <section class="py-20 px-[10%]">
      <h2 class="text-[32px] text-center mb-[50px] relative text-[#333333] after:content-[''] after:absolute after:bottom-[-10px] after:left-1/2 after:-translate-x-1/2 after:w-[60px] after:h-[3px] after:bg-[#FF8C00] after:rounded">
        合作伙伴
      </h2>
      <div class="grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-8 mt-10">
        <div class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1">
          <div class="text-4xl mb-4 text-[#FF8C00]">
            <i class="fa-solid fa-hospital"></i>
          </div>
          <div class="font-semibold text-[#333333] mb-2">爱心宠物医院</div>
          <div class="text-[#666666] text-sm">提供流浪动物免费基础体检服务</div>
        </div>
        <div class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1">
          <div class="text-4xl mb-4 text-[#FF8C00]">
            <i class="fa-solid fa-paw"></i>
          </div>
          <div class="font-semibold text-[#333333] mb-2">流浪动物救助中心</div>
          <div class="text-[#666666] text-sm">专业的动物救助和康复机构</div>
        </div>
        <div class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1">
          <div class="text-4xl mb-4 text-[#FF8C00]">
            <i class="fa-solid fa-hands-helping"></i>
          </div>
          <div class="font-semibold text-[#333333] mb-2">动物保护协会</div>
          <div class="text-[#666666] text-sm">推动动物福利和保护的公益组织</div>
        </div>
        <div class="bg-white rounded-xl p-8 shadow-sm text-center transition-transform hover:-translate-y-1">
          <div class="text-4xl mb-4 text-[#FF8C00]">
            <i class="fa-solid fa-heart"></i>
          </div>
          <div class="font-semibold text-[#333333] mb-2">志愿者团队</div>
          <div class="text-[#666666] text-sm">热心公益的志愿者服务团队</div>
        </div>
      </div>
    </section> -->

    <!-- Footer -->
    <footer class="bg-[#1E293B] text-white py-10 px-[10%] mt-16">
      <!-- <div class="flex flex-wrap justify-between max-w-[1200px] mx-auto gap-8">
        <div class="flex-1 min-w-[220px]">
          <h3 class="mb-4 text-lg">数据统计</h3>
          <p class="mb-2 text-sm text-[#ccc]">我们与多家救助站建立了长期合作关系</p>
          <p class="mb-2 text-sm text-[#ccc]">已为 <strong>1,286</strong> 只小动物找到新家</p>
          <a href="#" class="text-[#FF8C42] text-sm hover:underline">查看合作救助站 →</a>
        </div>
        <div class="flex-1 min-w-[220px]">
          <h3 class="mb-4 text-lg">联系开发团队</h3>
          <p class="mb-2 text-sm text-[#ccc]">如果您有任何问题、建议或合作意向，请随时联系我们的开发团队。</p>
          <div class="flex gap-3 mt-2.5">
            <i class="fa-brands fa-weixin text-2xl hover:text-[#FF8C00] cursor-pointer transition-colors"></i>
            <i class="fa-brands fa-qq text-2xl hover:text-[#FF8C00] cursor-pointer transition-colors"></i>
            <i class="fa-brands fa-weibo text-2xl hover:text-[#FF8C00] cursor-pointer transition-colors"></i>
          </div>
        </div>
      </div> -->
      <div class="text-center mt-8 pt-5 border-t border-[rgba(255,255,255,0.1)] text-sm text-[#aaa]">
        2025 FUREVERHOME动物领养平台 - 让每个生命都有温暖的家
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToPetList = () => {
  router.push('/pets')
}

const goToPostPet = () => {
  router.push('/post-pet')
}
</script>

<style scoped>
</style>