/**
 * 我的中心相关接口（我的宠物、我的帖子）
 */
import httpClient, { type ApiResponse } from './request'

// 通用分页请求
export interface MyPageRequest {
  page?: number
  pageSize?: number
  [property: string]: any
}

/**
 * -------------------------
 * 我的宠物列表（短期 / 长期）
 * -------------------------
 */

// 领养状态
export enum AdoptionStatus {
  短期领养 = '短期领养',
  长期领养 = '长期领养'
}

// 性别
export enum Gender {
  公 = '公',
  未知 = '未知',
  母 = '母'
}

// 是否绝育
export enum IsSterilized {
  否 = '否',
  是 = '是',
  未知 = '未知'
}

// 审核状态
export enum ReviewStatus {
  待审核 = '待审核',
  拒绝 = '拒绝',
  通过 = '通过'
}

// 动物种类
export enum Species {
  仓鼠 = '仓鼠',
  兔子 = '兔子',
  其他 = '其他',
  狗 = '狗',
  猫 = '猫',
  鱼类 = '鱼类',
  鸟类 = '鸟类',
  龟类 = '龟类'
}

/**
 * 动物信息，动物基础信息DTO
 */
export interface 动物信息 {
  adoptionStatus?: AdoptionStatus
  animalAge?: number
  animalId?: number
  animalName?: string
  breed?: string
  contactEmail?: string
  currentLocation?: string
  gender?: Gender
  healthStatus?: string
  isSterilized?: IsSterilized
  ownerEmail?: string
  ownerLocation?: string
  ownerPhone?: string
  photoUrls?: string[]
  reviewStatus?: ReviewStatus
  shortDescription?: string
  species?: Species
  userId?: number
  userName?: string
  [property: string]: any
}

/**
 * PageResult动物信息，通用分页返回结构
 */
export interface PageResult动物信息 {
  page?: number
  pageSize?: number
  records?: 动物信息[]
  total?: number
  [property: string]: any
}

export type 我的宠物列表响应 = ApiResponse<PageResult动物信息>

/**
 * 获取我的短期宠物列表
 * GET /animal/mine/short
 */
export function getMyShortAnimals(params?: MyPageRequest) {
  return httpClient.get<PageResult动物信息>('/animal/mine/short', {
    params
  })
}

/**
 * 获取我的长期宠物列表
 * GET /animal/mine/long
 */
export function getMyLongAnimals(params?: MyPageRequest) {
  return httpClient.get<PageResult动物信息>('/animal/mine/long', {
    params
  })
}

/**
 * -------------------------
 * 我的帖子列表
 * -------------------------
 */

// 帖子审核状态
export enum 帖子审核状态 {
  待审核 = '待审核',
  拒绝 = '拒绝',
  通过 = '通过'
}

/**
 * 帖子信息，帖子基础信息DTO
 */
export interface 帖子信息 {
  commentCount?: number
  content?: string
  createTime?: Date | string
  likeCount?: number
  mediaUrls?: string
  postId?: number
  reviewStatus?: 帖子审核状态
  title?: string
  userId?: number
  viewCount?: number
  [property: string]: any
}

/**
 * PageResult帖子信息，通用分页返回结构
 */
export interface PageResult帖子信息 {
  page?: number
  pageSize?: number
  records?: 帖子信息[]
  total?: number
  [property: string]: any
}

export type 我的帖子列表响应 = ApiResponse<PageResult帖子信息>

/**
 * 获取我的帖子列表
 * GET /post/mine/list
 */
export function getMyPosts(params?: MyPageRequest) {
  return httpClient.get<PageResult帖子信息>('/post/mine/list', {
    params
  })
}


