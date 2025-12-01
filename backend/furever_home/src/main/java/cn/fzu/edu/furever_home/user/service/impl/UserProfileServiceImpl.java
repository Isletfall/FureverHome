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

    @Override
    public UserDTO getMe(Integer userId) {
        User u = userMapper.selectById(userId);
        return toDTO(u);
    }

    @Override
    public void updateMe(Integer userId, UpdateUserRequest req) {
        User u = userMapper.selectById(userId);
        if (u == null) return;
        if (req.getUserAge() != null) u.setUserAge(req.getUserAge());
        if (req.getAvatarUrl() != null) u.setAvatarUrl(req.getAvatarUrl());
        if (req.getSex() != null) u.setSex(req.getSex());
        if (req.getLocation() != null) u.setLocation(req.getLocation());
        if (req.getProofText() != null) u.setProofText(req.getProofText());
        if (req.getProofPhoto() != null) u.setProofPhoto(req.getProofPhoto());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    @Override
    public UserDTO getById(Integer userId) {
        User u = userMapper.selectById(userId);
        return toDTO(u);
    }

    private UserDTO toDTO(User u) {
        if (u == null) return null;
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
