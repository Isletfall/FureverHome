//发布帖子接口
import request from './request'

export interface NewPostPayload {
  title: string
  content: string
  files?: File[]
}

export const submitNewPost = (payload: NewPostPayload) => {
  const formData = new FormData()
  formData.append('title', payload.title)
  formData.append('content', payload.content)
  payload.files?.forEach((file) => formData.append('files', file))

  return request.post<{ data: { id: number } }>('/posts', formData)
}
