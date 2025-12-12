<template>
  <div class="new-post-page">
    <div class="container">
      <main class="main-content">
        <!-- 返回按钮 -->
        <button 
          @click="router.back()" 
          class="mb-6 flex items-center gap-2 text-[#FF8C00] hover:text-[#e6722a] transition-colors"
          title="返回"
        >
          <i class="fa-solid fa-arrow-left text-lg"></i>
          <span class="font-medium">返回</span>
        </button>

        <h1 class="page-title">{{ isEditMode ? '编辑帖子' : '发布新帖子' }}</h1>

        <form @submit.prevent="submitPost">
          <!-- 帖子标题和内容 -->
          <div class="form-group">
            <h2 class="section-title">帖子信息</h2>
            <div class="form-group">
              <label for="post-title" class="required">帖子标题</label>
              <input type="text" id="post-title" v-model="postTitle" placeholder="请输入帖子标题" required />
            </div>
            <div class="form-group">
              <label for="post-content" class="required">帖子内容</label>
              <textarea
                id="post-content"
                v-model="postContent"
                @input="checkContent"
                placeholder="在这里输入你的帖子内容..."
                required
              ></textarea>
              <div class="char-count">
                <span :class="{'text-red-500': charCount > MAX_CHARS}">{{ charCount }}</span> 字
              </div>
            </div>
          </div>

          <!-- 文件上传 -->
          <div class="form-group mt-6">
            <h2 class="section-title">图片/视频上传</h2>
            <div
              class="upload-area"
              @click="fileInput?.click()"
              @dragover.prevent="isDragOver = true"
              @dragleave.prevent="isDragOver = false"
              @drop.prevent="handleFileDrop"
              :style="{borderColor: isDragOver ? 'var(--primary-color)' : 'var(--border-color)', backgroundColor: isDragOver ? 'rgba(255,140,0,0.05)' : 'transparent'}"
            >
              <div class="upload-icon"><i class="fa-solid fa-cloud-upload-alt"></i></div>
              <div class="upload-text">点击上传或拖拽文件到这里</div>
              <div class="upload-hint">
                支持 JPG, JPEG, PNG, WEBP, MP4 格式，最多5个文件
                <span v-if="uploadedFiles.length > 0" class="text-sm text-red-500 ml-2">({{ uploadedFiles.length }}/5)</span>
              </div>
              <input type="file" ref="fileInput" @change="handleFileSelect" multiple accept="image/jpeg,image/jpg,image/png,image/webp,video/mp4,.jpg,.jpeg,.png,.webp,.mp4" class="hidden" />
            </div>

            <div class="preview-container mt-4">
              <div v-for="(file, index) in uploadedFiles" :key="index" class="preview-item">
                <img
                  v-if="file.type.startsWith('image')"
                  :src="file.preview"
                  :alt="file.name"
                  @click="openImagePreview(file.remoteUrl || file.preview)"
                />
                <video
                  v-else-if="file.type.startsWith('video')"
                  :src="file.preview"
                  controls
                ></video>
                <div class="remove-btn" @click="removeFile(index)">&times;</div>
              </div>
            </div>
          </div>

          <!-- 表单操作 -->
          <div class="form-actions mt-6">
            <button type="button" class="btn btn-secondary" @click="cancelPost">取消</button>
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="!isFormValid || isSubmitting"
            >
              <template v-if="isSubmitting">
                <template v-if="uploadProgress.total > 0">
                  <span>上传中 ({{ uploadProgress.current }}/{{ uploadProgress.total }})</span>
                </template>
                <template v-else>
                  提交中...
                </template>
              </template>
              <template v-else>
                {{ isEditMode ? '保存修改' : '发布帖子' }}
              </template>
            </button>

            <!-- 上传进度条（显示在按钮下方） -->
            <div v-if="isSubmitting && uploadProgress.total > 0 && uploadProgress.files.length > 0" class="upload-progress-container">
              <div
                v-for="(file, index) in uploadProgress.files"
                :key="index"
                class="file-progress-item"
              >
                <div class="file-progress-name">{{ file.name }}</div>
                <div class="file-progress-bar">
                  <div
                    class="file-progress-fill"
                    :style="{ width: file.progress + '%' }"
                  ></div>
                </div>
                <div class="file-progress-percent">{{ file.progress }}%</div>
              </div>
            </div>
          </div>
        </form>
      </main>
    </div>

    <!-- 成功/取消模态框 -->
    <div v-if="showSuccessModal" class="modal" @click.self="showSuccessModal = false">
      <div class="modal-content">
        <div class="modal-icon">✅</div>
        <h2>{{ isEditMode ? '修改已提交' : '提交成功' }}</h2>
        <p>
          {{ isEditMode ? '您的帖子修改已提交成功，请等待审核结果。' : '您的帖子已提交成功，请等待审核结果。' }}
        </p>
        <div class="modal-buttons">
          <button class="btn btn-primary" @click="confirmSuccess">确定</button>
        </div>
      </div>
    </div>

    <div v-if="showCancelModal" class="modal" @click.self="showCancelModal = false">
      <div class="modal-content">
        <div class="modal-icon">⚠️</div>
        <h2>确认取消</h2>
        <p>您确定要取消发布吗？所有未保存的内容将会丢失。</p>
        <div class="modal-buttons">
          <button class="btn btn-secondary" @click="showCancelModal=false">继续编辑</button>
          <button class="btn btn-primary" @click="confirmCancel">确定取消</button>
        </div>
      </div>
    </div>

    <div v-if="showErrorModal" class="modal" @click.self="closeErrorModal">
      <div class="modal-content">
        <div class="modal-icon" style="color: var(--error-color);">❌</div>
        <h2>发布失败</h2>
        <p>{{ errorMessage }}</p>
        <div class="modal-buttons">
          <button class="btn btn-primary" @click="closeErrorModal">确定</button>
        </div>
      </div>
    </div>

    <!-- 图片大图预览 -->
    <div v-if="showImagePreview && previewImageUrl" class="modal" @click.self="closeImagePreview">
      <div class="modal-content">
        <div class="modal-icon">🖼️</div>
        <img :src="previewImageUrl" alt="预览图片" style="max-width: 100%; max-height: 60vh; object-fit: contain;" />
        <div class="modal-buttons">
          <button class="btn btn-primary" @click="closeImagePreview">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { createPost, getPostDetail, updatePost } from '@/api/postApi'; // 帖子相关接口
import { uploadMedia } from '@/api/storageApi'; // 媒体上传接口，返回后端真实 URL
import { isVideoUrl } from '@/utils/mediaUtils';

const route = useRoute();
const router = useRouter();

const MAX_CHARS = 500;
const MAX_FILES = 5;

interface UploadedMedia {
  name: string;
  preview: string;
  type: string;
  file: File | null; // 已有文件可能没有 File 对象
  remoteUrl?: string;
}

const postTitle = ref('');
const postContent = ref('');
const uploadedFiles = ref<UploadedMedia[]>([]);
const fileInput = ref<HTMLInputElement | null>(null);
const isDragOver = ref(false);
const isSubmitting = ref(false);
const uploadProgress = ref<{
  current: number;
  total: number;
  files: Array<{ name: string; progress: number }>;
}>({
  current: 0,
  total: 0,
  files: []
});

const showSuccessModal = ref(false);
const showCancelModal = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
const submittedPostId = ref<number | null>(null);
const showImagePreview = ref(false);
const previewImageUrl = ref<string | null>(null);

// 是否为编辑模式（从个人中心或其他地方带着帖子 ID 进入）
const editingPostId = ref<number | null>(null);
const isEditMode = computed(() => editingPostId.value !== null);

const charCount = computed(() => postContent.value.length);
const isFormValid = computed(() => postTitle.value.trim().length>0 && postContent.value.trim().length>0 && charCount.value<=MAX_CHARS);

const checkContent = () => {
  if(charCount.value>MAX_CHARS) postContent.value = postContent.value.substring(0,MAX_CHARS);
};

// 允许的文件格式
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
const ALLOWED_VIDEO_TYPES = ['video/mp4'];
const ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.webp', '.mp4'];

// 检查文件格式是否允许
const isFileTypeAllowed = (file: File): boolean => {
  const fileName = file.name.toLowerCase();
  const fileExtension = fileName.substring(fileName.lastIndexOf('.'));

  // 检查文件扩展名（主要依据扩展名，因为MIME类型可能不准确）
  if (!ALLOWED_EXTENSIONS.some(ext => fileName.endsWith(ext))) {
    console.warn('文件扩展名不支持:', fileExtension, '文件名:', fileName);
    return false;
  }

  // 检查MIME类型（放宽检查，因为某些浏览器可能返回不同的MIME类型）
  const allowedTypes = [...ALLOWED_IMAGE_TYPES, ...ALLOWED_VIDEO_TYPES];

  // 如果MIME类型为空或未知，但扩展名正确，也允许（某些浏览器可能不提供MIME类型）
  if (!file.type || file.type === '' || file.type === 'application/octet-stream') {
    console.log('文件MIME类型为空或未知，但扩展名正确，允许上传:', fileName);
    return true;
  }

  // 如果MIME类型匹配，允许
  if (allowedTypes.includes(file.type)) {
    return true;
  }

  // 对于图片，检查是否是image/*类型（某些浏览器可能返回image/pjpeg等）
  if (fileExtension.match(/\.(jpg|jpeg|png|webp)$/i) && file.type.startsWith('image/')) {
    console.log('文件是图片类型，允许上传:', fileName, file.type);
    return true;
  }

  // 对于视频，检查是否是video/*类型
  if (fileExtension.match(/\.mp4$/i) && file.type.startsWith('video/')) {
    console.log('文件是视频类型，允许上传:', fileName, file.type);
    return true;
  }

  console.warn('文件MIME类型不支持:', file.type, '文件名:', fileName);
  return false;
};

const processFiles = (files: FileList) => {
  const newFiles = Array.from(files);
  const invalidFiles: string[] = [];

  // 检查文件数量限制
  if (uploadedFiles.value.length + newFiles.length > MAX_FILES) {
    errorMessage.value = `最多只能上传${MAX_FILES}个文件`;
    showErrorModal.value = true;
    isDragOver.value = false;
    return;
  }

  newFiles.forEach(file => {
    if (!file) return;

    // 检查文件格式
    if (!isFileTypeAllowed(file)) {
      invalidFiles.push(file.name);
      return;
    }

    // 使用 URL.createObjectURL 创建预览（更高效，参考发布宠物页面）
    const preview = URL.createObjectURL(file);
    uploadedFiles.value.push({
      name: file.name,
      preview: preview,
      type: file.type,
      file: file
    });
  });

  // 显示错误提示
  if (invalidFiles.length > 0) {
    const fileList = invalidFiles.join('、');
    errorMessage.value = `以下文件格式不支持：${fileList}\n\n仅支持 JPG、JPEG、PNG、WEBP、MP4 格式的文件`;
    showErrorModal.value = true;
  }

  isDragOver.value = false;
};

const handleFileSelect = (e: Event)=>{ const t = e.target as HTMLInputElement; if(t.files) processFiles(t.files); t.value=''; };
const handleFileDrop = (e: DragEvent)=>{ isDragOver.value=false; if(e.dataTransfer?.files) processFiles(e.dataTransfer.files); };
const removeFile = (i:number)=>uploadedFiles.value.splice(i,1);

const openImagePreview = (url: string) => {
  if (!url) return;
  previewImageUrl.value = url;
  showImagePreview.value = true;
};

const closeImagePreview = () => {
  showImagePreview.value = false;
  previewImageUrl.value = null;
};

// 统一的媒体上传（简化：使用后端上传接口，无需手写XHR）
const uploadFileWithProgress = async (
  file: File,
  onProgress: (progress: number) => void
): Promise<{ success: boolean; url: string }> => {
  // 简单进度反馈：开始时 10%，成功后 100%
  onProgress(10);
  const isVideo = file.type.startsWith('video/');
  let res: any;

  try {
    res = await uploadMedia(file);
  } catch (err: any) {
    // 统一网络错误提示，便于定位后端/隧道问题
    const msg = err?.message || String(err);
    if (
      msg.includes('Failed to fetch') ||
      msg.includes('NetworkError') ||
      msg.includes('ERR_EMPTY_RESPONSE') ||
      msg.includes('网关') ||
      msg.includes('502')
    ) {
      throw new Error(isVideo
        ? '视频上传失败：无法连接上传服务（后端未响应或隧道断开）'
        : '图片上传失败：无法连接上传服务（后端未响应或隧道断开）');
    }
    throw err;
  }

  onProgress(100);

  // 兼容后端返回 {code,data} 或直接返回 URL 字符串
  const url = typeof res === 'string'
    ? res
    : res?.data;

  const okCode = res?.code === undefined || res?.code === 0 || res?.code === 200;

  if (okCode && url) {
    return { success: true, url: url as string };
  }
  const errMsg = res?.message || (isVideo ? '视频上传失败' : '图片上传失败');
  throw new Error(errMsg);
};


// ----------------------------------------
// 表单提交调用接口
// ----------------------------------------
const submitPost = async () => {
  console.log('submitPost 被调用', {
    isFormValid: isFormValid.value,
    title: postTitle.value,
    content: postContent.value.length,
    isSubmitting: isSubmitting.value
  });

  if(!isFormValid.value) {
    console.warn('表单验证失败，无法提交');
    errorMessage.value = '请填写标题和内容（内容不能超过500字）';
    showErrorModal.value = true;
    return;
  }

  if (isSubmitting.value) {
    console.warn('正在提交中，忽略重复提交');
    return;
  }

  try{
    isSubmitting.value = true;
    console.log('开始提交，isSubmitting设置为true');
    let res;

    if (isEditMode.value && editingPostId.value !== null) {
      console.log('开始调用 updatePost 接口...');

      // 编辑模式：并行上传所有新文件（提高速度）
      const filesToUpload: Array<{ item: typeof uploadedFiles.value[0], index: number }> = [];

      // 先收集需要上传的文件
      uploadedFiles.value.forEach((item, index) => {
        // 如果已经有远程URL（原有文件），直接使用
        if (item.remoteUrl) {
          return;
        }

        // 新上传的文件
        if (!item.file) {
          return;
        }

        filesToUpload.push({ item, index });
      });

      // 设置上传进度
      uploadProgress.value = {
        current: 0,
        total: filesToUpload.length,
        files: filesToUpload.map(f => ({ name: f.item.name, progress: 0 }))
      };

      // 并行上传所有文件（带进度追踪）
      const uploadPromises = filesToUpload.map(async ({ item, index }) => {
        const isVideo = item.type.startsWith('video/');
        const fileSizeMB = (item.file!.size / (1024 * 1024)).toFixed(2);

        console.log(`[编辑模式] 开始上传${isVideo ? '视频' : '图片'}文件:`, item.name, item.type, `大小: ${fileSizeMB}MB`);

        // 检查文件大小
        const maxSizeMB = isVideo ? 100 : 10;
        if (item.file!.size > maxSizeMB * 1024 * 1024) {
          throw new Error(`${item.name} (${isVideo ? '视频' : '图片'}): 文件大小超过限制（最大${maxSizeMB}MB）`);
        }

        // 更新进度回调
        const updateProgress = (progress: number) => {
          if (uploadProgress.value.files[index]) {
            uploadProgress.value.files[index].progress = progress;
          }
        };

        console.log(`[编辑模式] 准备调用上传接口...`);
        const uploadRes = await uploadFileWithProgress(item.file!, updateProgress);

        console.log(`[编辑模式] 上传响应:`, uploadRes);

        // 上传完成，设置为100%
        if (uploadProgress.value.files[index]) {
          uploadProgress.value.files[index].progress = 100;
        }

        if (uploadRes.success && uploadRes.url) {
          item.remoteUrl = uploadRes.url;
          console.log(`[编辑模式] ${isVideo ? '视频' : '图片'}上传成功`);
          return { success: true, url: uploadRes.url, name: item.name };
        }

        throw new Error(`${item.name} (${isVideo ? '视频' : '图片'}): 上传失败`);
      });

      // 等待所有上传完成
      const uploadResults = await Promise.allSettled(uploadPromises);

      // 处理上传结果
      const mediaUrls: string[] = [];
      const uploadErrors: string[] = [];

      // 先添加已有文件的URL
      uploadedFiles.value.forEach(item => {
        if (item.remoteUrl && !filesToUpload.some(f => f.item === item)) {
          mediaUrls.push(item.remoteUrl);
        }
      });

      // 处理上传结果
      uploadResults.forEach((result, index) => {
        uploadProgress.value.current = index + 1;

        if (result.status === 'fulfilled' && result.value.success) {
          mediaUrls.push(result.value.url);
        } else {
          let errorMsg = result.status === 'rejected'
            ? result.reason?.message || result.reason?.toString() || '上传出错'
            : '上传失败';

          if (errorMsg.includes('超时') || errorMsg.includes('timeout')) {
            const isVideo = filesToUpload[index]?.item.type.startsWith('video/');
            errorMsg = `上传超时（${isVideo ? '视频文件较大，请检查网络连接或稍后重试' : '请检查网络连接'}）`;
          }

          uploadErrors.push(errorMsg);
        }
      });

      // 重置上传进度
      uploadProgress.value = { current: 0, total: 0, files: [] };

      if (uploadErrors.length > 0) {
        errorMessage.value = `以下文件上传失败：\n${uploadErrors.join('\n')}`;
        showErrorModal.value = true;
        isSubmitting.value = false;
        return;
      }

      // 构建更新请求体
      const updateData: any = {
        title: postTitle.value.trim(),
        content: postContent.value.trim()
      };

      // 如果有媒体文件，添加mediaUrls字段
      if (mediaUrls.length > 0) {
        updateData.mediaUrls = mediaUrls;
      }

      console.log('更新帖子请求体:', JSON.stringify(updateData, null, 2));
      console.log('准备调用 updatePost 接口...');

      // 添加超时保护（30秒）
      const updateTimeout = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('更新帖子超时，请检查网络连接')), 30000);
      });

      // 使用更新帖子的接口 PUT /api/post/{id}
      // 后端需要允许帖子作者更新自己的帖子（不需要管理员权限）
      res = await Promise.race([
        updatePost(editingPostId.value, updateData),
        updateTimeout
      ]) as any;

      console.log('updatePost 接口调用完成');
      console.log('更新帖子响应:', res);
      console.log('更新帖子响应数据结构:', JSON.stringify(res, null, 2));
    } else {
      console.log('开始调用 createPost 接口...');

      // 先把本地选择的文件（图片或视频）上传到后端存储，获得真实的在线 URL
      // 使用并行上传提高速度
      const filesToUpload: Array<{ item: typeof uploadedFiles.value[0], index: number }> = [];

      // 先收集需要上传的文件
      uploadedFiles.value.forEach((item, index) => {
        // 如果已经有远程URL（编辑模式下的已有文件），直接使用，不需要重新上传
        if (item.remoteUrl) {
          console.log('使用已有文件URL:', item.remoteUrl);
          return;
        }

        // 需要上传的新文件
        if (!item.file) {
          console.warn('文件项没有file对象，跳过:', item.name);
          return;
        }

        filesToUpload.push({ item, index });
      });

      // 设置上传进度
      uploadProgress.value = {
        current: 0,
        total: filesToUpload.length,
        files: filesToUpload.map(f => ({ name: f.item.name, progress: 0 }))
      };

      // 并行上传所有文件（大幅提高速度，带进度追踪）
      const uploadPromises = filesToUpload.map(async ({ item, index }) => {
        const isVideo = item.type.startsWith('video/');
        const fileSizeMB = (item.file!.size / (1024 * 1024)).toFixed(2);

        console.log(`[创建模式] 开始上传${isVideo ? '视频' : '图片'}文件:`, item.name, item.type, `大小: ${fileSizeMB}MB`);

        // 检查文件大小（视频最大100MB，图片最大10MB）
        const maxSizeMB = isVideo ? 100 : 10;
        if (item.file!.size > maxSizeMB * 1024 * 1024) {
          throw new Error(`${item.name} (${isVideo ? '视频' : '图片'}): 文件大小超过限制（最大${maxSizeMB}MB）`);
        }

        // 更新进度回调
        const updateProgress = (progress: number) => {
          if (uploadProgress.value.files[index]) {
            uploadProgress.value.files[index].progress = progress;
          }
        };

        // 根据文件类型选择对应的上传接口
        console.log(`[创建模式] 准备上传${isVideo ? '视频' : '图片'}，使用统一上传接口`);

        const uploadRes = await uploadFileWithProgress(item.file!, updateProgress);

        console.log(`[创建模式] ${isVideo ? '视频' : '图片'}上传响应:`, uploadRes);

        // 上传完成，设置为100%
        if (uploadProgress.value.files[index]) {
          uploadProgress.value.files[index].progress = 100;
        }

        if (uploadRes.success && uploadRes.url) {
          const uploadedUrl = uploadRes.url;
          console.log(`[创建模式] ${isVideo ? '视频' : '图片'}上传成功，URL:`, uploadedUrl);
          // 保存远程URL到文件项中
          item.remoteUrl = uploadedUrl;
          return { success: true, url: uploadedUrl, name: item.name };
        }

        throw new Error(`${item.name} (${isVideo ? '视频' : '图片'}): 上传失败`);
      });

      // 等待所有上传完成
      const uploadResults = await Promise.allSettled(uploadPromises);

      // 处理上传结果
      const mediaUrls: string[] = [];
      const uploadErrors: string[] = [];

      // 先添加已有文件的URL
      uploadedFiles.value.forEach(item => {
        if (item.remoteUrl && !filesToUpload.some(f => f.item === item)) {
          mediaUrls.push(item.remoteUrl);
        }
      });

      // 处理上传结果
      uploadResults.forEach((result, index) => {
        uploadProgress.value.current = index + 1;

        if (result.status === 'fulfilled' && result.value.success) {
          mediaUrls.push(result.value.url);
        } else {
          let errorMsg = result.status === 'rejected'
            ? result.reason?.message || result.reason?.toString() || '上传出错'
            : '上传失败';

          // 针对500错误提供更友好的提示
          if (errorMsg.includes('500') || errorMsg.includes('Internal Server Error') || errorMsg.includes('HTTP error! status: 500')) {
            errorMsg = '服务器内部错误，可能是视频格式不支持或文件过大，请检查文件后重试';
          }

          uploadErrors.push(errorMsg);
          console.log(`[创建模式] 添加错误到列表，当前错误数量: ${uploadErrors.length}，错误内容:`, uploadErrors);
        }
      });

      // 重置上传进度
      uploadProgress.value = { current: 0, total: 0, files: [] };

      console.log('所有媒体文件上传完成，mediaUrls:', mediaUrls);
      console.log('上传错误列表:', uploadErrors);
      console.log('上传错误数量:', uploadErrors.length);

      // 如果有上传错误，显示错误提示并阻止提交
      if (uploadErrors.length > 0) {
        console.warn('检测到上传错误，阻止提交');
        errorMessage.value = `以下文件上传失败，请检查文件格式和大小：\n\n${uploadErrors.join('\n')}\n\n提示：\n- 图片仅支持 JPG、JPEG、PNG、WEBP 格式\n- 视频仅支持 MP4 格式\n- 请确保文件大小在允许范围内\n- 如果视频上传失败，可能是服务器问题，请稍后重试`;
        showErrorModal.value = true;
        isSubmitting.value = false;
        console.log('已阻止提交，isSubmitting重置为false');
        return;
      }

      console.log('没有上传错误，继续提交');

      // 构建请求体，确保符合接口文档要求
      const requestBody: {
        title: string;
        content: string;
        mediaUrls?: string[];
      } = {
        title: postTitle.value.trim(),
        content: postContent.value.trim()
      };

      // 只有当有图片URL时才添加mediaUrls字段
      if (mediaUrls.length > 0) {
        requestBody.mediaUrls = mediaUrls;
      }

      console.log('发布帖子请求体:', JSON.stringify(requestBody, null, 2));
      console.log('准备调用 createPost 接口...');

      // 添加超时保护（30秒）
      const createTimeout = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('发布帖子超时，请检查网络连接')), 30000);
      });

      res = await Promise.race([
        createPost(requestBody),
        createTimeout
      ]) as any;

      console.log('createPost 接口调用完成');
    }

    console.log('接口返回:', res);
    console.log('接口返回数据结构:', JSON.stringify(res, null, 2));

    // 检查响应是否存在
    if (!res) {
      console.error('接口返回为空');
      throw new Error('服务器无响应，请稍后重试');
    }

    // 检查响应是否成功（允许 code 为 200、0 或 undefined）
    const responseCode = res.code;
    if (responseCode !== undefined && responseCode !== null && responseCode !== 200 && responseCode !== 0) {
      console.error('接口返回错误码:', responseCode, res.message);
      throw new Error(res.message || '发布失败，请稍后重试');
    }

    console.log('接口调用成功，准备显示成功提示');

    // 后端可能返回 postId 或 id，都尝试一下
    // 更新帖子时，使用编辑的帖子ID；创建帖子时，从响应中获取ID
    try {
      if (isEditMode.value && editingPostId.value !== null) {
        // 更新帖子时，使用编辑的帖子ID
        submittedPostId.value = editingPostId.value;
        console.log('更新帖子，使用编辑的帖子ID:', submittedPostId.value);
      } else {
        // 创建帖子时，从响应中获取ID
        let postId = null;
        if (res.data) {
          if (typeof res.data === 'object') {
            postId = res.data.postId || res.data.id || res.data.post?.postId || res.data.post?.id;
          } else if (typeof res.data === 'number') {
            postId = res.data;
          }
        }
        submittedPostId.value = postId || undefined;
        console.log('创建帖子，获取到的帖子ID:', submittedPostId.value, '原始data:', res.data);
      }

      console.log('准备显示成功模态框');
      showSuccessModal.value = true;
      console.log('成功模态框已显示');
    } catch (modalError: any) {
      console.error('显示成功模态框时出错:', modalError);
      // 即使显示模态框失败，也要重置提交状态
      throw modalError;
    }

  }catch(err: any){
    console.error('========== 提交帖子错误详情 ==========');
    console.error('错误消息:', err?.message);
    console.error('HTTP状态码:', err?.status);
    console.error('请求URL:', err?.url);
    console.error('响应数据:', err?.responseData);
    console.error('完整错误对象:', err);
    console.error('错误堆栈:', err?.stack);
    console.error('=====================================');

    // 尝试从错误对象中提取更详细的错误信息
    let errorMsg = err?.message || err?.toString() || '发布失败，请检查网络连接或稍后重试';

    // 如果有响应数据，尝试提取更详细的错误信息
    if (err?.responseData) {
      const responseData = err.responseData;
      if (typeof responseData === 'object') {
        const backendMsg = responseData.message || responseData.msg || responseData.error || responseData.details;
        if (backendMsg) {
          errorMsg = backendMsg;
        }
      }
    }

    // 根据错误消息提供更友好的提示
    if (errorMsg.includes('无此权限') || errorMsg.includes('permission') || errorMsg.includes('权限')) {
      if (isEditMode.value) {
        errorMsg = `权限错误：${errorMsg}\n\n问题说明：\n后端要求用户具有特殊权限才能编辑帖子，但这是不合理的。\n\n正确的逻辑应该是：\n✅ 帖子作者可以编辑自己的帖子（不需要特殊权限）\n✅ 管理员可以编辑任何帖子\n\n请后端开发人员修改 PUT /api/post/{id} 接口的权限检查逻辑：\n1. 检查当前用户是否是帖子作者\n2. 如果是作者，允许编辑\n3. 如果是管理员，也允许编辑\n4. 否则才返回权限错误`;
      } else {
        errorMsg = `权限错误：${errorMsg}\n\n请检查您是否有发布帖子的权限。`;
      }
    } else if (errorMsg.includes('401') || errorMsg.includes('未登录')) {
      errorMsg = '请先登录后再试（401）。';
    } else if (errorMsg.includes('403') || errorMsg.includes('禁止')) {
      if (isEditMode.value) {
        errorMsg = '您没有权限编辑此帖子（403）。后端需要允许帖子作者编辑自己的帖子。';
      } else {
        errorMsg = '您没有权限发布帖子（403）。';
      }
    } else if (errorMsg.includes('500')) {
      if (isEditMode.value && errorMsg.includes('权限')) {
        errorMsg += '\n\n注意：后端返回500错误，但错误消息是权限问题。这可能是后端权限检查逻辑有问题。';
      }
    }

    errorMessage.value = errorMsg;
    showErrorModal.value = true;
  } finally {
    console.log('提交流程结束，重置 isSubmitting 为 false');
    isSubmitting.value = false;
    // 重置上传进度
    uploadProgress.value = { current: 0, total: 0, files: [] };
  }
};

const cancelPost = ()=>{ if(postTitle.value||postContent.value||uploadedFiles.value.length>0){ showCancelModal.value=true } else { router.back() } };
const confirmSuccess = ()=>{
  postTitle.value=''; postContent.value=''; uploadedFiles.value=[];
  showSuccessModal.value=false;

  if (isEditMode.value && editingPostId.value !== null) {
    // 如果来源于个人中心「我的帖子」，编辑后返回该列表
    if (route.query.from === 'myPosts') {
      router.push({ path: '/user-center', query: { menu: 'posts' } });
      return;
    }
    // 默认编辑完成后回到帖子详情
    router.push({ name: 'PostDetail', params: { id: editingPostId.value.toString() } });
  } else {
    // 发布成功后，返回论坛页面并恢复之前的页码（不跳转到帖子详情）
    const fromPage = route.query.fromPage;
    if (fromPage && typeof fromPage === 'string') {
      const pageNum = parseInt(fromPage, 10);
      if (!isNaN(pageNum) && pageNum > 0) {
        // 跳转到论坛页面并传递页码参数
        router.push({
          name: 'Forum',
          query: { page: pageNum.toString() }
        });
        return;
      }
    }
    // 如果没有页码信息，返回论坛首页（第1页）
    router.push({ name: 'Forum' });
  }
};
const confirmCancel = ()=>{ showCancelModal.value=false; router.back() };
const closeErrorModal = ()=>{ showErrorModal.value=false; errorMessage.value=''; };

// 加载已有的媒体文件（图片/视频）到上传列表中
const loadExistingMedia = (mediaUrls: string[]) => {
  if (!mediaUrls || !Array.isArray(mediaUrls) || mediaUrls.length === 0) {
    return;
  }

  mediaUrls.forEach((url) => {
    if (!url || typeof url !== 'string') return;

    // 判断是图片还是视频
    const isVideo = isVideoUrl(url);

    // 创建媒体项
    uploadedFiles.value.push({
      name: isVideo ? '已有视频' : '已有图片',
      preview: url, // 使用远程URL作为预览
      type: isVideo ? 'video/mp4' : 'image/jpeg',
      file: null as any, // 已有文件没有 File 对象
      remoteUrl: url // 保存远程URL，避免重复上传
    });
  });
};

// 初始化：如果带有 id 参数，则认为是编辑模式，先拉取帖子详情
onMounted(async () => {
  const idParam = route.query.id || route.params.id;
  const modeParam = route.query.mode;

  console.log('PostNew onMounted - idParam:', idParam, 'modeParam:', modeParam);
  console.log('路由query参数:', route.query);
  console.log('路由params参数:', route.params);

  const numId = typeof idParam === 'string' ? parseInt(idParam, 10) : Number(idParam);
  if (!isNaN(numId) && numId > 0) {
    editingPostId.value = numId;
    console.log('进入编辑模式，帖子ID:', numId);

    // 检查是否从"我的帖子"页面跳转过来，可能带有基础信息
    const fromMyPosts = route.query.from === 'myPosts';
    if (fromMyPosts) {
      console.log('从"我的帖子"页面跳转过来，尝试使用路由参数中的基础信息');
      const titleFromQuery = route.query.title as string;
      const contentFromQuery = route.query.content as string;
      const imagesFromQuery = route.query.images as string;

      if (titleFromQuery || contentFromQuery) {
        console.log('使用路由参数中的帖子信息');
        postTitle.value = titleFromQuery || '';
        postContent.value = contentFromQuery || '';

        // 解析图片
        if (imagesFromQuery) {
          try {
            const parsedImages = JSON.parse(imagesFromQuery);
            if (Array.isArray(parsedImages)) {
              loadExistingMedia(parsedImages);
            }
          } catch (e) {
            console.warn('解析图片参数失败:', e);
          }
        }
      }
    }

    try {
      console.log('开始获取帖子详情，ID:', numId);
      const res = await getPostDetail(numId);
      console.log('获取帖子详情响应:', res);
      console.log('响应数据结构:', {
        code: res.code,
        message: res.message,
        data: res.data,
        dataType: typeof res.data,
        dataIsNull: res.data === null,
        dataIsUndefined: res.data === undefined
      });
      console.log('完整响应对象（JSON）:', JSON.stringify(res, null, 2));
      console.log('响应对象的所有键:', Object.keys(res));

      // 检查响应是否成功（允许 code 为 200、0 或 undefined）
      const isSuccess = (res.code === 0 || res.code === 200 || res.code === undefined);

      if (isSuccess && res.data) {
        const data: any = res.data;
        console.log('帖子详情数据:', data);

        // 设置标题和内容
        postTitle.value = data.title || '';
        postContent.value = data.content || data.summary || '';
        console.log('已设置标题和内容:', { title: postTitle.value, contentLength: postContent.value.length });

        // 加载已有的图片和视频
        let mediaUrls: string[] = [];

        // 优先使用 mediaUrls（可能是数组或字符串）
        if (Array.isArray(data.mediaUrls)) {
          mediaUrls = data.mediaUrls.filter((url: any) => url && typeof url === 'string');
        } else if (data.mediaUrls && typeof data.mediaUrls === 'string') {
          try {
            const parsed = JSON.parse(data.mediaUrls);
            if (Array.isArray(parsed)) {
              mediaUrls = parsed.filter((url: any) => url && typeof url === 'string');
            } else {
              mediaUrls = [data.mediaUrls];
            }
          } catch {
            // 如果不是JSON，直接作为单个URL
            mediaUrls = [data.mediaUrls];
          }
        }

        // 如果没有 mediaUrls，尝试使用 images 字段
        if (mediaUrls.length === 0 && data.images) {
          if (Array.isArray(data.images)) {
            mediaUrls = data.images.filter((url: any) => url && typeof url === 'string');
          } else if (typeof data.images === 'string') {
            try {
              const parsed = JSON.parse(data.images);
              if (Array.isArray(parsed)) {
                mediaUrls = parsed.filter((url: any) => url && typeof url === 'string');
              } else {
                mediaUrls = [data.images];
              }
            } catch {
              mediaUrls = [data.images];
            }
          }
        }

        console.log('解析到的媒体URL列表:', mediaUrls);

        if (mediaUrls.length > 0) {
          loadExistingMedia(mediaUrls);
          console.log('已加载', mediaUrls.length, '个媒体文件到上传列表');
        } else {
          console.log('该帖子没有媒体文件');
        }
      } else if (!isSuccess) {
        // 响应码不是成功状态
        console.warn('获取帖子详情失败，响应码:', res.code, '消息:', res.message);
        errorMessage.value = `获取帖子详情失败：${res.message || '未知错误'}`;
        showErrorModal.value = true;
      } else {
        // 响应码成功但 data 为空
        console.warn('获取帖子详情响应成功，但 data 为空。响应:', res);
        console.warn('可能原因：1. 后端返回的 data 字段为空 2. 帖子不存在 3. 数据结构不匹配');
        console.warn('完整响应对象:', JSON.stringify(res, null, 2));

        // 尝试多种方式解析数据
        let data: any = null;

        // 方式1: 直接使用 res.data（如果存在）
        if (res.data) {
          data = res.data;
          console.log('方式1: 使用 res.data');
        }
        // 方式2: 如果 res.data 为空，但 res 本身有帖子字段，尝试使用 res
        else if (res && typeof res === 'object') {
          // 检查 res 是否直接包含帖子数据（没有包装在 data 中）
          if (res.title || res.postId || res.id) {
            data = res;
            console.log('方式2: 响应本身包含帖子数据，直接使用 res');
          }
          // 方式3: 检查是否有其他字段包含数据
          else if ((res as any).result) {
            data = (res as any).result;
            console.log('方式3: 使用 res.result');
          } else if ((res as any).content) {
            data = (res as any).content;
            console.log('方式4: 使用 res.content');
          }
        }

        if (data) {
          console.log('成功解析到数据:', data);
          // 设置标题和内容
          postTitle.value = data.title || '';
          postContent.value = data.content || data.summary || '';
          console.log('已设置标题和内容:', { title: postTitle.value, contentLength: postContent.value.length });

          // 加载已有的图片和视频
          let mediaUrls: string[] = [];

          // 优先使用 mediaUrls（可能是数组或字符串）
          if (Array.isArray(data.mediaUrls)) {
            mediaUrls = data.mediaUrls.filter((url: any) => url && typeof url === 'string');
          } else if (data.mediaUrls && typeof data.mediaUrls === 'string') {
            try {
              const parsed = JSON.parse(data.mediaUrls);
              if (Array.isArray(parsed)) {
                mediaUrls = parsed.filter((url: any) => url && typeof url === 'string');
              } else {
                mediaUrls = [data.mediaUrls];
              }
            } catch {
              mediaUrls = [data.mediaUrls];
            }
          }

          // 如果没有 mediaUrls，尝试使用 images 字段
          if (mediaUrls.length === 0 && data.images) {
            if (Array.isArray(data.images)) {
              mediaUrls = data.images.filter((url: any) => url && typeof url === 'string');
            } else if (typeof data.images === 'string') {
              try {
                const parsed = JSON.parse(data.images);
                if (Array.isArray(parsed)) {
                  mediaUrls = parsed.filter((url: any) => url && typeof url === 'string');
                } else {
                  mediaUrls = [data.images];
                }
              } catch {
                mediaUrls = [data.images];
              }
            }
          }

          console.log('解析到的媒体URL列表:', mediaUrls);

          if (mediaUrls.length > 0) {
            loadExistingMedia(mediaUrls);
            console.log('已加载', mediaUrls.length, '个媒体文件到上传列表');
          } else {
            console.log('该帖子没有媒体文件');
          }
        } else {
          console.error('无法解析帖子数据，响应对象:', res);

          // 如果已经从路由参数加载了基础信息，可以继续编辑
          if (postTitle.value || postContent.value) {
            console.log('已从路由参数加载基础信息，可以继续编辑');
            // 不显示错误，允许用户继续编辑
            // 只是提示用户某些信息可能不完整
            console.warn('注意：后端返回 data 为空，可能无法获取完整的帖子信息（如媒体文件）。');
            console.warn('但可以使用路由参数中的基础信息继续编辑。');
          } else {
            // 如果没有任何数据，显示错误
            errorMessage.value = `获取帖子详情失败：后端返回 data 为空。\n\n可能原因：\n1. 帖子不存在（ID: ${numId}）\n2. 您没有权限查看此帖子\n3. 后端接口有问题\n\n响应: ${JSON.stringify(res, null, 2)}\n\n如果您是从"我的帖子"页面跳转过来的，请返回重试。`;
            showErrorModal.value = true;
          }
        }
      }
    } catch (err: any) {
      console.error('获取帖子详情失败(编辑模式)', err);
      errorMessage.value = `获取帖子详情失败：${err?.message || '网络错误，请稍后重试'}`;
      showErrorModal.value = true;
    }
  } else {
    console.log('非编辑模式，进入新建帖子模式');
  }
});
</script>

<style scoped>
/* --- CSS 变量 --- */
.new-post-page {
  --primary-color: #FF8C00;
  --text-orange: #FF8C00;
  --bg-color: #F8F9FB;
  --card-bg: #FFFFFF;
  --text-main: #333333;
  --text-sub: #666666;
  --border-color: #e0e0e0;
  --error-color: #D9534F;
}

.new-post-page {
  background-color: var(--bg-color);
  min-height: 100vh;
  padding: 30px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  width: 100%;
}

.main-content {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  width: 80%;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 30px;
  color: var(--text-main);
  text-align: center;
}

.section-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 25px;
  color: var(--text-main);
  padding-bottom: 12px;
  border-bottom: 2px solid #ffe4cc;
}

.form-group {
  margin-bottom: 25px;
}

label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: var(--text-sub);
  font-size: 16px;
}

.required::after {
  content: " *";
  color: var(--error-color);
}

input,
textarea {
  width: 100%;
  padding: 14px 18px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
  font-family: inherit;
}

input:focus,
textarea:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 3px rgba(255, 140, 0, 0.1);
}

textarea {
  min-height: 250px;
  resize: vertical;
  line-height: 1.6;
}

.upload-area {
  border: 2px dashed var(--border-color);
  border-radius: 10px;
  padding: 50px 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: var(--primary-color);
  background-color: rgba(255, 140, 0, 0.02);
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: var(--text-sub);
}

.upload-text {
  color: var(--text-sub);
  margin-bottom: 12px;
  font-size: 18px;
}

.upload-hint {
  font-size: 14px;
  color: #999;
}

.preview-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 20px;
}

.preview-item {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.preview-item:hover {
  transform: scale(1.03);
}

.preview-item img,
.preview-item video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.2s;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.9);
}

.char-count {
  text-align: right;
  font-size: 14px;
  color: #888;
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

.btn {
  padding: 14px 40px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  min-width: 140px;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #E67A2A;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(255, 140, 0, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 上传进度样式 */
.upload-progress-container {
  width: 100%;
  margin-top: 15px;
  padding: 15px;
  background: rgba(255, 140, 0, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 140, 0, 0.2);
}

.file-progress-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 13px;
}

.file-progress-item:last-child {
  margin-bottom: 0;
}

.file-progress-name {
  min-width: 100px;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-main);
  font-weight: 500;
}

.file-progress-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.file-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF8C00, #FFA500);
  border-radius: 4px;
  transition: width 0.2s ease;
  box-shadow: 0 0 6px rgba(255, 140, 0, 0.4);
}

.file-progress-percent {
  min-width: 45px;
  text-align: right;
  color: var(--text-main);
  font-weight: 600;
  font-size: 12px;
}

.btn-secondary {
  background: #f0f0f0;
  color: var(--text-main);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 模态框 */
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: var(--card-bg);
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.modal-icon {
  font-size: 70px;
  margin-bottom: 25px;
}

.modal h2 {
  color: var(--text-main);
  margin-bottom: 20px;
  font-size: 24px;
}

.modal p {
  margin-bottom: 30px;
  color: var(--text-sub);
  font-size: 16px;
  line-height: 1.6;
}

.modal-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.hidden {
  display: none;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    width: 85%;
  }
}

@media (max-width: 992px) {
  .main-content {
    width: 90%;
    padding: 30px;
  }

  .container {
    padding: 0 15px;
  }
}

@media (max-width: 768px) {
  .form-actions {
    flex-direction: column;
    gap: 15px;
  }

  .btn {
    width: 100%;
  }

  .preview-item {
    width: 120px;
    height: 120px;
  }

  .upload-area {
    padding: 40px 20px;
  }

  .main-content {
    width: 95%;
    padding: 25px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 20px 15px;
    width: 100%;
  }

  .page-title {
    font-size: 26px;
  }

  .section-title {
    font-size: 20px;
  }

  .preview-item {
    width: 100px;
    height: 100px;
  }

  .upload-area {
    padding: 30px 15px;
  }
}
</style>
