/**
 * 后台管理API - 统一封装所有后台管理接口
 */
import { adminHttpClient, type ApiResponse } from './request'

// ==================== 通用类型定义 ====================

/**
 * 分页参数
 */
export interface AdminPageParams {
  page?: number
  pageSize?: number
  keyword?: string
  [property: string]: any
}

/**
 * 通用分页结果
 */
export interface PageResult<T> {
  list?: T[]
  records?: T[]
  page?: number
  pageSize?: number
  total?: number
  totalPages?: number
  [property: string]: any
}

// ==================== Dashboard 统计数据 ====================

export interface DashboardStatisticsDto {
  totalPostCount?: number
  longTermPetCount?: number
  shortTermPetCount?: number
  pendingPostCount?: number
  pendingPetCount?: number
  pendingAdoptCount?: number
  [property: string]: any
}

/**
 * 获取管理后台首页统计数据
 * GET /admin/dashboard/statistics
 */
export function getDashboardStatistics(): Promise<ApiResponse<DashboardStatisticsDto>> {
  return adminHttpClient.get<DashboardStatisticsDto>('/admin/dashboard/statistics')
}

// ==================== Posts 帖子管理 ====================

export enum PostReviewStatus {
  待审核 = '待审核',
  通过 = '通过',
  拒绝 = '拒绝'
}

export interface AdminPostSummaryDto {
  postId?: number
  title?: string
  excerpt?: string
  authorId?: number
  authorName?: string
  authorAvatar?: string
  reviewStatus?: PostReviewStatus | string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  createTime?: string | Date
  updateTime?: string | Date
  [property: string]: any
}

export interface AdminPostDetailDto {
  postId?: number
  title?: string
  content?: string
  authorId?: number
  authorName?: string
  authorAvatar?: string
  reviewStatus?: PostReviewStatus | string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  createTime?: string | Date
  images?: string[]
  mediaUrls?: string[]
  comments?: AdminCommentDto[]
  [property: string]: any
}

export interface AdminCommentDto {
  commentId?: number
  parentCommentId?: number
  content?: string
  userId?: number
  userName?: string
  userAvatar?: string
  likeCount?: number
  createTime?: string | Date
  [property: string]: any
}

export interface PostReviewRequest {
  reason?: string
  [property: string]: any
}

/**
 * 获取待审核帖子列表
 * GET /admin/posts/pending
 */
export function getPendingPosts(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminPostSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminPostSummaryDto>>('/admin/posts/pending', { params })
}

/**
 * 获取已发布帖子列表
 * GET /admin/posts/published
 */
export function getPublishedPosts(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminPostSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminPostSummaryDto>>('/admin/posts/published', { params })
}

/**
 * 获取帖子详情
 * GET /admin/posts/{id}
 */
export function getPostDetail(id: number): Promise<ApiResponse<AdminPostDetailDto>> {
  return adminHttpClient.get<AdminPostDetailDto>(`/admin/posts/${id}`)
}

/**
 * 审核通过帖子
 * POST /admin/posts/{id}/approve
 */
export function approvePost(id: number, data?: PostReviewRequest): Promise<ApiResponse<void>> {
  return adminHttpClient.post<void>(`/admin/posts/${id}/approve`, data || {})
}

/**
 * 审核拒绝帖子
 * POST /admin/posts/{id}/reject
 */
export function rejectPost(id: number, data: PostReviewRequest): Promise<ApiResponse<void>> {
  return adminHttpClient.post<void>(`/admin/posts/${id}/reject`, data)
}

/**
 * 删除帖子
 * DELETE /admin/posts/{id}
 */
export function deletePost(id: number): Promise<ApiResponse<void>> {
  return adminHttpClient.delete<void>(`/admin/posts/${id}`)
}

/**
 * 分页获取帖子评论列表
 * GET /admin/posts/{id}/comments
 */
export function getPostComments(id: number, params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageResult<AdminCommentDto>>> {
  return adminHttpClient.get<PageResult<AdminCommentDto>>(`/admin/posts/${id}/comments`, { params })
}

// ==================== Animals 宠物管理 ====================

export enum AnimalSpecies {
  狗 = '狗',
  猫 = '猫',
  兔子 = '兔子',
  仓鼠 = '仓鼠',
  鸟类 = '鸟类',
  鱼类 = '鱼类',
  龟类 = '龟类',
  其他 = '其他'
}

export enum AnimalGender {
  公 = '公',
  母 = '母',
  未知 = '未知'
}

export enum AnimalAdoptionStatus {
  短期领养 = '短期领养',
  长期领养 = '长期领养'
}

export enum AnimalReviewStatus {
  待审核 = '待审核',
  通过 = '通过',
  拒绝 = '拒绝'
}

export interface AdminAnimalSummaryDto {
  animalId?: number
  animalName?: string
  species?: AnimalSpecies | string
  breed?: string
  gender?: AnimalGender | string
  animalAge?: number
  adoptionStatus?: AnimalAdoptionStatus | string
  reviewStatus?: AnimalReviewStatus | string
  ownerId?: number
  ownerName?: string
  ownerAvatar?: string
  createdAt?: string | Date
  [property: string]: any
}

export interface AdminAnimalDetailDto {
  animalId?: number
  animalName?: string
  species?: AnimalSpecies | string
  breed?: string
  gender?: AnimalGender | string
  animalAge?: number
  adoptionStatus?: AnimalAdoptionStatus | string
  reviewStatus?: AnimalReviewStatus | string
  ownerId?: number
  ownerName?: string
  ownerAvatar?: string
  createdAt?: string | Date
  description?: string
  shortDescription?: string
  images?: string[]
  photoUrls?: string[]
  healthStatus?: string
  sterilizedDisplay?: string
  [property: string]: any
}

export interface AnimalReviewRequest {
  reason?: string
  animalId: number
  [property: string]: any
}

/**
 * 获取待审核的宠物列表
 * GET /admin/animals/pending
 */
export function getPendingAnimals(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminAnimalSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminAnimalSummaryDto>>('/admin/animals/pending', { params })
}

/**
 * 获取待审核短期宠物列表
 * GET /admin/animals/pending-short
 */
export function getPendingShortAnimals(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminAnimalSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminAnimalSummaryDto>>('/admin/animals/pending-short', { params })
}

/**
 * 获取待审核长期宠物列表
 * GET /admin/animals/pending-long
 */
export function getPendingLongAnimals(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminAnimalSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminAnimalSummaryDto>>('/admin/animals/pending-long', { params })
}

/**
 * 获取已发布短期宠物列表
 * GET /admin/animals/short
 */
export function getShortAnimals(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminAnimalSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminAnimalSummaryDto>>('/admin/animals/short', { params })
}

/**
 * 获取已发布长期宠物列表
 * GET /admin/animals/long
 */
export function getLongAnimals(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminAnimalSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminAnimalSummaryDto>>('/admin/animals/long', { params })
}

/**
 * 获取宠物详情
 * GET /admin/animals/{id}
 */
export function getAnimalDetail(id: number): Promise<ApiResponse<AdminAnimalDetailDto>> {
  return adminHttpClient.get<AdminAnimalDetailDto>(`/admin/animals/${id}`)
}

/**
 * 审核通过宠物
 * POST /admin/animals/{id}/approve
 */
export function approveAnimal(id: number, data?: AnimalReviewRequest): Promise<ApiResponse<void>> {
  return adminHttpClient.post<void>(`/admin/animals/${id}/approve`, data || { animalId: id })
}

/**
 * 审核拒绝宠物
 * POST /admin/animals/{id}/reject
 */
export function rejectAnimal(id: number, data: AnimalReviewRequest): Promise<ApiResponse<void>> {
  return adminHttpClient.post<void>(`/admin/animals/${id}/reject`, { ...data, animalId: id })
}

/**
 * 删除宠物
 * DELETE /admin/animals/{id}
 */
export function deleteAnimal(id: number): Promise<ApiResponse<void>> {
  return adminHttpClient.delete<void>(`/admin/animals/${id}`)
}

// ==================== Adopts 领养申请管理 ====================

export enum AdoptApplicationStatus {
  申请中 = '申请中',
  申请成功 = '申请成功',
  申请失败 = '申请失败'
}

export enum AdoptReviewStatus {
  待审核 = '待审核',
  通过 = '通过',
  拒绝 = '拒绝'
}

export interface AdminAdoptSummaryDto {
  adoptId?: number
  userId?: number
  userName?: string
  userAvatar?: string
  targetUserId?: number
  targetUserName?: string
  targetUserAvatar?: string
  animalId?: number
  animalName?: string
  applicationStatus?: AdoptApplicationStatus | string
  reviewStatus?: AdoptReviewStatus | string
  createTime?: string | Date
  passTime?: string | Date
  [property: string]: any
}

export interface AdminAdoptDetailDto {
  adoptId?: number
  userId?: number
  userName?: string
  userAvatar?: string
  targetUserId?: number
  targetUserName?: string
  targetUserAvatar?: string
  animalId?: number
  animalName?: string
  applicationStatus?: AdoptApplicationStatus | string
  reviewStatus?: AdoptReviewStatus | string
  createTime?: string | Date
  passTime?: string | Date
  reason?: string
  adoptReason?: string
  address?: string
  contactInfo?: string
  [property: string]: any
}

export interface AdoptReviewRequest {
  reason?: string
  adoptId: number
  [property: string]: any
}

/**
 * 获取待审核领养申请列表
 * GET /admin/adopts/pending
 */
export function getPendingAdopts(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminAdoptSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminAdoptSummaryDto>>('/admin/adopts/pending', { params })
}

/**
 * 获取已处理领养申请列表
 * GET /admin/adopts/processed
 */
export function getProcessedAdopts(params?: AdminPageParams): Promise<ApiResponse<PageResult<AdminAdoptSummaryDto>>> {
  return adminHttpClient.get<PageResult<AdminAdoptSummaryDto>>('/admin/adopts/processed', { params })
}

/**
 * 获取领养申请详情
 * GET /admin/adopts/{id}
 */
export function getAdoptDetail(id: number): Promise<ApiResponse<AdminAdoptDetailDto>> {
  return adminHttpClient.get<AdminAdoptDetailDto>(`/admin/adopts/${id}`)
}

/**
 * 审核通过领养申请
 * POST /admin/adopts/{id}/approve
 */
export function approveAdopt(id: number, data?: AdoptReviewRequest): Promise<ApiResponse<void>> {
  return adminHttpClient.post<void>(`/admin/adopts/${id}/approve`, data || { adoptId: id })
}

/**
 * 审核拒绝领养申请
 * POST /admin/adopts/{id}/reject
 */
export function rejectAdopt(id: number, data: AdoptReviewRequest): Promise<ApiResponse<void>> {
  return adminHttpClient.post<void>(`/admin/adopts/${id}/reject`, { ...data, adoptId: id })
}

