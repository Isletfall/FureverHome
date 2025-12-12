/**
 * 管理端帖子相关 API
 */
import { adminHttpClient, type ApiResponse } from './request'

/**
 * 审核状态
 */
export enum ReviewStatus {
  待审核 = '待审核',
  拒绝 = '拒绝',
  通过 = '通过'
}

/**
 * AdminPostSummaryDTO，管理端帖子列表项
 */
export interface AdminPostSummaryDTO {
  authorId?: number
  authorName?: string
  commentCount?: number
  createTime?: string
  excerpt?: string
  likeCount?: number
  postId?: number
  reviewStatus?: ReviewStatus
  title?: string
  viewCount?: number
  [property: string]: any
}

/**
 * PageResultAdminPostSummaryDTO，通用分页返回结构
 */
export interface PageResultAdminPostSummaryDTO {
  page?: number
  pageSize?: number
  records?: AdminPostSummaryDTO[]
  total?: number
  [property: string]: any
}

/**
 * ResultPageResultAdminPostSummaryDTO
 */
export interface AdminPostListResponse extends ApiResponse<PageResultAdminPostSummaryDTO> {}

export interface AdminPostListParams {
  page?: number
  pageSize?: number
  [property: string]: any
}

/**
 * 获取所有已发布帖子（管理端）
 * GET /admin/posts/published
 */
export function fetchPublishedAdminPosts(params?: AdminPostListParams) {
  return adminHttpClient.get<PageResultAdminPostSummaryDTO>('/admin/posts/published', {
    params
  })
}


