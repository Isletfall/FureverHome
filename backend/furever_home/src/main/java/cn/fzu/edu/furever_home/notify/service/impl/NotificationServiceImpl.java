package cn.fzu.edu.furever_home.notify.service.impl;

import cn.fzu.edu.furever_home.notify.entity.RecentActivity;
import cn.fzu.edu.furever_home.notify.mapper.RecentActivityMapper;
import cn.fzu.edu.furever_home.notify.service.NotificationService;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.notify.dto.NotificationItemDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import cn.fzu.edu.furever_home.ws.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final RecentActivityMapper recentActivityMapper;
    private final WebSocketSessionManager wsSessionManager;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final cn.fzu.edu.furever_home.post.mapper.PostMapper postMapper;
    private final cn.fzu.edu.furever_home.adopt.mapper.AdoptMapper adoptMapper;
    private final cn.fzu.edu.furever_home.animal.mapper.AnimalMapper animalMapper;
    private final cn.fzu.edu.furever_home.auth.mapper.UserMapper userMapper;
    private final cn.fzu.edu.furever_home.review.mapper.ReviewMapper reviewMapper;

    @Override
    public void notifyActivity(Integer recipientId, Integer actorId, String targetType, Integer targetId, String event,
            String title, String content, String extraJson) {
        RecentActivity ra = new RecentActivity();
        ra.setRecipientId(recipientId);
        ra.setActorId(actorId);
        ra.setTargetType(targetType);
        ra.setTargetId(targetId);
        ra.setEvent(event);
        ra.setTitle(title);
        ra.setContent(content);
        ra.setExtraInfo(extraJson);
        ra.setIsRead(false);
        ra.setCreateTime(LocalDateTime.now());
        recentActivityMapper.insert(ra);
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("activityId", ra.getActivityId());
            data.put("recipientId", recipientId);
            data.put("actorId", actorId);
            data.put("targetType", targetType);
            data.put("targetId", targetId);
            data.put("event", event);
            data.put("title", title);
            data.put("content", content);
            String text = objectMapper.writeValueAsString(new HashMap<String, Object>() {
                {
                    put("type", "activity");
                    put("data", data);
                }
            });
            if (wsSessionManager.hasOnline(recipientId)) {
                wsSessionManager.sendToUser(recipientId, text);
            } else {
                stringRedisTemplate.opsForList().leftPush("notify:pending:" + recipientId, text);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public PageResult<NotificationItemDTO> pageMyActivities(Integer userId, boolean onlyUnread, String targetType,
            int page, int pageSize) {
        Page<RecentActivity> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<RecentActivity> wrapper = new LambdaQueryWrapper<RecentActivity>()
                .eq(RecentActivity::getRecipientId, userId)
                .orderByDesc(RecentActivity::getCreateTime);
        if (onlyUnread) {
            wrapper.eq(RecentActivity::getIsRead, false);
        }
        if (targetType != null && !targetType.isBlank()) {
            wrapper.eq(RecentActivity::getTargetType, targetType);
        }
        Page<RecentActivity> result = recentActivityMapper.selectPage(mpPage, wrapper);
        java.util.List<NotificationItemDTO> records = result.getRecords().stream().map(ra -> {
            NotificationItemDTO d = new NotificationItemDTO();
            d.setActivityId(ra.getActivityId());
            d.setIsRead(ra.getIsRead());
            d.setEvent(ra.getEvent());
            d.setTitle(ra.getTitle());
            d.setContent(ra.getContent());
            d.setCreateTime(ra.getCreateTime());
            d.setTargetType(ra.getTargetType());
            d.setTargetId(ra.getTargetId());
            d.setActorUserId(ra.getActorId());
            if (ra.getActorId() != null) {
                cn.fzu.edu.furever_home.auth.entity.User actor = userMapper.selectById(ra.getActorId());
                if (actor != null) {
                    d.setActorUserName(actor.getUserName());
                    d.setActorUserAvatar(actor.getAvatarUrl());
                }
            }
            String tt = ra.getTargetType();
            Integer tid = ra.getTargetId();
            if ("post".equalsIgnoreCase(tt) && tid != null) {
                cn.fzu.edu.furever_home.post.entity.Post p = postMapper.selectById(tid);
                if (p != null) {
                    d.setPostTitle(p.getTitle());
                    if (p.getUserId() != null) {
                        cn.fzu.edu.furever_home.auth.entity.User author = userMapper.selectById(p.getUserId());
                        if (author != null) {
                            d.setPostAuthorId(author.getUserId());
                            d.setPostAuthorName(author.getUserName());
                            d.setPostAuthorAvatar(author.getAvatarUrl());
                        }
                    }
                }
            } else if ("adopt".equalsIgnoreCase(tt) && tid != null) {
                cn.fzu.edu.furever_home.adopt.entity.Adopt a = adoptMapper.selectById(tid);
                if (a != null) {
                    d.setAdoptApplicationStatus(
                            a.getApplicationStatus() == null ? null : a.getApplicationStatus().getValue());
                    d.setAdoptReviewStatus(a.getReviewStatus() == null ? null : a.getReviewStatus().getValue());
                    d.setApplicantUserId(a.getUserId());
                    if (a.getUserId() != null) {
                        cn.fzu.edu.furever_home.auth.entity.User u = userMapper.selectById(a.getUserId());
                        if (u != null) {
                            d.setApplicantUserName(u.getUserName());
                            d.setApplicantUserAvatar(u.getAvatarUrl());
                        }
                    }
                    d.setAnimalId(a.getAnimalId());
                    if (a.getAnimalId() != null) {
                        cn.fzu.edu.furever_home.animal.entity.Animal an = animalMapper.selectById(a.getAnimalId());
                        if (an != null) {
                            d.setAnimalName(an.getAnimalName());
                            java.util.List<String> photos = an.getPhotoUrls();
                            d.setAnimalPhoto(photos == null || photos.isEmpty() ? null : photos.get(0));
                        }
                    }
                }
            } else if ("review".equalsIgnoreCase(tt) && tid != null) {
                cn.fzu.edu.furever_home.review.entity.Review rv = reviewMapper.selectById(tid);
                if (rv != null) {
                    cn.fzu.edu.furever_home.common.enums.ReviewTargetType rtt = rv.getTargetType();
                    Integer rid = rv.getTargetId();
                    if (rtt == cn.fzu.edu.furever_home.common.enums.ReviewTargetType.POST && rid != null) {
                        cn.fzu.edu.furever_home.post.entity.Post p = postMapper.selectById(rid);
                        if (p != null) {
                            d.setPostTitle(p.getTitle());
                            if (p.getUserId() != null) {
                                cn.fzu.edu.furever_home.auth.entity.User author = userMapper.selectById(p.getUserId());
                                if (author != null) {
                                    d.setPostAuthorId(author.getUserId());
                                    d.setPostAuthorName(author.getUserName());
                                    d.setPostAuthorAvatar(author.getAvatarUrl());
                                }
                            }
                        }
                    } else if (rtt == cn.fzu.edu.furever_home.common.enums.ReviewTargetType.ADOPT && rid != null) {
                        cn.fzu.edu.furever_home.adopt.entity.Adopt a = adoptMapper.selectById(rid);
                        if (a != null) {
                            d.setAdoptApplicationStatus(
                                    a.getApplicationStatus() == null ? null : a.getApplicationStatus().getValue());
                            d.setAdoptReviewStatus(
                                    a.getReviewStatus() == null ? null : a.getReviewStatus().getValue());
                            d.setApplicantUserId(a.getUserId());
                            if (a.getUserId() != null) {
                                cn.fzu.edu.furever_home.auth.entity.User u = userMapper.selectById(a.getUserId());
                                if (u != null) {
                                    d.setApplicantUserName(u.getUserName());
                                    d.setApplicantUserAvatar(u.getAvatarUrl());
                                }
                            }
                            d.setAnimalId(a.getAnimalId());
                            if (a.getAnimalId() != null) {
                                cn.fzu.edu.furever_home.animal.entity.Animal an = animalMapper
                                        .selectById(a.getAnimalId());
                                if (an != null) {
                                    d.setAnimalName(an.getAnimalName());
                                    java.util.List<String> photos = an.getPhotoUrls();
                                    d.setAnimalPhoto(photos == null || photos.isEmpty() ? null : photos.get(0));
                                }
                            }
                        }
                    } else if (rtt == cn.fzu.edu.furever_home.common.enums.ReviewTargetType.ANIMAL && rid != null) {
                        cn.fzu.edu.furever_home.animal.entity.Animal an = animalMapper.selectById(rid);
                        if (an != null) {
                            d.setAnimalId(an.getAnimalId());
                            d.setAnimalName(an.getAnimalName());
                            d.setAnimalSpecies(String.valueOf(an.getSpecies()));
                            d.setAnimalPhotoUrls(an.getPhotoUrls());
                        }
                    }
                }
            } else if ("animal".equalsIgnoreCase(tt) && tid != null) {
                cn.fzu.edu.furever_home.animal.entity.Animal an = animalMapper.selectById(tid);
                if (an != null) {
                    d.setAnimalId(an.getAnimalId());
                    d.setAnimalName(an.getAnimalName());
                    d.setAnimalSpecies(String.valueOf(an.getSpecies()));
                    d.setAnimalPhotoUrls(an.getPhotoUrls());
                }
            }
            return d;
        }).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public void markRead(Integer userId, Integer activityId) {
        recentActivityMapper.update(null, new LambdaUpdateWrapper<RecentActivity>()
                .eq(RecentActivity::getActivityId, activityId)
                .eq(RecentActivity::getRecipientId, userId)
                .set(RecentActivity::getIsRead, true));

        String key = "notify:pending:" + userId;
        java.util.List<String> entries = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (entries != null && !entries.isEmpty()) {
            for (String e : entries) {
                try {
                    JsonNode root = objectMapper.readTree(e);
                    JsonNode data = root.get("data");
                    if (data != null) {
                        JsonNode idNode = data.get("activityId");
                        if (idNode != null && idNode.isInt() && idNode.intValue() == activityId) {
                            stringRedisTemplate.opsForList().remove(key, 0, e);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void markAllRead(Integer userId) {
        recentActivityMapper.update(null, new LambdaUpdateWrapper<RecentActivity>()
                .eq(RecentActivity::getRecipientId, userId)
                .eq(RecentActivity::getIsRead, false)
                .set(RecentActivity::getIsRead, true));

        String key = "notify:pending:" + userId;
        stringRedisTemplate.delete(key);
    }
}
