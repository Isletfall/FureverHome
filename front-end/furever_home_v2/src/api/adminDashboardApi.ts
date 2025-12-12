import { adminHttpClient, type ApiResponse } from './request'

export interface DashboardStatisticsDTO {
  /**
   * 已发布长期宠物数量
   */
  longTermPetCount?: number

  /**
   * 待审核的领养申请数量
   */
  pendingAdoptCount?: number

  /**
   * 待审核的宠物数量
   */
  pendingPetCount?: number

  /**
   * 待审核的帖子数量
   */
  pendingPostCount?: number

  /**
   * 已发布短期宠物数量
   */
  shortTermPetCount?: number

  /**
   * 已发布帖子数量
   */
  totalPostCount?: number

  [property: string]: any
}

export interface DashboardStatisticsResponse extends ApiResponse<DashboardStatisticsDTO> {}

/**
 * 获取管理后台首页统计数据
 */
export function fetchDashboardStatistics() {
  return adminHttpClient.get<DashboardStatisticsDTO>('/admin/dashboard/statistics')
}


