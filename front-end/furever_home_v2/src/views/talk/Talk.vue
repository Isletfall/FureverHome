<template>
  <div class="flex flex-col h-screen bg-[#F8F9FB]">
    <!-- 返回按钮 -->
    <div class="max-w-[1200px] w-full mx-auto px-5 pt-5">
      <button 
        @click="router.back()" 
        class="flex items-center gap-2 text-[#FF8C00] hover:text-[#e6722a] transition-colors"
        title="返回"
      >
        <i class="fa-solid fa-arrow-left text-lg"></i>
        <span class="font-medium">返回</span>
      </button>
    </div>

    <!-- 主要内容区域 -->
    <main class="flex-1 max-w-[1200px] w-full mx-auto mt-2 mb-5 px-5 flex gap-5 min-h-0">
      <!-- 左侧：联系人列表 -->
      <aside class="w-80 bg-white rounded-xl shadow-sm flex flex-col overflow-hidden">
        <div class="p-5 pb-2.5">
          <div class="text-xl font-bold mb-4 text-[#111]">消息</div>
          <div class="relative">
            <i class="fa-solid fa-magnifying-glass absolute left-3 top-1/2 -translate-y-1/2 text-[#9CA3AF] text-sm"></i>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索联系人..."
              class="w-full py-2.5 pl-9 pr-2.5 border border-[#F3F4F6] bg-[#F9FAFB] rounded-full text-sm outline-none transition-all focus:border-[#FF8C00] focus:bg-white"
            />
          </div>
        </div>

        <div class="flex-1 overflow-y-auto py-2.5">
          <div
            v-for="contact in filteredContacts"
            :key="contact.id"
            :class="[
              'flex items-center px-5 py-4 cursor-pointer transition-colors border-l-[3px] border-transparent',
              contact.conversationId
                ? (activeConversationId === contact.conversationId ? 'bg-[#FFF7ED] border-l-[#FF8C00]' : 'hover:bg-[#F9FAFB]')
                : (pendingTargetUserId === contact.targetUserId ? 'bg-[#FFF7ED] border-l-[#FF8C00]' : 'hover:bg-[#F9FAFB]')
            ]"
            @click="selectContact(contact)"
          >
            <div
              :class="[
                'w-[45px] h-[45px] rounded-full flex items-center justify-center font-bold text-white text-base mr-3 flex-shrink-0 overflow-hidden',
                contact.avatarColor
              ]"
            >
              <img
                v-if="contact.avatarUrl"
                :src="contact.avatarUrl"
                alt="avatar"
                class="w-full h-full object-cover"
              />
              <span v-else>{{ contact.avatar }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between mb-1">
                <span class="font-bold text-[15px] text-[#111]">{{ contact.name }}</span>
                <span class="text-xs text-[#9CA3AF]">{{ contact.time }}</span>
              </div>
              <div class="flex items-center">
                <div class="text-[13px] text-[#6B7280] whitespace-nowrap overflow-hidden text-ellipsis flex-1">
                  {{ contact.lastMessage || '开始新的对话' }}
                </div>
                <span v-if="contact.unread" class="bg-[#EF4444] text-white text-[10px] px-1.5 py-0.5 rounded-full ml-1.5">
                  {{ contact.unread }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧：聊天窗口 -->
      <section class="flex-1 bg-white rounded-xl shadow-sm flex flex-col overflow-hidden">
        <!-- 聊天头部 -->
        <div class="px-6 py-4 border-b border-[#E5E7EB] flex items-center justify-between h-[70px]">
          <div v-if="activeContact" class="flex items-center gap-3">
            <div
              :class="[
                'w-10 h-10 rounded-full flex items-center justify-center font-bold text-white text-base overflow-hidden cursor-pointer',
                activeContact.avatarColor
              ]"
              @click="goToUserProfile(activeContact.targetUserId)"
            >
              <img
                v-if="activeContact.avatarUrl"
                :src="activeContact.avatarUrl"
                alt="avatar"
                class="w-full h-full object-cover"
              />
              <span v-else>{{ activeContact.avatar }}</span>
            </div>
            <div 
              class="font-bold cursor-pointer transition-colors hover:text-[#FF8C00]"
              @click="goToUserProfile(activeContact.targetUserId)"
            >
              {{ activeContact.name }}
            </div>
          </div>
        </div>

        <!-- 消息区域 -->
        <div
          ref="messageListRef"
          class="flex-1 bg-[#F9FAFB] p-5 overflow-y-auto flex flex-col gap-5"
        >
          <div
            v-for="message in messages"
            :key="message.id"
            :class="[
              'flex flex-col max-w-[70%]',
              message.isSent ? 'self-end items-end' : 'self-start'
            ]"
          >
            <div
              v-if="isImageBubble(message)"
              :class="[
                'rounded-xl relative shadow-sm overflow-hidden max-w-[220px]',
                message.isSent ? 'self-end' : 'self-start',
              ]"
            >
              <img
                :src="message.content"
                alt="image"
                class="max-w-[220px] max-h-[260px] rounded-xl object-cover border border-[#E5E7EB] cursor-zoom-in"
                @click="openImagePreview(message.content)"
              />
            </div>
            <div
              v-else
              :class="[
                'px-4 py-3 rounded-xl text-sm leading-relaxed relative shadow-sm',
                message.isSent
                  ? 'bg-[#FF8C00] text-white rounded-tr-sm'
                  : 'bg-white text-[#333333] rounded-tl-sm'
              ]"
            >
              {{ message.content }}
            </div>
            <span class="text-[11px] text-[#9CA3AF] mt-1.5">{{ message.time }}</span>
          </div>
        </div>

        <div v-if="sendErrorMessage" class="px-6 pt-3 text-sm text-[#DC2626]">
          {{ sendErrorMessage }}
        </div>

        <!-- 输入区域 -->
        <div class="px-6 py-4 border-t border-[#E5E7EB] flex items-center gap-4 bg-white relative">
          <div class="relative">
            <i
              class="fa-regular fa-face-smile text-xl text-[#9CA3AF] cursor-pointer hover:text-[#333333]"
              @click="toggleEmojiPanel"
            ></i>

            <!-- 表情面板 -->
            <div
              v-if="showEmojiPanel"
              class="absolute bottom-10 left-0 bg-white rounded-xl shadow-lg border border-[#E5E7EB] p-3 grid grid-cols-8 gap-1.5 z-[2000] w-64"
            >
              <button
                v-for="emoji in emojiList"
                :key="emoji"
                type="button"
                class="w-7 h-7 flex items-center justify-center text-xl hover:bg-[#F3F4F6] rounded-lg"
                @click.stop="appendEmoji(emoji)"
              >
                {{ emoji }}
              </button>
            </div>
          </div>

          <i
            class="fa-solid fa-paperclip text-xl text-[#9CA3AF] cursor-pointer hover:text-[#333333]"
            @click="handleAttachmentClick"
          ></i>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleFileChange"
          />
          <input
            v-model="messageInput"
            type="text"
            placeholder="输入消息..."
            class="flex-1 border-none bg-transparent text-sm outline-none py-1.5"
            @keypress="handleKeyPress"
          />
          <button
            class="w-9 h-9 bg-[#FF8C00] text-white rounded-full flex items-center justify-center cursor-pointer border-none transition-all hover:bg-[#D97706] hover:scale-105"
            @click="sendMessage"
          >
            <i class="fa-solid fa-paper-plane"></i>
          </button>
        </div>
      </section>
    </main>

    <!-- 图片预览大图 -->
    <div
      v-if="showImagePreview && previewImageUrl"
      class="fixed inset-0 bg-black/70 z-[4000] flex items-center justify-center"
      @click="closeImagePreview"
    >
      <div class="max-w-[90vw] max-h-[90vh]" @click.stop>
        <img
          :src="previewImageUrl"
          alt="预览图片"
          class="max-w-full max-h-[90vh] rounded-xl shadow-2xl object-contain"
        />
      </div>
    </div>

    <!-- 页脚 -->
    <!-- <footer class="bg-[#1E293B] text-[#94A3B8] py-5 text-xs text-center mt-auto">
      <div>2025 FUREVERHOME流浪动物领养平台 - 让每个生命都有温暖的家</div>
    </footer> -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getConversations,
  getMessages,
  sendMessage as sendChatMessage,
  markConversationRead,
  uploadImage,
  type ConversationDto,
  type MessageDto,
  type SendMessageRequest,
} from '@/api'
import { formatDateTime } from '@/utils/format'

interface Contact {
  id: string
  conversationId?: number | null
  targetUserId?: number | null
  name: string
  avatar: string
  avatarColor: string
  avatarUrl?: string
  lastMessage: string
  time: string
  unread?: number
  isPending?: boolean
}

interface Message {
  id: number
  content: string
  time: string
  isSent: boolean
  conversationId?: number | null
  senderId?: number | null
  receiverId?: number | null
  messageType?: string
}

const route = useRoute()
const router = useRouter()

const goToUserProfile = (userId: number | null | undefined) => {
  if (userId) {
    router.push({ name: 'UserProfile', params: { userId } })
  }
}

const rawConversations = ref<ConversationDto[]>([])
const contacts = ref<Contact[]>([])
const activeConversationId = ref<number | null>(null)
const currentUserId = ref<number | null>(null)

const pendingTargetUserId = ref<number | null>(null)
const pendingTargetUserName = ref('')
const pendingTargetUserAvatar = ref('')

const searchQuery = ref('')
const messageInput = ref('')
const messages = ref<Message[]>([])
const wsInstance = ref<WebSocket | null>(null)
const wsReconnectTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const reconnectAttempts = ref(0)
const markReadInFlight = new Set<number>()
const messageListRef = ref<HTMLElement | null>(null)
const sendErrorMessage = ref('')
const sendErrorTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const showEmojiPanel = ref(false)
const emojiList = ref<string[]>([
  '😀', '😁', '😂', '🤣', '😊', '😍', '😘', '😜',
  '🤔', '😅', '😆', '😉', '😎', '😭', '😡', '👍',
  '👎', '👏', '🙏', '👌', '✌️', '🤝', '❤️', '💔',
  '⭐', '🌸', '🎉', '🎁', '🍰', '☕', '🍺', '🐶',
])
const fileInputRef = ref<HTMLInputElement | null>(null)
const showImagePreview = ref(false)
const previewImageUrl = ref<string | null>(null)

const unwrapDataContainer = (source: any) => {
  let current = source
  for (let depth = 0; depth < 3; depth += 1) {
    if (
      current &&
      typeof current === 'object' &&
      'data' in current &&
      current.data != null
    ) {
      current = current.data
    } else {
      break
    }
  }
  return current
}

const extractRecords = <T = any>(raw: any): T[] => {
  const payload = unwrapDataContainer(raw)
  if (Array.isArray(payload)) {
    return payload as T[]
  }
  if (!payload || typeof payload !== 'object') {
    return []
  }
  const candidateKeys = ['records', 'list', 'items', 'rows', 'content', 'data']
  for (const key of candidateKeys) {
    const value = (payload as any)[key]
    if (Array.isArray(value)) {
      return value as T[]
    }
  }
  // 有些接口直接把数组挂在某个未知字段上，这里做一次兜底查找
  for (const value of Object.values(payload)) {
    if (Array.isArray(value)) {
      return value as T[]
    }
  }
  return []
}

const pickNumber = (...values: Array<unknown>): number | null => {
  for (const value of values) {
    if (value === undefined || value === null || value === '') continue
    if (typeof value === 'number' && !Number.isNaN(value)) {
      return value
    }
    const parsed = Number(value)
    if (!Number.isNaN(parsed)) {
      return parsed
    }
  }
  return null
}

const pickString = (...values: Array<unknown>): string => {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) {
      return value
    }
  }
  return ''
}

// 规范化头像地址，支持 http/https、/api/ 开头或裸路径
const normalizeAvatarUrl = (url?: string | null): string | undefined => {
  if (!url) return undefined
  const trimmed = url.trim()
  if (!trimmed) return undefined
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) return trimmed
  if (trimmed.startsWith('/api/')) return trimmed
  return `/api/storage/image/${trimmed.replace(/^\/+/, '')}`
}

const scrollMessagesToBottom = (options?: { smooth?: boolean }) => {
  const container = messageListRef.value
  if (!container) return
  requestAnimationFrame(() => {
    container.scrollTo({
      top: container.scrollHeight,
      behavior: options?.smooth ? 'smooth' : 'auto',
    })
  })
}

const showSendError = (message: string) => {
  sendErrorMessage.value = message
  if (sendErrorTimer.value) {
    clearTimeout(sendErrorTimer.value)
  }
  sendErrorTimer.value = setTimeout(() => {
    sendErrorMessage.value = ''
    sendErrorTimer.value = null
  }, 4000)
}

const isImageBubble = (message: Message): boolean => {
  if (!message || !message.content) return false
  if (message.messageType && message.messageType.toUpperCase() === 'IMAGE') {
    return true
  }
  const content = message.content
  if (typeof content !== 'string') return false
  const lower = content.toLowerCase()
  if (lower.startsWith('/api/storage/image/')) return true
  if (lower.startsWith('http') && /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(lower)) {
    return true
  }
  return false
}

const isImageText = (text: string | undefined | null): boolean => {
  if (!text) return false
  const lower = String(text).toLowerCase()
  if (lower.startsWith('/api/storage/image/')) return true
  if (lower.startsWith('http') && /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(lower)) {
    return true
  }
  return false
}

const openImagePreview = (url: string) => {
  if (!url) return
  previewImageUrl.value = url
  showImagePreview.value = true
}

const closeImagePreview = () => {
  showImagePreview.value = false
  previewImageUrl.value = null
}

const toggleEmojiPanel = () => {
  showEmojiPanel.value = !showEmojiPanel.value
}

const appendEmoji = (emoji: string) => {
  const cursorAtEnd = messageInput.value.length
  messageInput.value =
    messageInput.value.slice(0, cursorAtEnd) + emoji + messageInput.value.slice(cursorAtEnd)
  showEmojiPanel.value = false
}

const handleAttachmentClick = () => {
  showEmojiPanel.value = false
  const input = fileInputRef.value
  if (input) {
    input.value = ''
    input.click()
  }
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showSendError('只能发送图片类型的附件')
    return
  }

  const receiverId = resolveReceiverId()
  if (!receiverId) {
    showSendError('请选择要发送的对象后再上传图片')
    return
  }

  try {
    const uploadRes = await uploadImage(file)
    const rawUrl = (uploadRes as any)?.data
    const uploadCode = (uploadRes as any)?.code ?? 200
    if (uploadCode !== 0 && uploadCode !== 200) {
      showSendError((uploadRes as any)?.message || '图片上传失败，请稍后重试')
      return
    }
    const imageUrl =
      (rawUrl && typeof rawUrl === 'object' && (rawUrl.url || rawUrl.path || rawUrl.imageUrl || rawUrl.fileUrl)) ||
      (typeof rawUrl === 'string' ? rawUrl : '')

    const finalUrl = imageUrl
      ? imageUrl.startsWith('http')
        ? imageUrl
        : imageUrl.startsWith('/api')
          ? imageUrl
          : `/api/storage/image/${imageUrl.replace(/^\/+/, '')}`
      : ''

    if (!finalUrl) {
      showSendError('图片上传失败，请稍后重试')
      return
    }

    // 构造一个 only-image 消息发送
    const optimisticId = Date.now()
    const optimistic: Message = {
      id: optimisticId,
      content: finalUrl,
      time: formatDateTime(new Date()),
      isSent: true,
      conversationId: activeConversationId.value,
      senderId: currentUserId.value,
      receiverId,
      messageType: 'IMAGE',
    }
    appendMessageToView(optimistic)
    scrollMessagesToBottom({ smooth: true })

    const payload: SendMessageRequest = {
      receiverId,
      content: finalUrl,
      messageType: 'IMAGE',
    }
    if (activeConversationId.value) {
      payload.conversationId = activeConversationId.value
    }

    const res = await sendChatMessage(payload)
    const responsePayload = unwrapDataContainer(res)
    const responseCode = (res as any)?.code ?? 200
    if (responseCode !== 0 && responseCode !== 200) {
      throw new Error((res as any)?.message || '发送图片失败')
    }

    const nextConversationId =
      pickNumber(
        (responsePayload as any)?.conversationId,
        (responsePayload as any)?.sessionId,
        payload.conversationId
      ) ?? null

    if (responsePayload) {
      const realMessage = mapDtoToMessage(
        { ...responsePayload, messageType: (responsePayload as any)?.messageType ?? 'IMAGE' },
        nextConversationId
      )
      replaceOptimisticMessage(optimisticId, realMessage)
      if (!activeConversationId.value && realMessage.conversationId) {
        activeConversationId.value = realMessage.conversationId
        pendingTargetUserId.value = null
        pendingTargetUserName.value = ''
        pendingTargetUserAvatar.value = ''
      }
    }

    await loadConversations()

    if (nextConversationId) {
      if (activeConversationId.value === nextConversationId) {
        await loadMessages(nextConversationId, { preserve: true })
      } else {
        activeConversationId.value = nextConversationId
      }
      if (pendingTargetUserId.value && receiverId === pendingTargetUserId.value) {
        pendingTargetUserId.value = null
        pendingTargetUserName.value = ''
        pendingTargetUserAvatar.value = ''
      }
    }
  } catch (e) {
    console.error('发送图片失败', e)
    const errMsg =
      (e as any)?.message ||
      (e as any)?.response?.data?.message ||
      '发送图片失败，请稍后再试'
    showSendError(errMsg)
    // 回滚乐观消息
    messages.value = messages.value.filter(item => item.content !== (event.target as any)?.result)
  }
}

const setConversationUnreadState = (conversationId: number, unreadCount?: number | null) => {
  if (!conversationId) return
  rawConversations.value = rawConversations.value.map(item =>
    item.conversationId === conversationId
      ? { ...item, unreadCount: unreadCount ?? 0 }
      : item
  )
  contacts.value = contacts.value.map(contact =>
    contact.conversationId === conversationId
      ? {
          ...contact,
          unread: unreadCount && unreadCount > 0 ? unreadCount : undefined,
        }
      : contact
  )
}

const markConversationAsRead = async (conversationId: number) => {
  if (!conversationId || markReadInFlight.has(conversationId)) return
  setConversationUnreadState(conversationId, 0)
  markReadInFlight.add(conversationId)
  try {
    await markConversationRead(conversationId)
  } catch (error) {
    console.warn('标记会话已读失败', error)
  } finally {
    markReadInFlight.delete(conversationId)
  }
}

const getAvatarColor = (name: string): string => {
  const colors = ['bg-[#FBBF24]', 'bg-[#34D399]', 'bg-[#60A5FA]', 'bg-[#A78BFA]', 'bg-[#F87171]']
  if (!name) return colors[0] ?? 'bg-[#FBBF24]'
  const code = name.charCodeAt(0)
  const color = colors[code % colors.length] ?? colors[0] ?? 'bg-[#FBBF24]'
  return color
}

const normalizeTokenValue = (value?: string | null) => {
  if (!value) return ''
  return value.startsWith('Bearer ') ? value.slice(7) : value
}

const getStoredToken = () => {
  const raw =
    localStorage.getItem('saTokenValue') ||
    localStorage.getItem('bearerToken') ||
    localStorage.getItem('token') ||
    ''
  return normalizeTokenValue(raw)
}

const mapConversationsToContacts = (list: ConversationDto[]): Contact[] => {
  return list.map(item => {
    const conversationId =
      pickNumber(
        (item as any).conversationId,
        (item as any).id,
        (item as any).conversationID,
        (item as any).conversation_id
      ) ?? undefined
    const targetUserId =
      pickNumber(
        (item as any).targetUserId,
        (item as any).userId,
        (item as any).receiverId,
        (item as any).friendUserId,
        (item as any).targetId,
        (item as any).toUserId
      ) ?? undefined
    const name =
      pickString(
        (item as any).targetUserName,
        (item as any).userName,
        (item as any).nickname,
        (item as any).contactName
      ) || '用户'
    const avatarUrl = normalizeAvatarUrl(
      pickString(
        (item as any).targetUserAvatar,
        (item as any).avatarUrl,
        (item as any).avatar,
        (item as any).targetAvatar
      )
    )
    const lastMessage = pickString(
      (item as any).lastMessage,
      (item as any).latestMessage,
      (item as any).lastMsg,
      (item as any).content
    )
    const lastMessageTime =
      (item as any).lastMessageTime ||
      (item as any).latestMessageTime ||
      (item as any).lastTime ||
      (item as any).updateTime ||
      (item as any).updatedAt
    const unreadCount =
      pickNumber((item as any).unreadCount, (item as any).unread, (item as any).unreadTotal) ?? 0

    const lastMessageDisplay = isImageText(lastMessage) ? '[图片]' : lastMessage

    return {
      id: conversationId ? `conv-${conversationId}` : `user-${targetUserId ?? name}`,
      conversationId: conversationId ?? null,
      targetUserId: targetUserId ?? null,
      name,
      avatar: name.charAt(0),
      avatarColor: getAvatarColor(name) ?? '',
      avatarUrl,
      lastMessage: lastMessageDisplay,
      time: formatDateTime(lastMessageTime),
      unread: unreadCount > 0 ? unreadCount : undefined,
    }
  })
}

const trySelectConversationByUser = (userId: number | null | undefined) => {
  if (!userId) return false
  const matched = rawConversations.value.find(item => item.targetUserId === userId)
  if (matched?.conversationId) {
    activeConversationId.value = matched.conversationId
    pendingTargetUserId.value = null
    pendingTargetUserName.value = ''
    pendingTargetUserAvatar.value = ''
    return true
  }
  return false
}

const loadConversations = async () => {
  try {
    const res = await getConversations({ page: 1, pageSize: 50 })
    const list = extractRecords<ConversationDto>(res)
    rawConversations.value = list
    contacts.value = mapConversationsToContacts(list)

    if (pendingTargetUserId.value) {
      const matched = trySelectConversationByUser(pendingTargetUserId.value)
      // 如果还没匹配到真实会话，保持当前的临时消息展示，不再清空
      if (!matched) {
        activeConversationId.value = null
      }
    } else if (!activeConversationId.value && contacts.value.length > 0) {
      const first = contacts.value.find(contact => contact.conversationId)
      if (first?.conversationId) {
        activeConversationId.value = first.conversationId
      }
    }
  } catch (e) {
    console.error('加载会话列表失败', e)
  }
}

const loadMessages = async (conversationId: number, options?: { preserve?: boolean }) => {
  const previousMessages = options?.preserve ? [...messages.value] : []
  try {
    const res = await getMessages(conversationId, { page: 1, pageSize: 50 })
    const list: MessageDto[] = extractRecords<MessageDto>(res)
    const mapped = list
      .slice()
      .sort((a, b) => {
        const t1 = a.createdAt ? new Date(a.createdAt).getTime() : 0
        const t2 = b.createdAt ? new Date(b.createdAt).getTime() : 0
        return t1 - t2
      })
      .map(m => ({
        id: m.messageId || 0,
        content: m.content || '',
        time: formatDateTime(m.createdAt),
        isSent: currentUserId.value != null && m.senderId === currentUserId.value,
        conversationId: m.conversationId ?? conversationId,
        senderId: m.senderId ?? null,
        receiverId: m.receiverId ?? null,
      }))
    if (mapped.length > 0) {
      messages.value = mapped
    } else if (options?.preserve) {
      messages.value = previousMessages
    } else {
      messages.value = []
    }
  } catch (e) {
    console.error('加载消息列表失败', e)
    if (options?.preserve && previousMessages.length) {
      messages.value = previousMessages
    }
  }
}

const mapDtoToMessage = (
  dto: Partial<MessageDto> & Record<string, any>,
  fallbackConversationId?: number | null
): Message => {
  const messageId =
    pickNumber(dto.messageId, dto.id, dto.messageID, dto.msgId, dto.tempId) ?? Date.now()
  const createdAt =
    dto.createdAt || dto.sendTime || dto.createTime || dto.timestamp || dto.time || new Date().toISOString()
  const senderId = pickNumber(dto.senderId, dto.fromUserId, dto.sourceUserId, dto.userId)
  const receiverId = pickNumber(dto.receiverId, dto.toUserId, dto.targetUserId, dto.peerUserId)
  const conversationId =
    pickNumber(dto.conversationId, dto.sessionId, dto.chatId) ?? fallbackConversationId ?? null
  const content = pickString(dto.content, dto.text, dto.message, dto.body)
  const messageType = pickString(dto.messageType, dto.type, dto.msgType)

  return {
    id: messageId,
    content,
    time: formatDateTime(createdAt),
    isSent: currentUserId.value != null && senderId === currentUserId.value,
    conversationId,
    senderId: senderId ?? null,
    receiverId: receiverId ?? null,
    messageType: messageType || undefined,
  }
}

const appendMessageToView = (message: Message) => {
  if (!message.content) return
  const duplicated = messages.value.some(item => item.id && message.id && item.id === message.id)
  if (duplicated) return
  messages.value = [...messages.value, message]
}

const replaceOptimisticMessage = (optimisticId: number, realMessage: Message) => {
  const index = messages.value.findIndex(item => item.id === optimisticId)
  if (index !== -1) {
    const updated = [...messages.value]
    updated[index] = realMessage
    messages.value = updated
  } else {
    appendMessageToView(realMessage)
  }
}

const displayContacts = computed(() => {
  const list = contacts.value.slice()
  if (
    pendingTargetUserId.value &&
    !list.some(contact => contact.targetUserId === pendingTargetUserId.value)
  ) {
    const name = pendingTargetUserName.value || '用户'
    list.unshift({
      id: `pending-${pendingTargetUserId.value}`,
      conversationId: null,
      targetUserId: pendingTargetUserId.value,
      name,
      avatar: name.charAt(0),
      avatarColor: getAvatarColor(name) ?? '',
      avatarUrl: pendingTargetUserAvatar.value || undefined,
      lastMessage: '开始新的对话',
      time: '',
      unread: undefined,
      isPending: true,
    })
  }
  return list
})

const filteredContacts = computed(() => {
  if (!searchQuery.value.trim()) {
    return displayContacts.value
  }
  const keyword = searchQuery.value.toLowerCase()
  return displayContacts.value.filter(contact =>
    contact.name.toLowerCase().includes(keyword)
  )
})

const activeContact = computed<Contact | undefined>(() => {
  if (activeConversationId.value) {
    return contacts.value.find(c => c.conversationId === activeConversationId.value)
  }
  if (pendingTargetUserId.value) {
    const name = pendingTargetUserName.value || '用户'
    return {
      id: `pending-${pendingTargetUserId.value}`,
      conversationId: null,
      targetUserId: pendingTargetUserId.value,
      name,
      avatar: name.charAt(0),
      avatarColor: getAvatarColor(name) ?? '',
      avatarUrl: pendingTargetUserAvatar.value || undefined,
      lastMessage: '',
      time: '',
      unread: undefined,
      isPending: true,
    }
  }
  return undefined
})

const selectContact = (contact: Contact) => {
  if (contact.conversationId) {
    setConversationUnreadState(contact.conversationId, 0)
    if (activeConversationId.value !== contact.conversationId) {
      activeConversationId.value = contact.conversationId
    }
    pendingTargetUserId.value = null
    pendingTargetUserName.value = ''
    pendingTargetUserAvatar.value = ''
  } else if (contact.targetUserId) {
    pendingTargetUserId.value = contact.targetUserId
    pendingTargetUserName.value = contact.name
    pendingTargetUserAvatar.value = contact.avatarUrl || ''
    if (activeConversationId.value !== null) {
      activeConversationId.value = null
    }
    messages.value = []
  }
}

const resolveReceiverId = (): number | null => {
  if (pendingTargetUserId.value) return pendingTargetUserId.value
  if (activeConversationId.value) {
    const contact = contacts.value.find(item => item.conversationId === activeConversationId.value)
    if (contact?.targetUserId) return contact.targetUserId
  }
  return null
}

const sendMessage = async () => {
  if (!messageInput.value.trim()) return

  showEmojiPanel.value = false

  const receiverId = resolveReceiverId()
  if (!receiverId) return

  const content = messageInput.value.trim()
  const optimistic: Message = {
    id: Date.now(),
    content,
    time: formatDateTime(new Date()),
    isSent: true,
    conversationId: activeConversationId.value,
    senderId: currentUserId.value,
    receiverId,
  }
  appendMessageToView(optimistic)
  messageInput.value = ''

  try {
    const payload: SendMessageRequest = {
      receiverId,
      content,
    }
    if (activeConversationId.value) {
      payload.conversationId = activeConversationId.value
    }

    const res = await sendChatMessage(payload)
    const responsePayload = unwrapDataContainer(res)
    const responseCode = (res as any)?.code ?? 200
    if (responseCode !== 0 && responseCode !== 200) {
      throw new Error((res as any)?.message || '发送消息失败')
    }

    const nextConversationId =
      pickNumber(
        (responsePayload as any)?.conversationId,
        (responsePayload as any)?.sessionId,
        payload.conversationId
      ) ?? null
    if (responsePayload) {
      const realMessage = mapDtoToMessage(responsePayload, nextConversationId)
      replaceOptimisticMessage(optimistic.id, realMessage)
      if (!activeConversationId.value && realMessage.conversationId) {
        activeConversationId.value = realMessage.conversationId
        pendingTargetUserId.value = null
        pendingTargetUserName.value = ''
        pendingTargetUserAvatar.value = ''
      }
    }

    await loadConversations()

    if (nextConversationId) {
      if (activeConversationId.value === nextConversationId) {
        await loadMessages(nextConversationId)
      } else {
        activeConversationId.value = nextConversationId
      }
      // 确认已经有真实会话后，再清理“待联系用户”的临时状态
      if (pendingTargetUserId.value && receiverId === pendingTargetUserId.value) {
        pendingTargetUserId.value = null
        pendingTargetUserName.value = ''
        pendingTargetUserAvatar.value = ''
      }
    }
  } catch (e) {
    console.error('发送消息失败', e)
    messages.value = messages.value.filter(item => item.id !== optimistic.id)
    const errorMessage =
      (e as Error)?.message?.includes('超时') || (e as Error)?.message?.includes('timeout')
        ? '发送超时，请检查网络后重试'
        : (e as Error)?.message || '发送失败，请稍后再试'
    showSendError(errorMessage)
  }
}

const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    showEmojiPanel.value = false
    sendMessage()
  }
}

const handleIncomingMessageEvent = (raw: any) => {
  if (!raw) return
  const dto = raw.message || raw.data || raw
  const formatted = mapDtoToMessage(dto, dto.conversationId ?? raw.conversationId ?? null)
  if (!formatted.content) return

  const relatesToPending =
    !activeConversationId.value &&
    pendingTargetUserId.value != null &&
    (pendingTargetUserId.value === formatted.senderId || pendingTargetUserId.value === formatted.receiverId)

  if (
    (formatted.conversationId && formatted.conversationId === activeConversationId.value) ||
    relatesToPending
  ) {
    if (!activeConversationId.value && formatted.conversationId) {
      activeConversationId.value = formatted.conversationId
      pendingTargetUserId.value = null
      pendingTargetUserName.value = ''
      pendingTargetUserAvatar.value = ''
    }
    appendMessageToView(formatted)
    if (formatted.conversationId) {
      markConversationAsRead(formatted.conversationId)
    }
  }

  loadConversations()
}

const handleWebsocketMessage = (raw: any) => {
  if (!raw) return
  if (raw === 'ping') {
    wsInstance.value?.send('pong')
    return
  }
  let payload: any = raw
  if (typeof raw === 'string') {
    try {
      payload = JSON.parse(raw)
    } catch (error) {
      console.warn('无法解析聊天推送消息', raw)
      return
    }
  }
  const eventType = payload.eventType || payload.type || payload.topic || payload.action
  if (eventType) {
    switch (eventType) {
      case 'CHAT_MESSAGE':
      case 'chat_message':
      case 'MESSAGE':
      case 'message':
        handleIncomingMessageEvent(payload.data || payload.message || payload)
        break
      case 'CONVERSATION_UPDATED':
      case 'conversation_updated':
      case 'CONVERSATION':
        loadConversations()
        break
      default:
        if (payload.data && (payload.data.messageId || payload.data.conversationId)) {
          handleIncomingMessageEvent(payload.data)
        }
    }
  } else if (payload.messageId || payload.conversationId || payload.message) {
    handleIncomingMessageEvent(payload)
  }
}

const clearReconnectTimer = () => {
  if (wsReconnectTimer.value) {
    clearTimeout(wsReconnectTimer.value)
    wsReconnectTimer.value = null
  }
}

const scheduleReconnect = () => {
  if (wsReconnectTimer.value) return
  const delay = Math.min(10000, 2000 * Math.max(1, reconnectAttempts.value + 1))
  wsReconnectTimer.value = setTimeout(() => {
    wsReconnectTimer.value = null
    reconnectAttempts.value += 1
    initChatWebsocket()
  }, delay)
}

const cleanupWebsocket = () => {
  clearReconnectTimer()
  if (wsInstance.value) {
    wsInstance.value.onopen = null
    wsInstance.value.onmessage = null
    wsInstance.value.onerror = null
    wsInstance.value.onclose = null
    try {
      wsInstance.value.close()
    } catch (error) {
      // ignore
    }
    wsInstance.value = null
  }
}

const extractWsUrlCandidate = (payload: any): string => {
  if (!payload) return ''
  if (typeof payload === 'string') {
    return payload
  }
  if (typeof payload !== 'object') return ''
  const candidateKeys = [
    'wsUrl',
    'url',
    'endpoint',
    'wsEndpoint',
    'fullUrl',
    'urlTemplate',
    'url_template',
    'template',
    'path',
    'wsPath',
    'connectUrl',
    'socketUrl',
    'ws',
    'uri',
  ]
  for (const key of candidateKeys) {
    const value = (payload as Record<string, unknown>)[key]
    if (typeof value === 'string' && value.trim()) {
      return value.trim()
    }
  }
  if (payload.receiveExamples) {
    const nested = extractWsUrlCandidate(payload.receiveExamples)
    if (nested) return nested
  }
  for (const value of Object.values(payload)) {
    if (typeof value === 'string' && value.trim()) {
      if (value.includes('/ws') || value.includes('ws://') || value.includes('wss://')) {
        return value.trim()
      }
    }
    if (typeof value === 'object') {
      const nested = extractWsUrlCandidate(value)
      if (nested) return nested
    }
  }
  return ''
}

const resolveWsUrl = (rawUrl: string, token?: string) => {
  if (!rawUrl) return ''
  let url = rawUrl
  if (token && url.includes('{userToken}')) {
    url = url.replace('{userToken}', encodeURIComponent(token))
  }
  if (url.startsWith('//')) {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    url = `${protocol}${url}`
  } else if (url.startsWith('/')) {
    const origin =
      window.location.origin.startsWith('http')
        ? window.location.origin.replace(/^http/, 'ws')
        : `ws://${window.location.host}`
    url = `${origin}${url}`
  } else if (!url.startsWith('ws')) {
    url = url.replace(/^http/, 'ws')
  }
  return url
}

const getFallbackWsUrl = () => {
  // 优先使用环境变量；未配置时直连测试环境的 ws 地址，避免本地代理异常
  const envUrl =
    (import.meta as any).env?.VITE_CHAT_WS_URL ||
    (import.meta as any).env?.VITE_WS_URL ||
    (import.meta as any).env?.VITE_WS_BASE_URL ||
    'ws://xd6325a9.natappfree.cc/ws/common'
  return resolveWsUrl(envUrl)
}

const initChatWebsocket = async () => {
  try {
    cleanupWebsocket()

    // 新接口不再提供 /ws/chat/info；直接使用固定路径 /api/ws/common，支持环境覆盖
    const token = getStoredToken()
    let wsUrl = getFallbackWsUrl()

    // 兼容旧返回 /ws/chat 的情况
    if (typeof wsUrl === 'string' && wsUrl.includes('/ws/chat')) {
      wsUrl = wsUrl.replace('/ws/chat', '/ws/common')
    }

    let connector = resolveWsUrl(wsUrl, token)
    if (!connector) {
      console.warn('聊天 WebSocket 地址解析失败', wsUrl)
      return
    }

    if (token && !connector.includes('token=')) {
      connector = `${connector}${connector.includes('?') ? '&' : '?'}token=${encodeURIComponent(token)}`
    }

    const socket = new WebSocket(connector)
    wsInstance.value = socket
    socket.onopen = () => {
      reconnectAttempts.value = 0
    }
    socket.onmessage = event => handleWebsocketMessage(event.data)
    socket.onerror = () => {
      scheduleReconnect()
    }
    socket.onclose = () => {
      scheduleReconnect()
    }
  } catch (error) {
    console.error('初始化聊天 WebSocket 失败', error)
    scheduleReconnect()
  }
}

watch(
  () => [route.query.userId, route.query.userName, route.query.userAvatar],
  ([userIdParam, userNameParam, userAvatarParam]) => {
    const parsed = Number(userIdParam)
    if (!Number.isNaN(parsed) && parsed > 0) {
      pendingTargetUserId.value = parsed
      pendingTargetUserName.value = typeof userNameParam === 'string' ? userNameParam : ''
      pendingTargetUserAvatar.value =
        typeof userAvatarParam === 'string' ? normalizeAvatarUrl(userAvatarParam) || '' : ''
      trySelectConversationByUser(parsed)
    }
  },
  { immediate: true }
)

watch(activeConversationId, (conversationId, previousConversationId) => {
  if (conversationId) {
    const shouldPreserve = previousConversationId == null && pendingTargetUserId.value != null
    loadMessages(conversationId, { preserve: shouldPreserve }).finally(() => {
      markConversationAsRead(conversationId)
    })
  } else if (!pendingTargetUserId.value) {
    messages.value = []
  }
})

watch(
  messages,
  () => {
    nextTick(() => {
      scrollMessagesToBottom({ smooth: true })
    })
  },
  { deep: true }
)

onMounted(() => {
  try {
    const userStr = localStorage.getItem('currentUser')
    if (userStr) {
      const u = JSON.parse(userStr)
      if (u && typeof u.userId === 'number') {
        currentUserId.value = u.userId
      }
    }
  } catch (e) {
    console.warn('解析 currentUser 失败', e)
  }

  loadConversations().then(() => {
    if (activeConversationId.value) {
      loadMessages(activeConversationId.value)
    }
  })

  initChatWebsocket()
})

onBeforeUnmount(() => {
  cleanupWebsocket()
  if (sendErrorTimer.value) {
    clearTimeout(sendErrorTimer.value)
  }
})
</script>

<style scoped>
</style>