/**
 * 评价相关API
 */
import httpClient, { type ApiResponse } from './request'

// 评价接口
export interface Evaluation {
  id: number
  author: string
  authorId: number
  stars: number
  content: string
  date: string
  appealable?: boolean
  reply?: {
    content: string
    date: string
  }
}

// 添加评价请求接口
export interface AddEvaluationRequest {
  userId: number
  stars: number
  content: string
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

/**
 * 获取用户评价列表（分页）
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserEvaluations(
  userId: number,
  params: PaginationParams
): Promise<ApiResponse<PaginatedResponse<Evaluation>>> {
  return httpClient.get<PaginatedResponse<Evaluation>>(
    `/users/${userId}/evaluations`,
    { params }
  )
}

/**
 * 添加评价
 * @param data 评价数据
 */
export function addEvaluation(
  data: AddEvaluationRequest
): Promise<ApiResponse<Evaluation>> {
  return httpClient.post<Evaluation>('/evaluations', data)
}

/**
 * 删除评价（仅评价作者可删除）
 * @param evaluationId 评价ID
 */
export function deleteEvaluation(
  evaluationId: number
): Promise<ApiResponse<void>> {
  return httpClient.delete<void>(`/evaluations/${evaluationId}`)
}

/**
 * 回复评价
 * @param evaluationId 评价ID
 * @param content 回复内容
 */
export function replyEvaluation(
  evaluationId: number,
  content: string
): Promise<ApiResponse<Evaluation>> {
  return httpClient.post<Evaluation>(`/evaluations/${evaluationId}/reply`, {
    content,
  })
}

/**
 * 申诉评价（如果评价不合理）
 * @param evaluationId 评价ID
 * @param reason 申诉理由
 */
export function appealEvaluation(
  evaluationId: number,
  reason: string
): Promise<ApiResponse<void>> {
  return httpClient.post<void>(`/evaluations/${evaluationId}/appeal`, {
    reason,
  })
}

