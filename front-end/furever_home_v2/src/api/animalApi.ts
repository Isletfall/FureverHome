import httpClient, { type ApiResponse } from './request'

export interface AnimalListRequest {
  adoptionStatus?: AdoptionStatus
  age?: string
  city?: string
  gender?: Gender
  page?: number
  pageSize?: number
  province?: string
  species?: Species
  [property: string]: any
}

export interface PageResult动物公开信息 {
  page?: number
  pageSize?: number
  records?: 动物公开信息[]
  total?: number
  [property: string]: any
}

export interface 动物公开信息 {
  adopterName?: string
  adoptionDays?: number
  adoptionStatus?: AdoptionStatus
  animalAge?: number
  animalId?: number
  animalName?: string
  breed?: string
  gender?: Gender
  isSterilized?: IsSterilized
  photoUrls?: string[]
  shortDescription?: string
  // 审核状态（仅对发布者可见）
  reviewStatus?: ReviewStatus
  species?: Species
  userId?: number
  [property: string]: any
}

// 获取宠物详情用的完整动物信息 DTO
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
  /** 发布用户头像 */
  userAvatar?: string
  [property: string]: any
}

// 发布动物信息请求体
export interface CreateAnimalRequest {
  adoptionStatus: AdoptionStatus
  animalAge?: number
  animalName: string
  breed?: string
  city: string
  contactEmail: string
  contactPhone: string
  currentLocation: string
  gender: Gender
  healthStatus: string
  isSterilized: IsSterilized
  photoUrls?: string[]
  province: string
  shortDescription?: string
  species: Species
  [property: string]: any
}

// 更新动物信息请求体
export interface UpdateAnimalRequest {
  adoptionStatus?: AdoptionStatus
  animalAge?: number
  animalName?: string
  breed?: string
  city?: string
  contactEmail?: string
  contactPhone?: string
  currentLocation?: string
  gender?: Gender
  healthStatus?: string
  isSterilized?: IsSterilized
  photoUrls?: string[]
  province?: string
  shortDescription?: string
  species?: Species
  [property: string]: any
}

export enum AdoptionStatus {
  短期领养 = '短期领养',
  长期领养 = '长期领养',
}

export enum Gender {
  公 = '公',
  未知 = '未知',
  母 = '母',
}

export enum IsSterilized {
  否 = '否',
  是 = '是',
  未知 = '未知',
}

export enum ReviewStatus {
  待审核 = '待审核',
  拒绝 = '拒绝',
  通过 = '通过',
}

export enum Species {
  仓鼠 = '仓鼠',
  兔子 = '兔子',
  其他 = '其他',
  狗 = '狗',
  猫 = '猫',
  鱼类 = '鱼类',
  鸟类 = '鸟类',
  龟类 = '龟类',
}

export type AnimalListResponse = ApiResponse<PageResult动物公开信息>
export type AnimalDetailResponse = ApiResponse<动物信息>
export type CreateAnimalResponse = ApiResponse<void>
export type DeleteAnimalResponse = ApiResponse<void>
export type UpdateAnimalResponse = ApiResponse<void>

export function getAnimalList(params: AnimalListRequest) {
  return httpClient.get<PageResult动物公开信息>('/animal/list', { params })
}

export function getAnimalDetail(id: number) {
  return httpClient.get<动物信息>(`/animal/${id}`)
}

// 发布动物信息
export function createAnimal(data: CreateAnimalRequest) {
  return httpClient.post<void>('/animal', data)
}

// 删除动物信息
export function deleteAnimal(id: number) {
  return httpClient.delete<void>(`/animal/${id}`)
}

// 更新动物信息
export function updateAnimal(id: number, data: UpdateAnimalRequest) {
  return httpClient.put<void>(`/animal/${id}`, data)
}

// 获取当前用户发布的短期宠物列表
export function getMyShortAnimals(params: AnimalListRequest) {
  return httpClient.get<PageResult动物公开信息>('/animal/mine/short', { params })
}

// 获取当前用户发布的长期宠物列表
export function getMyLongAnimals(params: AnimalListRequest) {
  return httpClient.get<PageResult动物公开信息>('/animal/mine/long', { params })
}

// 获取他人短期宠物列表
// 对应后端接口：GET /api/animal/user/{userId}/short
export function getUserShortAnimals(
  userId: number,
  params?: { page?: number; pageSize?: number; [property: string]: any }
) {
  return httpClient.get<PageResult动物公开信息>(`/animal/user/${userId}/short`, { params })
}

// 获取他人长期宠物列表
// 对应后端接口：GET /api/animal/user/{userId}/long
export function getUserLongAnimals(
  userId: number,
  params?: { page?: number; pageSize?: number; [property: string]: any }
) {
  return httpClient.get<PageResult动物公开信息>(`/animal/user/${userId}/long`, { params })
}
