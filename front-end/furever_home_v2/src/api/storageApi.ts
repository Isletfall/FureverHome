import httpClient, { type ApiResponse } from './request'

/**
 * 上传图片，返回图片 URL 字符串
 * 添加超时保护（30秒）
 */
export function uploadImage(file: File): Promise<ApiResponse<string>> {
  const formData = new FormData()
  formData.append('file', file)
  return httpClient.upload<string>('/storage/upload/image', formData, {
    timeout: 30000 // 30秒超时
  })
}

/**
 * 上传视频，返回视频 URL 字符串
 * 视频文件通常较大，设置更长的超时时间（5分钟）
 */
export function uploadVideo(file: File): Promise<ApiResponse<string>> {
  const formData = new FormData()
  formData.append('file', file)
  // 视频文件较大，设置5分钟超时（300秒 = 300000毫秒）
  return httpClient.upload<string>('/storage/upload/video', formData, {
    timeout: 300000 // 5分钟超时
  })
}

/**
 * 上传媒体文件（图片或视频），返回媒体 URL 字符串
 * 根据文件类型自动选择上传接口
 */
export function uploadMedia(file: File): Promise<ApiResponse<string>> {
  const formData = new FormData()
  formData.append('file', file)
  
  // 判断文件类型
  const isVideo = file.type.startsWith('video/')
  
  // 根据文件类型选择对应的上传接口
  const uploadUrl = isVideo 
    ? '/storage/upload/video'  // 视频上传接口
    : '/storage/upload/image'  // 图片上传接口
  
  // 视频可能较大，延长超时时间；图片保持 30 秒
  const timeout = isVideo ? 300000 : 30000

  return httpClient.upload<string>(uploadUrl, formData, { timeout })
}

/**
 * 删除图片
 * @param object 图片对象名（或从 URL 提取出的文件名）
 */
export function deleteImage(object: string): Promise<ApiResponse<any>> {
  return httpClient.delete<any>(`/storage/image/${encodeURIComponent(object)}`)
}

