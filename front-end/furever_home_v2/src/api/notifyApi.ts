import httpClient, { type ApiResponse } from './request';

export interface NotifyRecord {
  activityId?: number;
  recipientId?: number;
  actorId?: number;
  targetType?: string;
  targetId?: number;
  event?: string;
  title?: string;
  content?: string;
  extraInfo?: string;
  isRead?: boolean;
  createTime?: string;
  // 以下字段用于生成消息内容
  actorUserId?: number;
  actorUserName?: string;
  actorUserAvatar?: string | null;
  postTitle?: string | null;
  postAuthorId?: number | null;
  postAuthorName?: string | null;
  postAuthorAvatar?: string | null;
  adoptApplicationStatus?: string;
  adoptReviewStatus?: string;
  applicantUserId?: number;
  applicantUserName?: string;
  applicantUserAvatar?: string | null;
  animalId?: number;
  animalName?: string;
  animalPhoto?: string;
  animalSpecies?: string | null;
  animalPhotoUrls?: string[] | null;
}

export interface PageResultNotify {
  page?: number;
  pageSize?: number;
  total?: number;
  records?: NotifyRecord[];
}

export type NotifyListResponse = ApiResponse<PageResultNotify>;

export interface NotifyListParams {
  onlyUnread?: boolean;
  page?: number;
  pageSize?: number;
}

// 查看我的通知列表
export function getMyNotifications(params?: NotifyListParams) {
  return httpClient.get<PageResultNotify>('/notify/list', { params });
}

// 标记全部已读
export function markAllNotificationsRead() {
  return httpClient.put('/notify/read/all');
}

// 标记单条已读
export function markNotificationRead(id: number) {
  return httpClient.put(`/notify/read/${id}`);
}

