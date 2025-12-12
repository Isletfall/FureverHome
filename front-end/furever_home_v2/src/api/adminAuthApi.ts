import httpClient, { type ApiResponse } from './request'

export interface AdminLoginRequest {
  username: string
  password: string
}

export interface AdminLoginDTO {
  token: string
  [property: string]: any
}

export type AdminLoginResponse = ApiResponse<AdminLoginDTO>

/**
 * 管理后台登录
 */
export function adminLogin(data: AdminLoginRequest) {
  return httpClient.post<AdminLoginDTO>('/admin/login', data)
}


