import httpClient, { type ApiResponse } from './request'

export interface ReceivedRatingItemDTO {
  content?: string
  createTime?: string
  otherUserAvatar?: string
  otherUserId?: number
  otherUserName?: string
  ratingId?: number
  score?: number
  [property: string]: any
}

export interface PageResultReceivedRatingItemDTO {
  page?: number
  pageSize?: number
  records?: ReceivedRatingItemDTO[]
  total?: number
  [property: string]: any
}

export interface RatingListRequest {
  page?: number
  pageSize?: number
  [property: string]: any
}

export type ReceivedRatingListResponse = ApiResponse<PageResultReceivedRatingItemDTO>

// 添加我对某个用户的评价（Query 方式：score, content）
export interface AddMyRatingRequest {
  content: string
  score: number
}

// 获取我的评价（他人对我的评价）列表
export function getReceivedRatings(params: RatingListRequest) {
  return httpClient.get<PageResultReceivedRatingItemDTO>('/rating/received', { params })
}

// 获取他人对某个用户的评价列表（用于个人主页查看他人评价）
// 按后端定义：GET /api/rating/others/{targetUserId}?page=1&pageSize=50
export function getOthersRatings(
  targetUserId: number,
  params: RatingListRequest
) {
  return httpClient.get<PageResultReceivedRatingItemDTO>(`/rating/others/${targetUserId}`, {
    params,
  })
}

// 添加我对某个用户的评价
// 后端文档：POST /api/rating/mine/{targetUserId}
// Path 参数：targetUserId，Query 参数：score, content
export function addMyRating(
  targetUserId: number,
  data: AddMyRatingRequest
) {
  return httpClient.post<void>(
    `/rating/mine/${targetUserId}`,
    undefined,
    { params: { score: data.score, content: data.content } },
  )
}

// 修改我对某个用户的评价（Query 方式：ratingId, score, content）
export interface UpdateMyRatingRequest {
  ratingId: number
  content: string
  score: number
}

// 对应后端接口：PUT /api/rating/mine/{targetUserId}
export function updateMyRating(
  targetUserId: number,
  data: UpdateMyRatingRequest
) {
  return httpClient.put<void>(
    `/rating/mine/${targetUserId}`,
    undefined,
    { params: { ratingId: data.ratingId, score: data.score, content: data.content } },
  )
}