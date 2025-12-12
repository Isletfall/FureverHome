<template>
  <div>
    <h2 class="text-2xl font-bold mb-5" style="color: #111;">基本信息</h2>

    <div class="bg-white rounded-xl p-10 shadow-sm">
      <div class="grid grid-cols-[120px_1fr] gap-6 items-center">
        <!-- 头像 -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">头像</label>
        <div class="flex items-center gap-5">
          <div
            class="w-20 h-20 rounded-full flex items-center justify-center text-3xl text-white border-2 border-white shadow-md overflow-hidden bg-cover bg-center"
            :style="avatarUrl ? '' : 'background-color: #F3C697;'"
          >
            <img
              v-if="avatarUrl"
              :src="avatarUrl"
              alt="头像预览"
              class="w-full h-full object-cover"
            />
            <i v-else class="fa-regular fa-circle"></i>
          </div>
          <button
            class="px-4 py-2 bg-white border border-gray-300 rounded-md text-xs cursor-pointer transition-colors hover:border-[#FF8C00] hover:text-[#FF8C00]"
            style="color: #4B5563;"
            @click="triggerAvatarSelect"
          >
            <i class="fa-solid fa-camera"></i> 更换头像
          </button>
          <input
            ref="avatarInputRef"
            type="file"
            accept=".jpg,.jpeg,.png,.webp"
            class="hidden"
            @change="onAvatarSelected"
          />
        </div>

        <!-- 用户ID -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">用户ID</label>
        <div class="relative max-w-[500px]">
          <input 
            type="text" 
            class="w-full px-4 py-2.5 border border-gray-300 rounded-md bg-gray-50 text-sm text-gray-500 cursor-not-allowed outline-none"
            :value="userId ?? ''" 
            disabled
          />
        </div>

        <!-- 用户名（不可编辑） -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">用户名</label>
        <div class="relative max-w-[500px]">
          <input 
            type="text" 
            :value="userName"
            class="w-full px-4 py-2.5 border border-gray-300 rounded-md bg-gray-50 text-sm text-gray-500 cursor-not-allowed outline-none"
            disabled
          />
        </div>

        <!-- 年龄 -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">年龄</label>
        <div class="relative max-w-[500px]">
          <input 
            type="number" 
            v-model="formData.age"
            class="w-full px-4 py-2.5 border border-gray-300 rounded-md bg-gray-50 text-sm outline-none transition-colors focus:border-[#FF8C00] focus:bg-white"
            style="color: #111;"
          />
        </div>

        <!-- 账户密码 -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">账户密码</label>
        <div class="relative max-w-[500px]">
          <input 
            type="password" 
            class="w-full px-4 py-2.5 border border-gray-300 rounded-md bg-gray-200 text-sm text-gray-500 cursor-not-allowed outline-none"
            value="password123" 
            disabled
          />
          <span 
            class="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs cursor-pointer font-medium transition-colors hover:opacity-80"
            style="color: #FF8C00;"
            @click="handleModifyPassword"
          >
            修改
          </span>
        </div>

        <!-- 邮箱号 -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">邮箱号</label>
        <div class="relative max-w-[500px]">
          <input 
            type="email" 
            :value="formData.email"
            class="w-full px-4 py-2.5 border border-gray-300 rounded-md bg-gray-50 text-sm text-gray-500 cursor-not-allowed outline-none"
            disabled
          />
        </div>

        <!-- 性别 -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">性别</label>
        <div class="flex gap-5">
          <label class="flex items-center gap-1.5 text-sm cursor-pointer">
            <input 
              type="radio" 
              name="gender" 
              value="male"
              v-model="formData.gender"
              style="accent-color: #FF8C00;"
            />
            男
          </label>
          <label class="flex items-center gap-1.5 text-sm cursor-pointer">
            <input 
              type="radio" 
              name="gender" 
              value="female"
              v-model="formData.gender"
              style="accent-color: #FF8C00;"
            />
            女
          </label>
          <label class="flex items-center gap-1.5 text-sm cursor-pointer">
            <input 
              type="radio" 
              name="gender" 
              value="secret"
              v-model="formData.gender"
              style="accent-color: #FF8C00;"
            />
            保密
          </label>
        </div>

        <!-- 所在地 -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">所在地</label>
        <div class="relative max-w-[500px]">
          <input 
            type="text" 
            v-model="formData.location"
            class="w-full px-4 py-2.5 border border-gray-300 rounded-md bg-gray-50 text-sm outline-none transition-colors focus:border-[#FF8C00] focus:bg-white"
            style="color: #111;"
          />
        </div>

        <!-- 爱宠证明简介 -->
        <label class="text-sm font-medium text-right self-start mt-2.5" style="color: #4B5563;">爱宠证明简介</label>
        <div class="relative max-w-[500px]">
          <textarea 
            v-model="formData.petProofIntro"
            class="w-full px-4 py-2.5 border border-gray-300 rounded-md bg-gray-50 text-sm outline-none transition-colors focus:border-[#FF8C00] focus:bg-white min-h-[100px] resize-y"
            style="color: #111;"
            placeholder="请简要描述您的养宠经验、环境条件等..."
          ></textarea>
        </div>

        <!-- 爱宠证明 -->
        <label class="text-sm font-medium text-right" style="color: #4B5563;">爱宠证明</label>
        <div class="relative max-w-[500px]">
          <div 
            class="border-2 border-dashed border-gray-300 bg-gray-50 rounded-md p-5 text-center cursor-pointer text-xs transition-colors hover:border-[#FF8C00] hover:bg-[#FFF7ED]"
            style="color: #6B7280;"
            @click="triggerProofSelect"
          >
            <i class="fa-solid fa-cloud-arrow-up text-2xl mb-1 block"></i>
            <div>点击上传文件或图片（最多3张）</div>
            <div class="text-xs mt-1" style="color: #999;">支持 JPG、JPEG、PNG、WEBP 格式</div>
          </div>
          <input
            ref="proofInputRef"
            type="file"
            accept=".jpg,.jpeg,.png,.webp"
            class="hidden"
            @change="onProofSelected"
          />
          <div v-if="proofPhotos.length" class="mt-3 flex flex-wrap gap-3">
            <div
              v-for="url in proofPhotos"
              :key="url"
              class="relative w-24 h-24 rounded-md overflow-hidden bg-gray-100 flex items-center justify-center cursor-pointer"
              @click="openPreview(url)"
            >
              <img :src="url" alt="爱宠证明" class="w-full h-full object-cover" />
              <button
                type="button"
                class="absolute top-1 right-1 w-5 h-5 rounded-full bg-black/60 text-white text-xs flex items-center justify-center cursor-pointer"
                @click.stop="removeProof(url)"
              >
                ×
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 保存按钮 -->
      <div class="mt-10 pt-5 border-t border-gray-200 flex justify-end">
        <button 
          class="px-8 py-3 rounded-md text-base font-semibold cursor-pointer transition-colors hover:opacity-90"
          style="background-color: #FF8C00; color: white;"
          @click="handleSave"
        >
          保存更改
        </button>
      </div>
    </div>

    <!-- 成功弹窗 -->
    <SuccessModal
      :visible="showSuccessModal"
      title="保存成功！"
      message="您的信息已成功保存。"
      @close="closeSuccessModal"
    />
    <!-- 错误弹窗（例如超出上传数量限制） -->
    <ErrorModal
      :visible="showErrorModal"
      title="提示"
      :message="errorMessage"
      @close="showErrorModal = false"
    />

    <!-- 证明大图预览 -->
    <div
      v-if="showPreview"
      class="fixed inset-0 bg-black/60 z-[1000] flex items-center justify-center"
      @click.self="closePreview"
    >
      <div class="relative max-w-[90vw] max-h-[90vh] bg-white rounded-lg overflow-hidden shadow-xl">
        <button
          type="button"
          class="absolute top-2 right-2 w-8 h-8 rounded-full bg-black/70 text-white flex items-center justify-center text-lg cursor-pointer"
          @click="closePreview"
        >
          ×
        </button>
        <img
          v-if="previewUrl"
          :src="previewUrl"
          alt="爱宠证明预览"
          class="max-w-[90vw] max-h-[90vh] object-contain block"
        />
      </div>
    </div>

    <!-- 图片裁剪器 -->
    <ImageCropper
      :visible="showCropper"
      :image-file="currentCropFile"
      :aspect-ratio="cropAspectRatio"
      @confirm="handleCropConfirm"
      @cancel="handleCropCancel"
      @update:visible="showCropper = $event"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import SuccessModal from '../../../components/common/SuccessModal.vue';
import ErrorModal from '../../../components/common/ErrorModal.vue';
import ImageCropper from '../../../components/common/ImageCropper.vue';
import {
  getCurrentUser,
  updateCurrentUser,
  Sex,
  type CurrentUserInfo,
  type UpdateUserRequest
} from '../../../api/userApi';
import { uploadImage, deleteImage } from '../../../api/storageApi';

interface FormData {
  age: number;
  email: string;
  gender: string;
  location: string;
  petProofIntro: string;
}

const formData = ref<FormData>({
  age: 0,
  email: '',
  gender: 'secret',
  location: '',
  petProofIntro: ''
});

const router = useRouter();

const showSuccessModal = ref(false);
const isLoading = ref(false);
const avatarUrl = ref<string>('');
const avatarInputRef = ref<HTMLInputElement | null>(null);
const userName = ref<string>('');
const userId = ref<number | null>(null);
const proofPhotos = ref<string[]>([]);
const proofInputRef = ref<HTMLInputElement | null>(null);
const showErrorModal = ref(false);
const errorMessage = ref('最多只能上传 3 张爱宠证明照片');
const showPreview = ref(false);
const previewUrl = ref<string | null>(null);
const removedProofs = ref<string[]>([]); // 记录待删除但未提交的证明图片

// 图片裁剪相关
const showCropper = ref(false);
const currentCropFile = ref<File | null>(null);
const cropAspectRatio = ref(4 / 3);
const isAvatarCrop = ref(false);

function mapSexToGender(sex?: Sex): 'male' | 'female' | 'secret' {
  if (sex === Sex.男) return 'male';
  if (sex === Sex.女) return 'female';
  return 'secret';
}

function mapGenderToSex(gender: string): Sex {
  if (gender === 'male') return Sex.男;
  if (gender === 'female') return Sex.女;
  return Sex.保密;
}

async function loadCurrentUser() {
  try {
    const res = await getCurrentUser();
    if ((res.code === 0 || res.code === 200) && res.data) {
      const data: CurrentUserInfo = res.data;
      userName.value = data.userName || '';
      userId.value = data.userId ?? null;
      formData.value.age = data.userAge ?? 0;
      formData.value.email = data.email || '';
      formData.value.gender = mapSexToGender(data.sex);
      formData.value.location = data.location || '';
      formData.value.petProofIntro = data.proofText || '';
      avatarUrl.value = data.avatarUrl || '';
      proofPhotos.value = data.proofPhoto || [];

      // 更新本地缓存
      localStorage.setItem('currentUser', JSON.stringify(data));
      if (data.userName) localStorage.setItem('userName', data.userName);
      if (data.avatarUrl) localStorage.setItem('avatarUrl', data.avatarUrl);
    }
  } catch (error) {
    console.error('获取当前用户信息失败', error);
  }
}

function openPreview(url: string) {
  previewUrl.value = url
  showPreview.value = true
}

function closePreview() {
  showPreview.value = false
  previewUrl.value = null
}

function removeProof(url: string) {
  // 仅做本地移除，真正删除文件延迟到保存时执行
  proofPhotos.value = proofPhotos.value.filter(item => item !== url)
  if (!removedProofs.value.includes(url)) {
    removedProofs.value.push(url)
  }
}

function handleModifyPassword() {
  // 跳转到重置密码页面，带上当前邮箱（如果有的话）
  router.push({
    name: 'ResetPasswordRequest',
    query: { email: formData.value.email || '' }
  });
}

function triggerAvatarSelect() {
  avatarInputRef.value?.click();
}

function onAvatarSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  currentCropFile.value = file;
  cropAspectRatio.value = 1;
  isAvatarCrop.value = true;
  showCropper.value = true;
  
  input.value = '';
}

function triggerProofSelect() {
  proofInputRef.value?.click();
}

function onProofSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  if (proofPhotos.value.length >= 3) {
    showErrorModal.value = true;
    input.value = '';
    return;
  }

  currentCropFile.value = file;
  cropAspectRatio.value = 4 / 3;
  isAvatarCrop.value = false;
  showCropper.value = true;

  input.value = '';
}

async function handleCropConfirm(croppedFile: File) {
  if (isAvatarCrop.value) {
    try {
      isLoading.value = true;
      const res = await uploadImage(croppedFile);
      if ((res.code === 0 || res.code === 200) && res.data) {
        avatarUrl.value = res.data;
      } else {
        alert(res.message || '头像上传失败，请稍后重试');
      }
    } catch (error) {
      console.error('上传头像失败', error);
      alert('头像上传失败，请稍后重试');
    } finally {
      isLoading.value = false;
    }
  } else {
    try {
      isLoading.value = true;
      const res = await uploadImage(croppedFile);
      if ((res.code === 0 || res.code === 200) && res.data) {
        proofPhotos.value.push(res.data);
      } else {
        errorMessage.value = res.message || '爱宠证明上传失败，请稍后重试';
        showErrorModal.value = true;
      }
    } catch (error) {
      console.error('上传爱宠证明失败', error);
      errorMessage.value = '爱宠证明上传失败，请稍后重试';
      showErrorModal.value = true;
    } finally {
      isLoading.value = false;
    }
  }
  
  currentCropFile.value = null;
}

function handleCropCancel() {
  currentCropFile.value = null;
}

async function handleSave() {
  const payload: UpdateUserRequest = {
    userAge: formData.value.age,
    location: formData.value.location,
    proofText: formData.value.petProofIntro,
    proofPhoto: proofPhotos.value,
    sex: mapGenderToSex(formData.value.gender),
    avatarUrl: avatarUrl.value || undefined
  };

  isLoading.value = true;
  try {
    const res = await updateCurrentUser(payload);
    if (res.code === 0 || res.code === 200) {
      // 用户信息更新成功后，再删除已标记的文件
      const urlsToDelete = [...removedProofs.value];
      if (urlsToDelete.length) {
        const tasks = urlsToDelete.map(url => {
          const parts = url.split('/');
          const object = parts[parts.length - 1] || url;
          return deleteImage(object);
        });
        const results = await Promise.allSettled(tasks);
        const hasError = results.some(r => r.status === 'rejected' || (r.status === 'fulfilled' && (r.value as any)?.code !== 0 && (r.value as any)?.code !== 200));
        if (hasError) {
          errorMessage.value = '部分图片删除失败，请稍后重试';
          showErrorModal.value = true;
        } else {
          // 删除成功后清空列表
          removedProofs.value = [];
        }
      }

      // 重新获取用户信息以更新本地缓存，并通知其他组件更新
      await loadCurrentUser();
      window.dispatchEvent(new Event('current-user-updated'));

      showSuccessModal.value = true;
    } else {
      alert(res.message || '保存失败，请稍后重试');
    }
  } catch (error) {
    console.error('更新用户信息失败', error);
    alert('保存失败，请稍后重试');
  } finally {
    isLoading.value = false;
  }
}

function closeSuccessModal() {
  showSuccessModal.value = false;
}

onMounted(() => {
  loadCurrentUser();
});
</script>

<style scoped>
</style>

