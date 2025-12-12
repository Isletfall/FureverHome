import { formatDateTime } from '@/utils/format';
import type { Comment } from '@/api/commentapi';

export const normalizeComments = (list: any[]): Comment[] => {
  if (!Array.isArray(list)) return [];
  return list.map((item: any, index: number) => {
    const authorName = item.authorName ?? item.userName ?? item.nickName ?? item.nickname ?? '用户';
    const ts =
      item.createTime
        ? new Date(item.createTime).getTime()
        : item.timeStamp
          ? Number(item.timeStamp)
          : Date.now();
    
    // 递归处理子评论
    let children: Comment[] = [];
    if (Array.isArray(item.children) && item.children.length > 0) {
       children = normalizeComments(item.children);
    } else if (Array.isArray(item.replies) && item.replies.length > 0) {
       children = normalizeComments(item.replies);
    }

    return {
      id: item.id ?? item.commentId ?? index + 1,
      content: item.content ?? item.commentContent ?? item.text ?? item.body ?? item.message ?? '',
      authorName: authorName,
      authorAvatar: item.authorAvatar ?? item.avatar ?? item.userAvatar ?? '',
      date: formatDateTime(item.createTime || item.date) || item.timeAgo || '刚刚',
      likes: item.likes ?? item.likeCount ?? 0,
      isLiked: item.isLiked ?? item.liked ?? false,
      parentId: item.parentId ?? item.parentCommentId ?? item.parent_id ?? null,
      replyTo: item.replyTo ?? item.replyToUser ?? item.reply_to_user_name ?? item.to?.userName ?? item.to?.name ?? undefined,
      children: children,
      replyCount: item.replyCount ?? item.childrenCount ?? item.reply_count ?? children.length ?? 0,
      _ts: ts
    };
  });
};
