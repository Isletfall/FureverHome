/**
 * 申请相关API
 */
import httpClient, { type ApiResponse } from './request'

// 申请接口
export interface Application {
  id: number
  petId: number
  petName: string
  applicantId: number
  applicantName?: string
  reason: string
  status: 'pending' | 'approved' | 'rejected'
  statusLabel: string
  date: string
  ownerId?: number
  ownerName?: string
  reply?: string
  replyDate?: string
}

// 创建申请请求接口
export interface CreateApplicationRequest {
  petId: number
  reason: string
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
 * 创建领养申请
 * @param data 申请数据
 */
export function createApplication(
  data: CreateApplicationRequest
): Promise<ApiResponse<Application>> {
  return httpClient.post<Application>('/applications', data)
}

/**
 * 获取用户的申请列表（分页）
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserApplications(
  userId: number,
  params: PaginationParams
): Promise<ApiResponse<PaginatedResponse<Application>>> {
  return httpClient.get<PaginatedResponse<Application>>(
    `/users/${userId}/applications`,
    { params }
  )
}

/**
 * 获取收到的申请列表（分页）- 作为宠物主人
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getReceivedApplications(
  userId: number,
  params: PaginationParams
): Promise<ApiResponse<PaginatedResponse<Application>>> {
  return httpClient.get<PaginatedResponse<Application>>(
    `/users/${userId}/applications/received`,
    { params }
  )
}

/**
 * 获取申请详情
 * @param applicationId 申请ID
 */
export function getApplicationDetail(
  applicationId: number
): Promise<ApiResponse<Application>> {
  return httpClient.get<Application>(`/applications/${applicationId}`)
}

/**
 * 处理申请（同意/拒绝）
 * @param applicationId 申请ID
 * @param action 操作：'approve' | 'reject'
 * @param reply 回复内容（可选）
 */
export function handleApplication(
  applicationId: number,
  action: 'approve' | 'reject',
  reply?: string
): Promise<ApiResponse<Application>> {
  return httpClient.post<Application>(`/applications/${applicationId}/handle`, {
    action,
    reply,
  })
}

/**
 * 取消申请
 * @param applicationId 申请ID
 */
export function cancelApplication(
  applicationId: number
): Promise<ApiResponse<void>> {
  return httpClient.delete<void>(`/applications/${applicationId}`)
}

/**
 * 获取申请统计
 * @param userId 用户ID
 */
export function getApplicationStats(
  userId: number
): Promise<ApiResponse<{
  sent: { total: number; pending: number; approved: number; rejected: number }
  received: { total: number; pending: number; approved: number; rejected: number }
}>> {
  return httpClient.get<{
    sent: { total: number; pending: number; approved: number; rejected: number }
    received: { total: number; pending: number; approved: number; rejected: number }
  }>(`/users/${userId}/applications/stats`)
}

