/**
 * 帖子相关API
 */
import httpClient, { type ApiResponse } from './request'

// 帖子接口（用于论坛列表与详情内部展示结构）
export interface Post {
  id: number
  title: string
  date: string
  summary: string
  content?: string
  status: 'approved' | 'pending' | 'rejected'
  statusLabel: string
  colorClass: string
  reason?: string
  authorId: number
  authorName?: string
  authorAvatar?: string
  images?: string[]
  likes?: number
  comments?: number
  views?: number
  tags?: string[]
  category?: string
}

// 他人帖子列表 DTO（与后端 /post/user/{userId}/list 返回的 帖子公开信息 对齐）
export interface 帖子公开信息 {
  commentCount?: number
  content?: string
  createTime?: string | Date
  likeCount?: number
  mediaUrls?: string
  postId?: number
  title?: string
  userId?: number
  userName?: string
  viewCount?: number
  [property: string]: any
}

// 分页参数接口
export interface PaginationParams {
  page: number
  pageSize: number
}

// 分页响应接口
export interface PaginatedResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// 我的帖子列表 DTO（与后端 /post/mine/list 返回结构对齐）
export interface MyPostItemDTO {
  commentCount?: number
  content?: string
  createTime?: string
  likeCount?: number
  mediaUrls?: string
  postId?: number
  reviewStatus?: string
  title?: string
  userId?: number
  viewCount?: number
  [property: string]: any
}

export interface MyPostListParams {
  page?: number
  pageSize?: number
  [property: string]: any
}

export interface MyPostListPageResult {
  page?: number
  pageSize?: number
  records?: MyPostItemDTO[]
  total?: number
  [property: string]: any
}

export type MyPostListResponse = ApiResponse<MyPostListPageResult>

// 他人帖子列表分页结果（PageResult帖子公开信息）
export interface PageResult帖子公开信息 {
  page?: number
  pageSize?: number
  records?: 帖子公开信息[]
  total?: number
  [property: string]: any
}

export type UserPostListResponse = ApiResponse<PageResult帖子公开信息>

/**
 * 帖子详情 DTO（对应接口：GET /api/post/{id} 返回的 data）
 * 与你文档中的「帖子信息」结构保持一致
 */
export interface 帖子详情DTO {
  commentCount?: number
  content?: string
  createTime?: string | Date
  likeCount?: number
  mediaUrls?: string[]
  postId?: number
  reviewStatus?: string
  title?: string
  userAvatar?: string
  userId?: number
  viewCount?: number
  [property: string]: any
}

/**
 * 获取当前登录用户发布的帖子列表（我的帖子）
 * @param params 分页参数
 */
export function getMyPostList(params: MyPostListParams): Promise<MyPostListResponse> {
  return httpClient.get<MyPostListPageResult>('/post/mine/list', { params })
}

/**
 * 获取帖子列表（论坛首页）
 * @param params 分页参数
 */
export function getPostList(params?: Partial<PaginationParams>): Promise<ApiResponse<Post[]>> {
  return httpClient.get<Post[]>('/post/list', { params })
}

/**
 * 搜索帖子
 * @param params 搜索参数
 */
export function searchPosts(params: {
  keyword: string
  page?: number
  pageSize?: number
}): Promise<ApiResponse<PaginatedResponse<Post>>> {
  return httpClient.get<PaginatedResponse<Post>>('/post/search', { params })
}

/**
 * 获取用户发布的帖子列表（他人帖子）
 * 对应后端接口：GET /api/post/user/{userId}/list
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserPosts(
  userId: number,
  params: { page?: number; pageSize?: number; [property: string]: any }
): Promise<UserPostListResponse> {
  return httpClient.get<PageResult帖子公开信息>(`/post/user/${userId}/list`, { params })
}

/**
 * 获取帖子详情
 * @param postId 帖子ID
 */
export function getPostDetail(postId: number): Promise<ApiResponse<帖子详情DTO>> {
  // /api 前缀由 request.ts 的 BASE_URL 和 Vite 代理处理
  // 这里的路径 /post/{id} 对应 Apifox 文档中的 GET /api/post/{id}
  return httpClient.get<帖子详情DTO>(`/post/${postId}`)
}

/**
 * 创建帖子
 * @param data 帖子数据
 */
export function createPost(data: {
  title: string
  content: string
  mediaUrls?: string[]
}): Promise<ApiResponse<Post>> {
  return httpClient.post<Post>('/post', data)
}

/**
 * 更新帖子
 * @param postId 帖子ID
 * @param data 更新数据
 */
export function updatePost(
  postId: number,
  data: Partial<Post>
): Promise<ApiResponse<Post>> {
  return httpClient.put<Post>(`/post/${postId}`, data)
}

/**
 * 删除帖子
 * @param postId 帖子ID
 */
export function deletePost(postId: number): Promise<ApiResponse<void>> {
  return httpClient.delete<void>(`/post/${postId}`)
}

/**
 * 删除自己的帖子（用于个人中心）
 * @param postId 帖子ID
 */
export function deleteMyPost(postId: number): Promise<ApiResponse<void>> {
  return httpClient.delete<void>(`/post/mine/${postId}`)
}

/**
 * 更新自己的帖子（用于个人中心）
 * @param postId 帖子ID
 * @param data 更新数据
 */
export function updateMyPost(
  postId: number,
  data: Partial<Post>
): Promise<ApiResponse<Post>> {
  return httpClient.put<Post>(`/post/mine/${postId}`, data)
}

/**
 * 获取用户帖子统计
 * @param userId 用户ID
 */
export function getUserPostStats(
  userId: number
): Promise<ApiResponse<{ total: number; approved: number; pending: number; rejected: number }>> {
  return httpClient.get<{ total: number; approved: number; pending: number; rejected: number }>(
    `/users/${userId}/posts/stats`
  )
}

