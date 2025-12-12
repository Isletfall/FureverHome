/**
 * 宠物相关API
 */
import httpClient, { type ApiResponse } from './request'

// 宠物接口
export interface Pet {
  id: number
  name: string
  gender: '公' | '母'
  desc: string
  days: number
  status: 'approved' | 'pending' | 'rejected'
  statusLabel: string
  bgClass: string
  titleClass: string
  reason?: string
  images?: string[]
  adoptionType: 'short-term' | 'long-term'
  ownerId: number
  ownerName?: string
  breed?: string
  age?: string
  vaccinated?: boolean
  neutered?: boolean
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
 * 获取用户短期领养的宠物列表（分页）
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserShortTermPets(
  userId: number,
  params: PaginationParams
): Promise<ApiResponse<PaginatedResponse<Pet>>> {
  return httpClient.get<PaginatedResponse<Pet>>(
    `/users/${userId}/pets/short-term`,
    { params }
  )
}

/**
 * 获取用户长期领养的宠物列表（分页）
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserLongTermPets(
  userId: number,
  params: PaginationParams
): Promise<ApiResponse<PaginatedResponse<Pet>>> {
  return httpClient.get<PaginatedResponse<Pet>>(
    `/users/${userId}/pets/long-term`,
    { params }
  )
}

/**
 * 获取用户所有领养的宠物列表（分页）
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserAllPets(
  userId: number,
  params: PaginationParams
): Promise<ApiResponse<PaginatedResponse<Pet>>> {
  return httpClient.get<PaginatedResponse<Pet>>(
    `/users/${userId}/pets`,
    { params }
  )
}

/**
 * 获取宠物详情
 * @param petId 宠物ID
 */
export function getPetDetail(petId: number): Promise<ApiResponse<Pet>> {
  return httpClient.get<Pet>(`/pets/${petId}`)
}

/**
 * 获取用户领养的宠物统计
 * @param userId 用户ID
 */
export function getUserPetStats(
  userId: number
): Promise<ApiResponse<{ shortTerm: number; longTerm: number; total: number }>> {
  return httpClient.get<{ shortTerm: number; longTerm: number; total: number }>(
    `/users/${userId}/pets/stats`
  )
}

