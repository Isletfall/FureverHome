package cn.fzu.edu.furever_home.review.service;

import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;

public interface ReviewService {
    void createPending(ReviewTargetType targetType, Integer targetId);
    void updateStatus(ReviewTargetType targetType, Integer targetId, ReviewStatus status);
}