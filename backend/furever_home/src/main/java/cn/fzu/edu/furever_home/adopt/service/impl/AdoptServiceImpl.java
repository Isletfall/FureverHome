package cn.fzu.edu.furever_home.adopt.service.impl;

import cn.fzu.edu.furever_home.adopt.dto.AdoptDTO;
import cn.fzu.edu.furever_home.adopt.entity.Adopt;
import cn.fzu.edu.furever_home.adopt.mapper.AdoptMapper;
import cn.fzu.edu.furever_home.adopt.request.SubmitAdoptRequest;
import cn.fzu.edu.furever_home.adopt.service.AdoptService;
import cn.fzu.edu.furever_home.common.enums.ApplicationStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import cn.fzu.edu.furever_home.review.service.ReviewService;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdoptServiceImpl implements AdoptService {
    private final AdoptMapper adoptMapper;
    private final ReviewService reviewService;
    private final cn.fzu.edu.furever_home.animal.mapper.AnimalMapper animalMapper;
    private final UserMapper userMapper;
    private final cn.fzu.edu.furever_home.notify.service.NotificationService notificationService;

    @Override
    public Integer submit(Integer userId, SubmitAdoptRequest req) {
        cn.fzu.edu.furever_home.animal.entity.Animal animal = animalMapper.selectById(req.getAnimalId());
        if (animal == null) {
            throw new IllegalArgumentException("动物不存在或已删除");
        }
        if (animal.getUserId() != null && animal.getUserId().equals(userId)) {
            throw new IllegalStateException("不能申请领养自己的动物");
        }
        if (animal.getReviewStatus() != ReviewStatus.APPROVED) {
            throw new IllegalStateException("动物未通过审核，暂不可申请");
        }
        if (animal.getAdoptionStatus() != AdoptionStatus.SHORT_TERM) {
            throw new IllegalStateException("仅可申请短期领养宠物");
        }
        Long exists = adoptMapper.selectCount(new LambdaQueryWrapper<Adopt>()
                .eq(Adopt::getAnimalId, req.getAnimalId())
                .eq(Adopt::getUserId, userId)
                .eq(Adopt::getIsDeleted, false)
                .eq(Adopt::getIsCancelled, false)
                .eq(Adopt::getApplicationStatus, ApplicationStatus.APPLYING)
                .ne(Adopt::getReviewStatus, ReviewStatus.REJECTED));
        if (exists != null && exists > 0) {
            throw new IllegalStateException("已提交过该动物的领养申请");
        }
        Adopt a = new Adopt();
        a.setAnimalId(req.getAnimalId());
        a.setUserId(userId);
        a.setApplicationStatus(ApplicationStatus.APPLYING);
        a.setReviewStatus(ReviewStatus.PENDING);
        a.setUserName(req.getUserName());
        a.setPhone(req.getPhone());
        a.setEmail(req.getEmail());
        a.setProvince(req.getProvince());
        a.setCity(req.getCity());
        a.setLivingLocation(req.getLivingLocation());
        a.setAdoptReason(req.getAdoptReason());
        a.setCreateTime(LocalDateTime.now());
        adoptMapper.insert(a);
        reviewService.createPending(ReviewTargetType.ADOPT, a.getAdoptId());
        return a.getAdoptId();
    }

    @Override
    public AdoptDTO getById(Integer id) {
        Adopt a = adoptMapper.selectById(id);
        if (a == null || Boolean.TRUE.equals(a.getIsDeleted()))
            return null;
        return toDTO(a);
    }

    @Override
    public void review(Integer reviewerUserId, Integer id, ApplicationStatus status) {
        Adopt a = adoptMapper.selectById(id);
        if (a == null)
            throw new IllegalStateException("申请不存在");
        if (Boolean.TRUE.equals(a.getIsDeleted()))
            throw new IllegalStateException("申请不存在");
        if (a.getReviewStatus() != ReviewStatus.APPROVED)
            throw new IllegalStateException("管理员未通过审核，无法进行用户审核");
        cn.fzu.edu.furever_home.animal.entity.Animal animal = animalMapper.selectById(a.getAnimalId());
        if (animal == null)
            throw new IllegalStateException("动物不存在");
        if (animal.getUserId() == null || !animal.getUserId().equals(reviewerUserId))
            throw new IllegalStateException("无权审核该领养申请");
        if (status != ApplicationStatus.SUCCESS && status != ApplicationStatus.FAIL) {
            throw new IllegalArgumentException("状态不合法");
        }
        a.setApplicationStatus(status);
        adoptMapper.updateById(a);

        if (status == ApplicationStatus.SUCCESS && a.getUserId() != null) {
            animal.setUserId(a.getUserId());
            animal.setAdoptionStatus(AdoptionStatus.LONG_TERM);
            animal.setUpdatedAt(LocalDateTime.now());
            animalMapper.updateById(animal);

            java.util.List<Adopt> others = adoptMapper.selectList(new LambdaQueryWrapper<Adopt>()
                    .eq(Adopt::getAnimalId, a.getAnimalId())
                    .eq(Adopt::getIsDeleted, false)
                    .ne(Adopt::getAdoptId, a.getAdoptId()));
            if (!others.isEmpty()) {
                String reason = "该动物已被其他申请人成功领养";
                for (Adopt o : others) {
                    o.setApplicationStatus(ApplicationStatus.FAIL);
                    adoptMapper.update(null, new LambdaUpdateWrapper<Adopt>()
                            .eq(Adopt::getAdoptId, o.getAdoptId())
                            .eq(Adopt::getIsDeleted, false)
                            .set(Adopt::getApplicationStatus, ApplicationStatus.FAIL));
                    if (o.getUserId() != null) {
                        notificationService.notifyActivity(o.getUserId(), reviewerUserId, "adopt", o.getAdoptId(),
                                "申请失败", null, reason, null);
                    }
                }
            }
        }

        String event = status == ApplicationStatus.SUCCESS ? "申请成功" : "申请失败";
        notificationService.notifyActivity(a.getUserId(), reviewerUserId, "adopt", a.getAdoptId(), event, null, null,
                null);

    }

    @Override
    public java.util.List<Adopt> listByAnimalOwner(Integer ownerUserId) {
        java.util.List<Integer> myAnimalIds = animalMapper
                .selectList(new LambdaQueryWrapper<cn.fzu.edu.furever_home.animal.entity.Animal>()
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getUserId, ownerUserId))
                .stream().map(cn.fzu.edu.furever_home.animal.entity.Animal::getAnimalId).toList();
        if (myAnimalIds.isEmpty())
            return java.util.Collections.emptyList();
        return adoptMapper.selectList(new LambdaQueryWrapper<Adopt>().in(Adopt::getAnimalId, myAnimalIds)
                .eq(Adopt::getIsDeleted, false)
                .eq(Adopt::getIsCancelled, false)
                .eq(Adopt::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Adopt::getApplicationStatus, ApplicationStatus.APPLYING)
                .orderByDesc(Adopt::getCreateTime));
    }

    @Override
    public java.util.List<Adopt> listByApplicant(Integer applicantUserId) {
        return adoptMapper.selectList(new LambdaQueryWrapper<Adopt>().eq(Adopt::getUserId, applicantUserId)
                .eq(Adopt::getIsDeleted, false)
                .orderByDesc(Adopt::getCreateTime));
    }

    @Override
    public java.util.List<cn.fzu.edu.furever_home.adopt.dto.AdoptTodoItemDTO> listTodoItems(Integer ownerUserId) {
        java.util.List<Adopt> adopts = listByAnimalOwner(ownerUserId);
        java.util.List<cn.fzu.edu.furever_home.adopt.dto.AdoptTodoItemDTO> list = new java.util.ArrayList<>();
        for (Adopt a : adopts) {
            cn.fzu.edu.furever_home.adopt.dto.AdoptTodoItemDTO dto = new cn.fzu.edu.furever_home.adopt.dto.AdoptTodoItemDTO();
            dto.setAdoptId(a.getAdoptId());
            User u = userMapper.selectById(a.getUserId());
            if (u != null) {
                dto.setApplicantId(u.getUserId());
                dto.setApplicantAvatar(u.getAvatarUrl());
                dto.setApplicantName(u.getUserName());
            }
            cn.fzu.edu.furever_home.animal.entity.Animal animal = animalMapper.selectById(a.getAnimalId());
            if (animal != null) {
                dto.setAnimalId(animal.getAnimalId());
                dto.setAnimalName(animal.getAnimalName());
                java.util.List<String> photos = animal.getPhotoUrls();
                dto.setAnimalPhoto(photos == null || photos.isEmpty() ? null : photos.get(0));
            }
            dto.setReason(a.getAdoptReason());
            dto.setCreateTime(a.getCreateTime());
            dto.setApplicationStatus(mapConfirmStatus(a.getApplicationStatus()));
            list.add(dto);
        }
        return list;
    }

    @Override
    public java.util.List<cn.fzu.edu.furever_home.adopt.dto.MyAdoptItemDTO> listMyApplicationItems(
            Integer applicantUserId) {
        java.util.List<Adopt> adopts = listByApplicant(applicantUserId);
        java.util.List<cn.fzu.edu.furever_home.adopt.dto.MyAdoptItemDTO> list = new java.util.ArrayList<>();
        for (Adopt a : adopts) {
            cn.fzu.edu.furever_home.adopt.dto.MyAdoptItemDTO dto = new cn.fzu.edu.furever_home.adopt.dto.MyAdoptItemDTO();
            dto.setAdoptId(a.getAdoptId());
            cn.fzu.edu.furever_home.animal.entity.Animal animal = animalMapper.selectById(a.getAnimalId());
            if (animal != null) {
                User owner = animal.getUserId() != null ? userMapper.selectById(animal.getUserId()) : null;
                if (owner != null) {
                    dto.setTargetUserId(owner.getUserId());
                    dto.setTargetUserAvatar(owner.getAvatarUrl());
                    dto.setTargetUserName(owner.getUserName());
                }
                dto.setAnimalId(animal.getAnimalId());
                dto.setAnimalName(animal.getAnimalName());
                java.util.List<String> photos = animal.getPhotoUrls();
                dto.setAnimalPhoto(photos == null || photos.isEmpty() ? null : photos.get(0));
            }
            dto.setReason(a.getAdoptReason());
            dto.setCreateTime(a.getCreateTime());
            dto.setReviewStatus(String.valueOf(a.getReviewStatus()));
            dto.setApplicationStatus(mapConfirmStatus(a.getApplicationStatus()));
            dto.setIsCanceled(String.valueOf(a.getIsCancelled()));
            list.add(dto);
        }
        return list;
    }

    private String mapConfirmStatus(ApplicationStatus st) {
        if (st == null)
            return "待审核";
        switch (st) {
            case APPLYING:
                return "待审核";
            case SUCCESS:
                return "已同意";
            case FAIL:
                return "已拒绝";
            default:
                return "待审核";
        }
    }

    @Override
    public void updateMyApplication(Integer userId, Integer id,
            cn.fzu.edu.furever_home.adopt.request.UpdateAdoptRequest req) {
        Adopt a = adoptMapper.selectById(id);
        if (a == null)
            throw new IllegalStateException("申请不存在");
        if (Boolean.TRUE.equals(a.getIsCancelled()))
            throw new IllegalStateException("申请已撤销");
        if (!a.getUserId().equals(userId))
            throw new IllegalStateException("无权修改该申请");
        a.setUserName(req.getUserName());
        a.setEmail(req.getEmail());
        a.setPhone(req.getPhone());
        a.setProvince(req.getProvince());
        a.setCity(req.getCity());
        a.setLivingLocation(req.getLivingLocation());
        a.setAdoptReason(req.getAdoptReason());
        a.setReviewStatus(ReviewStatus.PENDING);
        a.setPassTime(null);
        adoptMapper.updateById(a);
        reviewService.createPending(ReviewTargetType.ADOPT, a.getAdoptId());
    }

    private AdoptDTO toDTO(Adopt a) {
        if (a == null)
            return null;
        AdoptDTO d = new AdoptDTO();
        d.setAdoptId(a.getAdoptId());
        d.setAnimalId(a.getAnimalId());
        d.setUserId(a.getUserId());
        d.setApplicationStatus(a.getApplicationStatus());
        d.setUserName(a.getUserName());
        d.setPhone(a.getPhone());
        d.setEmail(a.getEmail());
        d.setProvince(a.getProvince());
        d.setCity(a.getCity());
        d.setLivingLocation(a.getLivingLocation());
        d.setAdoptReason(a.getAdoptReason());
        d.setCreateTime(a.getCreateTime());
        d.setPassTime(a.getPassTime());
        d.setReviewStatus(a.getReviewStatus());
        if (a.getUserId() != null) {
            User applicant = userMapper.selectById(a.getUserId());
            if (applicant != null) {
                d.setApplicantAvatar(applicant.getAvatarUrl());
            }
        }
        if (a.getAnimalId() != null) {
            cn.fzu.edu.furever_home.animal.entity.Animal animal = animalMapper.selectById(a.getAnimalId());
            if (animal != null && animal.getUserId() != null) {
                User owner = userMapper.selectById(animal.getUserId());
                if (owner != null) {
                    d.setTargetUserId(owner.getUserId());
                    d.setTargetUserName(owner.getUserName());
                    d.setTargetUserAvatar(owner.getAvatarUrl());
                }
            }
            if (animal != null) {
                d.setAnimalPhotoUrls(animal.getPhotoUrls());
            }
        }
        return d;
    }

    @Override
    public void cancel(Integer userId, Integer id) {
        Adopt a = adoptMapper.selectById(id);
        if (a == null)
            throw new IllegalStateException("申请不存在");
        if (!a.getUserId().equals(userId))
            throw new IllegalStateException("无权撤销该申请");
        if (Boolean.TRUE.equals(a.getIsCancelled()))
            throw new IllegalStateException("申请已撤销");
        LambdaUpdateWrapper<Adopt> wrapper = new LambdaUpdateWrapper<Adopt>()
                .eq(Adopt::getAdoptId, id)
                .eq(Adopt::getIsDeleted, false)
                .set(Adopt::getIsCancelled, true)
                .set(Adopt::getCancelledAt, LocalDateTime.now());
        adoptMapper.update(null, wrapper);
    }
}
