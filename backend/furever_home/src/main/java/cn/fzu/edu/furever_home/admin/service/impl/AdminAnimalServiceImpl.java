package cn.fzu.edu.furever_home.admin.service.impl;

import cn.fzu.edu.furever_home.admin.dto.AdminAnimalDetailDTO;
import cn.fzu.edu.furever_home.admin.dto.AdminAnimalSummaryDTO;
import cn.fzu.edu.furever_home.admin.service.AdminAnimalService;
import cn.fzu.edu.furever_home.animal.entity.Animal;
import cn.fzu.edu.furever_home.animal.mapper.AnimalMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.review.entity.Review;
import cn.fzu.edu.furever_home.review.mapper.ReviewMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAnimalServiceImpl implements AdminAnimalService {

    private final AnimalMapper animalMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final cn.fzu.edu.furever_home.notify.service.NotificationService notificationService;

    @Override
    public PageResult<AdminAnimalSummaryDTO> listPending(int page, int pageSize, String keyword) {
        Page<Animal> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<Animal>()
                .eq(Animal::getReviewStatus, ReviewStatus.PENDING)
                .orderByAsc(Animal::getAnimalId);
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            Integer id = tryParseInt(kw);
            wrapper.and(w -> {
                // 按宠物名称模糊搜索
                w.like(Animal::getAnimalName, kw);
                // 额外支持按动物 ID 精确搜索
                if (id != null) {
                    w.or().eq(Animal::getAnimalId, id);
                }
            });
        }
        Page<Animal> resultPage = animalMapper.selectPage(mpPage, wrapper);
        List<AdminAnimalSummaryDTO> records = resultPage.getRecords().stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal(), records);
    }

    //@Override
    //public PageResult<AdminAnimalSummaryDTO> listPendingShortTerm(int page, int pageSize, String keyword) {
    //    Page<Animal> mpPage = new Page<>(page, pageSize);
    //    LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<Animal>()
    //            .eq(Animal::getReviewStatus, ReviewStatus.PENDING)
    //            .eq(Animal::getAdoptionStatus, AdoptionStatus.SHORT_TERM)
    //            .orderByAsc(Animal::getAnimalId);
    //    if (keyword != null && !keyword.isBlank()) {
    //        String kw = keyword.trim();
    //        Integer id = tryParseInt(kw);
    //        wrapper.and(w -> {
    //            // 按宠物名称模糊搜索
    //            w.like(Animal::getAnimalName, kw);
    //            // 额外支持按动物 ID 精确搜索
    //            if (id != null) {
    //                w.or().eq(Animal::getAnimalId, id);
    //            }
    //        });
    //    }
    //    Page<Animal> resultPage = animalMapper.selectPage(mpPage, wrapper);
    //    List<AdminAnimalSummaryDTO> records = resultPage.getRecords().stream()
    //            .map(this::toSummaryDTO)
    //            .collect(Collectors.toList());
    //    return new PageResult<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal(), records);
    //}

    @Override
    public PageResult<AdminAnimalSummaryDTO> listApprovedByType(int page, int pageSize, String keyword, AdoptionStatus adoptionStatus) {
        Page<Animal> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<Animal>()
                .eq(Animal::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Animal::getAdoptionStatus, adoptionStatus)
                .orderByAsc(Animal::getAnimalId);
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            Integer id = tryParseInt(kw);
            wrapper.and(w -> {
                // 按宠物名称模糊搜索
                w.like(Animal::getAnimalName, kw);
                // 额外支持按动物 ID 精确搜索
                if (id != null) {
                    w.or().eq(Animal::getAnimalId, id);
                }
            });
        }
        Page<Animal> resultPage = animalMapper.selectPage(mpPage, wrapper);
        List<AdminAnimalSummaryDTO> records = resultPage.getRecords().stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal(), records);
    }

    @Override
    public AdminAnimalDetailDTO getDetail(Integer animalId) {
        Animal animal = animalMapper.selectById(animalId);
        if (animal == null) {
            return null;
        }
        AdminAnimalDetailDTO dto = new AdminAnimalDetailDTO();
        dto.setAnimalId(animal.getAnimalId());
        dto.setAnimalName(animal.getAnimalName());
        // animal.getPhotoUrls() 已经是列表类型，直接赋值给 DTO，避免类型转换错误
        dto.setPhotoUrls(animal.getPhotoUrls());
        dto.setSpecies(animal.getSpecies());
        dto.setBreed(animal.getBreed());
        dto.setGender(animal.getGender());
        dto.setAnimalAge(animal.getAnimalAge());
        dto.setHealthStatus(animal.getHealthStatus());
        dto.setSterilizedDisplay(animal.getIsSterilized() == null ? null : animal.getIsSterilized().getValue());
        dto.setAdoptionStatus(animal.getAdoptionStatus());
        dto.setReviewStatus(animal.getReviewStatus());
        dto.setShortDescription(animal.getShortDescription());
        dto.setCreatedAt(animal.getCreatedAt());
        if (animal.getUserId() != null) {
            User u = userMapper.selectById(animal.getUserId());
            if (u != null) {
                dto.setOwnerId(u.getUserId());
                dto.setOwnerName(u.getUserName());
                dto.setOwnerAvatar(u.getAvatarUrl());
            }
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Integer reviewerId, Integer animalId, String reason) {
        updateReviewAndAnimalStatus(reviewerId, animalId, reason, ReviewStatus.APPROVED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Integer reviewerId, Integer animalId, String reason) {
        updateReviewAndAnimalStatus(reviewerId, animalId, reason, ReviewStatus.REJECTED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer animalId) {
        if (animalId == null) {
            return;
        }
        int deleted = animalMapper.deleteById(animalId);
        if (deleted == 0) {
            throw new IllegalStateException("宠物不存在或已被删除");
        }
    }

    private AdminAnimalSummaryDTO toSummaryDTO(Animal a) {
        AdminAnimalSummaryDTO dto = new AdminAnimalSummaryDTO();
        dto.setAnimalId(a.getAnimalId());
        dto.setAnimalName(a.getAnimalName());
        dto.setSpecies(a.getSpecies());
        dto.setBreed(a.getBreed());
        dto.setGender(a.getGender());
        dto.setAnimalAge(a.getAnimalAge());
        dto.setAdoptionStatus(a.getAdoptionStatus());
        dto.setReviewStatus(a.getReviewStatus());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setOwnerId(a.getUserId());
        if (a.getUserId() != null) {
            User u = userMapper.selectById(a.getUserId());
            if (u != null) {
                dto.setOwnerName(u.getUserName());
                dto.setOwnerAvatar(u.getAvatarUrl());
            }
        }
        return dto;
    }

    private void updateReviewAndAnimalStatus(Integer reviewerId, Integer animalId, String reason, ReviewStatus status) {
        if (animalId == null) {
            throw new IllegalArgumentException("animalId 不能为空");
        }
        // 使用悲观锁（SELECT FOR UPDATE）锁定宠物记录，防止并发审核
        Animal current = animalMapper.selectOne(
                new LambdaQueryWrapper<Animal>()
                        .eq(Animal::getAnimalId, animalId)
                        .last("FOR UPDATE"));
        if (current == null) {
            throw new IllegalStateException("宠物不存在");
        }
        if (current.getReviewStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("宠物已被其他管理员处理");
        }
        Review review = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetType, ReviewTargetType.ANIMAL)
                        .eq(Review::getTargetId, animalId)
                        .orderByDesc(Review::getCreateTime)
                        .last("limit 1"));
        if (review != null) {
            review.setStatus(status);
            review.setReviewerId(reviewerId);
            if (reason != null && !reason.isBlank()) {
                review.setReason(reason.trim());
            }
            reviewMapper.updateById(review);
        }
        LambdaUpdateWrapper<Animal> updateWrapper = new LambdaUpdateWrapper<Animal>()
                .eq(Animal::getAnimalId, animalId)
                .eq(Animal::getReviewStatus, ReviewStatus.PENDING)
                .set(Animal::getReviewStatus, status);
        int affected = animalMapper.update(null, updateWrapper);
        if (affected == 0) {
            throw new IllegalStateException("宠物状态已变化，请刷新后重试");
        }
        Integer recipientId = current.getUserId();
        String event = status == cn.fzu.edu.furever_home.common.enums.ReviewStatus.APPROVED ? "通过" : "拒绝";
        notificationService.notifyActivity(recipientId, reviewerId, "animal", animalId, event, null, reason, null);
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


