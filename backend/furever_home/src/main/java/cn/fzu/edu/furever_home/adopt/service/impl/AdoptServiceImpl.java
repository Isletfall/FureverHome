package cn.fzu.edu.furever_home.adopt.service.impl;

import cn.fzu.edu.furever_home.adopt.dto.AdoptDTO;
import cn.fzu.edu.furever_home.adopt.entity.Adopt;
import cn.fzu.edu.furever_home.adopt.mapper.AdoptMapper;
import cn.fzu.edu.furever_home.adopt.request.SubmitAdoptRequest;
import cn.fzu.edu.furever_home.adopt.service.AdoptService;
import cn.fzu.edu.furever_home.common.enums.ApplicationStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.review.service.ReviewService;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdoptServiceImpl implements AdoptService {
    private final AdoptMapper adoptMapper;
    private final ReviewService reviewService;
    private final cn.fzu.edu.furever_home.animal.mapper.AnimalMapper animalMapper;

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
        Long exists = adoptMapper.selectCount(new LambdaQueryWrapper<Adopt>()
                .eq(Adopt::getAnimalId, req.getAnimalId())
                .eq(Adopt::getUserId, userId));
        if (exists != null && exists > 0) {
            throw new IllegalStateException("已提交过该动物的领养申请");
        }
        Adopt a = new Adopt();
        a.setAnimalId(req.getAnimalId());
        a.setUserId(userId);
        a.setApplicationStatus(ApplicationStatus.APPLYING);
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
        return toDTO(a);
    }

    @Override
    public void review(Integer id, ApplicationStatus status) {
        Adopt a = adoptMapper.selectById(id);
        if (a == null) throw new IllegalStateException("申请不存在");
        if (status != ApplicationStatus.SUCCESS && status != ApplicationStatus.FAIL) {
            throw new IllegalArgumentException("状态不合法");
        }
        a.setApplicationStatus(status);
        a.setReviewStatus(status == ApplicationStatus.SUCCESS ? ReviewStatus.APPROVED : ReviewStatus.REJECTED);
        a.setPassTime(LocalDateTime.now());
        adoptMapper.updateById(a);
        reviewService.updateStatus(ReviewTargetType.ADOPT, a.getAdoptId(), a.getReviewStatus());
    }

    @Override
    public java.util.List<Adopt> listByAnimalOwner(Integer ownerUserId) {
        java.util.List<Integer> myAnimalIds = animalMapper.selectList(new LambdaQueryWrapper<cn.fzu.edu.furever_home.animal.entity.Animal>()
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getUserId, ownerUserId))
                .stream().map(cn.fzu.edu.furever_home.animal.entity.Animal::getAnimalId).toList();
        if (myAnimalIds.isEmpty()) return java.util.Collections.emptyList();
        return adoptMapper.selectList(new LambdaQueryWrapper<Adopt>().in(Adopt::getAnimalId, myAnimalIds)
                .orderByDesc(Adopt::getCreateTime));
    }

    @Override
    public java.util.List<Adopt> listByApplicant(Integer applicantUserId) {
        return adoptMapper.selectList(new LambdaQueryWrapper<Adopt>().eq(Adopt::getUserId, applicantUserId)
                .orderByDesc(Adopt::getCreateTime));
    }

    private AdoptDTO toDTO(Adopt a) {
        if (a == null) return null;
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
        return d;
    }
}