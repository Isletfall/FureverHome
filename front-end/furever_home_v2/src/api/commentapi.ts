//评论相关接口
import request, { type ApiResponse } from './request'

export interface Comment {
  id: number
  content: string
  authorName: string
  authorAvatar?: string
  date: string
  parentId?: number | null
  replyTo?: string
  likes: number
  isLiked: boolean
  children?: Comment[]
  replyCount?: number // 子评论总数
}

/**
 * 获取帖子评论列表 (父评论)
 * @param postId 帖子ID
 * @param params 分页和排序参数
 */
export const getPostComments = (postId: number, params?: { page?: number; pageSize?: number; sortBy?: string; order?: string }): Promise<ApiResponse<any>> => {
  return request.get<any>(`/post/${postId}/comments`, { params })
}

/**
 * 获取评论的子评论列表
 * @param commentId 父评论ID
 * @param params 分页参数
 */
export const getCommentReplies = (commentId: number, params?: { page?: number; pageSize?: number; sortBy?: string; order?: string }): Promise<ApiResponse<any>> => {
  return request.get<any>(`/post/comments/${commentId}/replies`, { params })
}

/**
 * 提交评论
 * @param postId 帖子ID
 * @param payload 评论内容
 */
export const submitComment = (postId: number, payload: { content: string, parentId?: number, rootId?: number, replyTo?: string }): Promise<ApiResponse<Comment>> => {
  // 兼容后端可能接受的字段名（驼峰或下划线）
  const data = {
    ...payload,
    // Apifox 定义的字段名
    parentCommentId: payload.parentId,
    // 兼容其他命名习惯
    parent_id: payload.parentId,
    // 如果指定了 rootId 则使用 rootId，否则使用 parentId (兼容旧逻辑)
    root_parent_id: payload.rootId ?? payload.parentId,
    reply_comment_id: payload.parentId,
    reply_to: payload.replyTo,
    reply_to_user: payload.replyTo,
    commentContent: payload.content,
    text: payload.content
  }
  return request.post<Comment>(`/post/${postId}/comments`, data)
}

/**
 * 点赞/取消点赞帖子
 * @param postId 帖子ID
 */
export const likePost = (postId: number): Promise<ApiResponse<void>> => {
  return request.post<void>(`/post/${postId}/like`)
}

/**
 * 点赞/取消点赞评论
 * @param commentId 评论ID
 */
export const likeComment = (commentId: number): Promise<ApiResponse<void>> => {
  return request.post<void>(`/post/comments/${commentId}/like`)
}

/**
 * 增加帖子浏览数
 * @param postId 帖子ID
 */
export const incrementPostViews = (postId: number): Promise<ApiResponse<void>> => {
  return request.post<void>(`/post/${postId}/view`)
}
