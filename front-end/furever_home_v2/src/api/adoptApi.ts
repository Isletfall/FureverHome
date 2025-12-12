import httpClient, { type ApiResponse } from './request'

/**
 * -------------------------
 * 我的待办列表（别人对我的申请）
 * GET /adopt/todo
 * -------------------------
 */

// AdoptTodoItemDTO，对应后端「我的待办」列表项结构
export interface AdoptTodoItem {
  /** 领养申请ID */
  adoptId?: number
  /** 动物ID */
  animalId?: number
  /** 动物名称 */
  animalName?: string
  /** 动物封面图片URL */
  animalPhoto?: string
  /** 申请人头像URL */
  applicantAvatar?: string
  /** 申请人ID */
  applicantId?: number
  /** 申请人昵称 */
  applicantName?: string
  /** 申请时间 */
  createTime?: string | Date
  /** 申请理由 */
  reason?: string
  /**
   * 是否由当前登录用户主动撤销本次申请
   * - 后端约定：只有在用户点击“撤销申请”后才返回 true，其它场景不返回或为 false
   */
  cancelledByUser?: boolean
  [property: string]: any
}

export type AdoptTodoListResponse = ApiResponse<AdoptTodoItem[]>

export function getMyAdoptTodoList() {
  // /api 前缀由 request.ts 的 BASE_URL 和 Vite 代理处理
  return httpClient.get<AdoptTodoItem[]>('/adopt/todo')
}

// MyAdoptItemDTO，我的领养申请列表项
export interface MyAdoptItemDTO {
  /** 领养申请ID */
  adoptId?: number
  /** 宠物ID */
  animalId?: number
  /** 宠物名称 */
  animalName?: string
  /** 宠物封面照片 URL */
  animalPhoto?: string
  /** 用户审核状态 */
  applicationStatus?: string
  /** 创建时间 */
  createTime?: Date | string
  /** 用户撤销状态 */
  isCanceled?: string
  /** 领养理由 */
  reason?: string
  /** 管理员审核状态 */
  reviewStatus?: string
  /** 被申请用户头像 URL */
  targetUserAvatar?: string
  /** 被申请用户ID（宠物发布者ID） */
  targetUserId?: number
  /** 被申请用户名称（宠物发布者昵称） */
  targetUserName?: string
  [property: string]: any
}

export type AdoptMineListResponse = ApiResponse<MyAdoptItemDTO[]>

// 获取我的领养申请列表
export function getMyAdoptMineList() {
  return httpClient.get<MyAdoptItemDTO[]>('/adopt/mine')
}

/**
 * 撤销我的领养申请
 * DELETE /adopt/mine/{id}
 */
export function cancelMyAdopt(id: number) {
  return httpClient.delete<void>(`/adopt/mine/${id}`)
}

// 提交领养申请请求体
export interface SubmitAdoptRequest {
  adoptReason: string
  animalId: number
  city: string
  email: string
  livingLocation: string
  phone: string
  province: string
  userName: string
  [property: string]: any
}

// 提交领养申请
export function submitAdopt(data: SubmitAdoptRequest) {
  return httpClient.post<void>('/adopt', data)
}

/**
 * -------------------------
 * 领养申请详情（/adopt/{id}）
 * -------------------------
 */

/**
 * 申请状态
 */
export enum ApplicationStatus {
  申请中 = '申请中',
  申请失败 = '申请失败',
  申请成功 = '申请成功',
}

/**
 * 审核状态
 */
export enum ReviewStatus {
  待审核 = '待审核',
  拒绝 = '拒绝',
  通过 = '通过',
}

/**
 * 领养申请信息，领养申请DTO
 * - 对应 GET /adopt/{id} 返回的 data
 */
export interface AdoptDetail {
  /** 申请ID */
  adoptId?: number
  /** 领养理由 */
  adoptReason?: string
  /** 动物ID */
  animalId?: number
  /** 动物照片URL列表 */
  animalPhotoUrls?: string[]
  /** 申请人头像URL */
  applicantAvatar?: string
  /** 申请状态 */
  applicationStatus?: ApplicationStatus
  /** 所在市 */
  city?: string
  /** 申请时间 */
  createTime?: string | Date
  /** 申请人邮箱 */
  email?: string
  /** 居住地址 */
  livingLocation?: string
  /** 审核通过时间 */
  passTime?: string | Date
  /** 申请人电话 */
  phone?: string
  /** 所在省 */
  province?: string
  /** 审核状态 */
  reviewStatus?: ReviewStatus
  /** 动物主人的头像URL */
  targetUserAvatar?: string
  /** 动物主人的用户ID */
  targetUserId?: number
  /** 动物主人的昵称 */
  targetUserName?: string
  /** 申请人的用户ID */
  userId?: number
  /** 申请人昵称 */
  userName?: string
  /**
   * 是否由当前登录用户主动撤销本次申请
   * - 可选：供详情页使用，与列表含义一致
   */
  cancelledByUser?: boolean
  [property: string]: any
}

export type AdoptDetailResponse = ApiResponse<AdoptDetail>

/**
 * 获取领养申请详情
 * GET /adopt/{id}
 * @param id 领养申请ID
 */
export function getAdoptDetail(id: number) {
  return httpClient.get<AdoptDetail>(`/adopt/${id}`)
}

/**
 * -------------------------
 * 宠物主处理领养申请（同意 / 拒绝）
 * -------------------------
 */

export interface HandleAdoptRequest {
  reason?: string
  [property: string]: any
}

/**
 * 审核领养申请（同意 / 拒绝）
 * POST /adopt/{id}/review
 */
export interface ReviewAdoptRequest extends HandleAdoptRequest {
  applicationStatus: ApplicationStatus
}

export function reviewAdopt(id: number, data: ReviewAdoptRequest) {
  return httpClient.post<void>(`/adopt/${id}/review`, data)
}