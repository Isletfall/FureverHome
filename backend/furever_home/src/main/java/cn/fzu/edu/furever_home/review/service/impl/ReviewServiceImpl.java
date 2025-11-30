package cn.fzu.edu.furever_home.review.service.impl;

import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.review.entity.Review;
import cn.fzu.edu.furever_home.review.mapper.ReviewMapper;
import cn.fzu.edu.furever_home.review.service.ReviewService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewMapper reviewMapper;

    @Override
    public void createPending(ReviewTargetType targetType, Integer targetId) {
        Review r = new Review();
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setStatus(ReviewStatus.PENDING);
        r.setCreateTime(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        reviewMapper.insert(r);
    }

    @Override
    public void updateStatus(ReviewTargetType targetType, Integer targetId, ReviewStatus status) {
        reviewMapper.update(null, new LambdaUpdateWrapper<Review>()
                .eq(Review::getTargetType, targetType)
                .eq(Review::getTargetId, targetId)
                .set(Review::getStatus, status)
                .set(Review::getUpdatedAt, LocalDateTime.now()));
    }
}