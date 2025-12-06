package cn.fzu.edu.furever_home.admin.service.impl;

import cn.fzu.edu.furever_home.admin.dto.AdminAdoptDetailDTO;
import cn.fzu.edu.furever_home.admin.dto.AdminAdoptSummaryDTO;
import cn.fzu.edu.furever_home.admin.service.AdminAdoptService;
import cn.fzu.edu.furever_home.adopt.entity.Adopt;
import cn.fzu.edu.furever_home.adopt.mapper.AdoptMapper;
import cn.fzu.edu.furever_home.animal.entity.Animal;
import cn.fzu.edu.furever_home.animal.mapper.AnimalMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.common.enums.ApplicationStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.review.entity.Review;
import cn.fzu.edu.furever_home.review.mapper.ReviewMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAdoptServiceImpl implements AdminAdoptService {

    private final AdoptMapper adoptMapper;
    private final AnimalMapper animalMapper;
    private final UserMapper userMapper;
    private final ReviewMapper reviewMapper;
    private final cn.fzu.edu.furever_home.notify.service.NotificationService notificationService;

    @Override
    public PageResult<AdminAdoptSummaryDTO> listPending(int page, int pageSize, String keyword) {
        Page<Adopt> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Adopt> wrapper = new LambdaQueryWrapper<Adopt>()
                .eq(Adopt::getIsDeleted, false)
                .eq(Adopt::getReviewStatus, ReviewStatus.PENDING)
                .orderByAsc(Adopt::getAdoptId);
        if (keyword != null && !keyword.isBlank()) {
            applyKeywordFilter(wrapper, keyword);
        }
        Page<Adopt> resultPage = adoptMapper.selectPage(mpPage, wrapper);
        List<AdminAdoptSummaryDTO> records = resultPage.getRecords().stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal(), records);
    }

    @Override
    public PageResult<AdminAdoptSummaryDTO> listProcessed(int page, int pageSize, String keyword) {
        Page<Adopt> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Adopt> wrapper = new LambdaQueryWrapper<Adopt>()
                .eq(Adopt::getIsDeleted, false)
                .ne(Adopt::getReviewStatus, ReviewStatus.PENDING)
                .orderByAsc(Adopt::getAdoptId);
        // 处理已审核列表的关键字模糊查询（申请人姓名 / 宠物名称）
        if (keyword != null && !keyword.isBlank()) {
            applyKeywordFilter(wrapper, keyword);
        }
        Page<Adopt> resultPage = adoptMapper.selectPage(mpPage, wrapper);
        List<AdminAdoptSummaryDTO> records = resultPage.getRecords().stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal(), records);
    }

    @Override
    public AdminAdoptDetailDTO getDetail(Integer adoptId) {
        Adopt adopt = adoptMapper.selectById(adoptId);
        if (adopt == null) {
            return null;
        }
        AdminAdoptDetailDTO dto = new AdminAdoptDetailDTO();
        dto.setAdoptId(adopt.getAdoptId());
        dto.setAnimalId(adopt.getAnimalId());
        dto.setUserId(adopt.getUserId());
        dto.setApplicationStatus(adopt.getApplicationStatus());
        dto.setReviewStatus(adopt.getReviewStatus());
        // 目前 Adopt 实体中只保留了基础信息，这里只映射仍然存在的字段
        dto.setAdoptReason(adopt.getAdoptReason());
        dto.setCreateTime(adopt.getCreateTime());
        dto.setPassTime(adopt.getPassTime());

        if (adopt.getAnimalId() != null) {
            Animal animal = animalMapper.selectById(adopt.getAnimalId());
            if (animal != null) {
                dto.setAnimalName(animal.getAnimalName());
                dto.setAnimalSpecies(animal.getSpecies());
                dto.setAnimalGender(animal.getGender());
                // 被申请用户 = 宠物发布者
                if (animal.getUserId() != null) {
                    User owner = userMapper.selectById(animal.getUserId());
                    if (owner != null) {
                        dto.setTargetUserId(owner.getUserId());
                        dto.setTargetUserName(owner.getUserName());
                        dto.setTargetUserAvatar(owner.getAvatarUrl());
                    }
                }
            }
        }
        if (adopt.getUserId() != null) {
            User user = userMapper.selectById(adopt.getUserId());
            if (user != null) {
                dto.setUserName(user.getUserName());
                dto.setUserAvatar(user.getAvatarUrl());
                dto.setAddress(user.getLocation());
            }
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Integer reviewerId, Integer adoptId, String reason) {
        updateReviewAndAdoptStatus(reviewerId, adoptId, reason, ReviewStatus.APPROVED, ApplicationStatus.SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Integer reviewerId, Integer adoptId, String reason) {
        updateReviewAndAdoptStatus(reviewerId, adoptId, reason, ReviewStatus.REJECTED, ApplicationStatus.FAIL);
    }

    private void applyKeywordFilter(LambdaQueryWrapper<Adopt> wrapper, String keyword) {
        String kw = keyword.trim();
        Integer id = tryParseInt(kw);

        // 先根据关键字查出匹配的用户和宠物ID，再在申请记录里按 userId / animalId 过滤
        List<Integer> userIds = userMapper.selectList(
                new LambdaQueryWrapper<User>().like(User::getUserName, kw))
                .stream().map(User::getUserId).toList();
        List<Integer> animalIds = animalMapper.selectList(
                new LambdaQueryWrapper<Animal>().like(Animal::getAnimalName, kw))
                .stream().map(Animal::getAnimalId).toList();

        // 如果关键字可以解析成数字，则额外按 ID 维度进行匹配
        if (id != null) {
            List<Integer> tmpUserIds = new ArrayList<>(userIds);
            tmpUserIds.add(id);
            userIds = tmpUserIds;

            List<Integer> tmpAnimalIds = new ArrayList<>(animalIds);
            tmpAnimalIds.add(id);
            animalIds = tmpAnimalIds;
        }

        List<Integer> finalUserIds = userIds;
        List<Integer> finalAnimalIds = animalIds;

        wrapper.and(w -> {
            boolean hasCond = false;
            if (!finalUserIds.isEmpty()) {
                w.in(Adopt::getUserId, finalUserIds);
                hasCond = true;
            }
            if (!finalAnimalIds.isEmpty()) {
                if (hasCond) {
                    w.or();
                }
                w.in(Adopt::getAnimalId, finalAnimalIds);
                hasCond = true;
            }
            // 直接按申请 ID 精确匹配
            if (id != null) {
                if (hasCond) {
                    w.or();
                }
                w.eq(Adopt::getAdoptId, id);
                hasCond = true;
            }
            // 如果都没有匹配到，则强制无结果
            if (!hasCond) {
                w.apply("1 = 0");
            }
        });
    }

    private AdminAdoptSummaryDTO toSummaryDTO(Adopt a) {
        AdminAdoptSummaryDTO dto = new AdminAdoptSummaryDTO();
        dto.setAdoptId(a.getAdoptId());
        dto.setAnimalId(a.getAnimalId());
        dto.setUserId(a.getUserId());
        dto.setApplicationStatus(a.getApplicationStatus());
        dto.setReviewStatus(a.getReviewStatus());
        dto.setCreateTime(a.getCreateTime());
        dto.setPassTime(a.getPassTime());
        if (a.getAnimalId() != null) {
            Animal animal = animalMapper.selectById(a.getAnimalId());
            if (animal != null) {
                dto.setAnimalName(animal.getAnimalName());
                // 被申请用户 = 宠物发布者
                if (animal.getUserId() != null) {
                    User owner = userMapper.selectById(animal.getUserId());
                    if (owner != null) {
                        dto.setTargetUserId(owner.getUserId());
                        dto.setTargetUserName(owner.getUserName());
                        dto.setTargetUserAvatar(owner.getAvatarUrl());
                    }
                }
            }
        }
        if (a.getUserId() != null) {
            User user = userMapper.selectById(a.getUserId());
            if (user != null) {
                dto.setUserName(user.getUserName());
                dto.setUserAvatar(user.getAvatarUrl());
            }
        }
        return dto;
    }

    private void updateReviewAndAdoptStatus(Integer reviewerId, Integer adoptId, String reason,
            ReviewStatus reviewStatus, ApplicationStatus applicationStatus) {
        if (adoptId == null) {
            throw new IllegalArgumentException("adoptId 不能为空");
        }
        // 使用悲观锁（SELECT FOR UPDATE）锁定当前申请记录，防止并发审核
        Adopt current = adoptMapper.selectOne(
                new LambdaQueryWrapper<Adopt>()
                        .eq(Adopt::getAdoptId, adoptId)
                        .eq(Adopt::getIsDeleted, false)
                        .last("FOR UPDATE"));
        if (current == null) {
            throw new IllegalStateException("领养申请不存在");
        }
        if (current.getReviewStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("领养申请已被其他管理员处理");
        }

        // ------------------------
        // 处理「同一只动物只允许一条申请通过」的业务规则
        // ------------------------
        ReviewStatus finalReviewStatus = reviewStatus;
        ApplicationStatus finalApplicationStatus = applicationStatus;
        String finalReason = reason;

        Review review = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetType, ReviewTargetType.ADOPT)
                        .eq(Review::getTargetId, adoptId)
                        .orderByDesc(Review::getCreateTime)
                        .last("limit 1"));
        if (review != null) {
            review.setStatus(finalReviewStatus);
            review.setReviewerId(reviewerId);
            if (finalReason != null && !finalReason.isBlank()) {
                review.setReason(finalReason.trim());
            }
            reviewMapper.updateById(review);
        }
        // 管理员更新 review_status 与 pass_time
        LambdaUpdateWrapper<Adopt> updateWrapper = new LambdaUpdateWrapper<Adopt>()
                .eq(Adopt::getAdoptId, adoptId)
                .eq(Adopt::getReviewStatus, ReviewStatus.PENDING)
                .eq(Adopt::getIsDeleted, false)
                .set(Adopt::getReviewStatus, finalReviewStatus)
                .set(Adopt::getPassTime, LocalDateTime.now());
        int affected = adoptMapper.update(null, updateWrapper);
        if (affected == 0) {
            throw new IllegalStateException("领养申请状态已变化，请刷新后重试");
        }
        Integer reviewId = review == null ? null : review.getReviewId();
        Integer recipientId = current.getUserId();
        String event = finalReviewStatus == ReviewStatus.APPROVED ? "通过" : "拒绝";
        notificationService.notifyActivity(recipientId, reviewerId, "adopt", adoptId,
                event, null, finalReason, null);

        if (finalReviewStatus == ReviewStatus.APPROVED && current.getAnimalId() != null) {
            Animal animal = animalMapper.selectById(current.getAnimalId());
            if (animal != null && animal.getUserId() != null) {
                notificationService.notifyActivity(animal.getUserId(), reviewerId, "review",
                        reviewId == null ? adoptId : reviewId, "新的待办事项", null, finalReason, null);
            }
        }

    }

    /**
     * 尝试将关键字解析为整数，失败则返回 null（用于按 ID 精确匹配）。
     */
    private Integer tryParseInt(String s) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
