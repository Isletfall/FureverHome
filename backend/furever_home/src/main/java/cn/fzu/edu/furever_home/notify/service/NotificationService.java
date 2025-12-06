package cn.fzu.edu.furever_home.notify.service;

import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.notify.dto.NotificationItemDTO;

public interface NotificationService {
    void notifyActivity(Integer recipientId, Integer actorId, String targetType, Integer targetId, String event,
            String title, String content, String extraJson);

    PageResult<NotificationItemDTO> pageMyActivities(Integer userId, boolean onlyUnread, String targetType, int page,
            int pageSize);

    void markRead(Integer userId, Integer activityId);

    void markAllRead(Integer userId);
}
