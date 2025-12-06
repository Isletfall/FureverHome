package cn.fzu.edu.furever_home.user.service;

import cn.fzu.edu.furever_home.user.dto.UserDTO;
import cn.fzu.edu.furever_home.user.dto.UserStatsDTO;
import cn.fzu.edu.furever_home.user.request.UpdateUserRequest;

public interface UserProfileService {
    UserDTO getMe(Integer userId);
    void updateMe(Integer userId, UpdateUserRequest req);
    UserDTO getById(Integer userId);
    UserStatsDTO getMyStats(Integer userId);
}
