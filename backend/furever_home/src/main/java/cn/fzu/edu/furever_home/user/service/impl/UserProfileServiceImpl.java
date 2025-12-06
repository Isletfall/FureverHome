package cn.fzu.edu.furever_home.user.service.impl;

import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.user.dto.UserDTO;
import cn.fzu.edu.furever_home.user.request.UpdateUserRequest;
import cn.fzu.edu.furever_home.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserMapper userMapper;
    private final cn.fzu.edu.furever_home.post.mapper.PostMapper postMapper;
    private final cn.fzu.edu.furever_home.animal.mapper.AnimalMapper animalMapper;
    private final cn.fzu.edu.furever_home.adopt.mapper.AdoptMapper adoptMapper;

    @Override
    public UserDTO getMe(Integer userId) {
        User u = userMapper.selectById(userId);
        return toDTO(u);
    }

    @Override
    public void updateMe(Integer userId, UpdateUserRequest req) {
        User u = userMapper.selectById(userId);
        if (u == null)
            return;
        if (req.getUserAge() != null)
            u.setUserAge(req.getUserAge());
        if (req.getAvatarUrl() != null)
            u.setAvatarUrl(req.getAvatarUrl());
        if (req.getSex() != null)
            u.setSex(req.getSex());
        if (req.getLocation() != null)
            u.setLocation(req.getLocation());
        if (req.getProofText() != null)
            u.setProofText(req.getProofText());
        if (req.getProofPhoto() != null)
            u.setProofPhoto(req.getProofPhoto());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    @Override
    public UserDTO getById(Integer userId) {
        User u = userMapper.selectById(userId);
        return toDTO(u);
    }

    @Override
    public cn.fzu.edu.furever_home.user.dto.UserStatsDTO getMyStats(Integer userId) {
        cn.fzu.edu.furever_home.user.dto.UserStatsDTO s = new cn.fzu.edu.furever_home.user.dto.UserStatsDTO();
        Long postCount = postMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.fzu.edu.furever_home.post.entity.Post>()
                        .eq(cn.fzu.edu.furever_home.post.entity.Post::getUserId, userId)
                        .eq(cn.fzu.edu.furever_home.post.entity.Post::getReviewStatus,
                                cn.fzu.edu.furever_home.common.enums.ReviewStatus.APPROVED));
        s.setPostCount(postCount == null ? 0L : postCount);

        Long shortCount = animalMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.fzu.edu.furever_home.animal.entity.Animal>()
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getUserId, userId)
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getReviewStatus,
                                cn.fzu.edu.furever_home.common.enums.ReviewStatus.APPROVED)
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getAdoptionStatus,
                                cn.fzu.edu.furever_home.common.enums.AdoptionStatus.SHORT_TERM));
        s.setShortTermPetCount(shortCount == null ? 0L : shortCount);

        Long longCount = animalMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.fzu.edu.furever_home.animal.entity.Animal>()
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getUserId, userId)
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getReviewStatus,
                                cn.fzu.edu.furever_home.common.enums.ReviewStatus.APPROVED)
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getAdoptionStatus,
                                cn.fzu.edu.furever_home.common.enums.AdoptionStatus.LONG_TERM));
        s.setLongTermPetCount(longCount == null ? 0L : longCount);

        Long apps = adoptMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.fzu.edu.furever_home.adopt.entity.Adopt>()
                        .eq(cn.fzu.edu.furever_home.adopt.entity.Adopt::getUserId, userId)
                        .eq(cn.fzu.edu.furever_home.adopt.entity.Adopt::getIsDeleted, false));
        s.setMyApplicationsCount(apps == null ? 0L : apps);

        java.util.List<Integer> myAnimalIds = animalMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.fzu.edu.furever_home.animal.entity.Animal>()
                        .eq(cn.fzu.edu.furever_home.animal.entity.Animal::getUserId, userId))
                .stream().map(cn.fzu.edu.furever_home.animal.entity.Animal::getAnimalId).toList();
        Long todos = myAnimalIds.isEmpty() ? 0L
                : adoptMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.fzu.edu.furever_home.adopt.entity.Adopt>()
                                .in(cn.fzu.edu.furever_home.adopt.entity.Adopt::getAnimalId, myAnimalIds)
                                .eq(cn.fzu.edu.furever_home.adopt.entity.Adopt::getIsDeleted, false)
                                .eq(cn.fzu.edu.furever_home.adopt.entity.Adopt::getIsCancelled, false)
                                .eq(cn.fzu.edu.furever_home.adopt.entity.Adopt::getReviewStatus,
                                        cn.fzu.edu.furever_home.common.enums.ReviewStatus.APPROVED)
                                .eq(cn.fzu.edu.furever_home.adopt.entity.Adopt::getApplicationStatus,
                                        cn.fzu.edu.furever_home.common.enums.ApplicationStatus.APPLYING));
        s.setMyTodosCount(todos == null ? 0L : todos);

        User u = userMapper.selectById(userId);
        if (u != null) {
            Integer c = u.getCreditScoreCount();
            Double sum = u.getCreditScore();
            s.setRatingCount(c);
            s.setRatingAverage(c == null || c == 0 || sum == null ? null : sum / c);
        }
        return s;
    }

    private UserDTO toDTO(User u) {
        if (u == null)
            return null;
        UserDTO d = new UserDTO();
        d.setUserId(u.getUserId());
        d.setUserName(u.getUserName());
        d.setUserAge(u.getUserAge());
        d.setEmail(u.getEmail());
        d.setAvatarUrl(u.getAvatarUrl());
        d.setSex(u.getSex());
        d.setLocation(u.getLocation());
        d.setProofText(u.getProofText());
        d.setProofPhoto(u.getProofPhoto());
        d.setCreditScore(u.getCreditScore());
        d.setCreditScoreCount(u.getCreditScoreCount());
        d.setStatus(u.getStatus());
        d.setCreateTime(u.getCreateTime());
        d.setUpdatedAt(u.getUpdatedAt());
        d.setLastLoginAt(u.getLastLoginAt());
        return d;
    }
}
